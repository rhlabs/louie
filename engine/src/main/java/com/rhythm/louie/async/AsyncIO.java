/*
 * AsyncIO.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.louie.async;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Iterator;
import java.util.List;

/**
 * Non-callable AsyncIO tool (execute it using the current thread)
 * This code starts/manages asynchronous communication with a number of other hosts
 * so long as they read/write Message
 * The incoming and outgoing information is managed via AsyncIOBundles
 * @author eyasukoc
 */
public class AsyncIO {
    
    private static final int DEF_TIMEOUT_MS = 15000;
    private static final int DEF_SIMUL_CONN = 50;
    
    Selector selector;
    private final int simultaneousConns;
    private final long maxTimeout;
    
    public AsyncIO(int numSimultaneousConns, long maxTimeoutMS) throws IOException {
        this.selector = Selector.open();
        this.simultaneousConns = numSimultaneousConns;
        this.maxTimeout = maxTimeoutMS*1000000;
    }
    
    public AsyncIO() throws IOException {
        this(DEF_SIMUL_CONN, DEF_TIMEOUT_MS);
        //TODO: Build in prop driven settings for asynchronous behavior
//        this(ServiceProperties.getServiceProperties("async")
//                .getCustomIntegerProperty("simul_conn", DEF_SIMUL_CONN),
//            ServiceProperties.getServiceProperties("async")
//                .getCustomIntegerProperty("timeout", DEF_TIMEOUT_MS));
    }
    
    /**
     * Executes socket level Message communication with multiple hosts, using 
     * NIO Selectors 
     * The number of simultaneous connections and max timeout are configurable
     * via the Constructor or a a set of configs(NOT YET).
     * @param bundles 
     * @throws Exception 
     */
    public void execute(List<AsyncIOBundle> bundles) throws Exception {
        long start = System.nanoTime();
        int openedConns = 0;
        int subviewStart = 0;
        int subviewEnd = 0;
        List<AsyncIOBundle> unconnectedBundles = null;
        for (int i = 0; i < bundles.size(); i++) {
            AsyncIOBundle bundle = bundles.get(i);
            try {
                openConnection(bundle);
            } catch (AsyncIOCreationException ex) {
                continue;
            }
            openedConns++;
            if (openedConns == simultaneousConns && i+1 < bundles.size()) {
                subviewStart = ++i;
                subviewEnd = bundles.size();
                unconnectedBundles = bundles.subList(subviewStart, subviewEnd);
                break;
            }
        }
        
        boolean connectionAvailable = false;
        try {
            while (!selector.keys().isEmpty() && (System.nanoTime()-start < maxTimeout)) {  
                selector.select(2);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    
                    SelectionKey key = keys.next();
                    if (!key.isValid()) continue;
                    SocketChannel channel = (SocketChannel) key.channel();
                    AsyncIOBundle bundle = (AsyncIOBundle) key.attachment();

                    if (key.isConnectable()) { 
                        try {
                            connect(channel, key);
                        } catch (AsyncIOException aex) {
                            bundle.setErrorMessage(aex.getMessage());
                            key.cancel();
                            keys.remove();
                            channel.close();
                            connectionAvailable = true;
                            openedConns--;
                            continue;      
                        }
                    }

                    if (key.isWritable() && !bundle.isDoneWriting()) {
                        write(channel, bundle, key);
                    } 

                    if (key.isReadable() && bundle.isDoneWriting()) {
                        try {
                            read(channel, bundle);
                        } catch (AsyncIOShutdownException shutdown) {
                            key.cancel();
                            keys.remove();
                            channel.close();
                            connectionAvailable = true;
                            openedConns--;
                        }
                    }
                    //new connection queue management logic
                    if (connectionAvailable && unconnectedBundles != null) {
                        if (unconnectedBundles.isEmpty()) {
                            unconnectedBundles = null;
                            continue;
                        }
                        if(openedConns <= simultaneousConns) {
                            AsyncIOBundle innerBundle = unconnectedBundles.get(0);
                            try {
                                openConnection(innerBundle);
                            } catch (AsyncIOCreationException ex) {
                                continue;
                            }
                            unconnectedBundles = bundles.subList(++subviewStart,subviewEnd); //self eliminating views
                            openedConns++;
                            connectionAvailable = false;
                            break;
                        }
                    }
                } 
            }
//            System.out.println("Async IO Time cost (ms): " + (System.nanoTime() - start)/1000000);
//            return bundles;
        } finally {
            selector.close();
            for (AsyncIOBundle bundle : bundles) {
                bundle.forceComplete();
            }
        }
    }
    
    private void openConnection(AsyncIOBundle bundle) throws IOException, AsyncIOCreationException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        SelectionKey registered = channel.register(selector, SelectionKey.OP_CONNECT);
        try {
            channel.connect(new InetSocketAddress(bundle.getAddress(), bundle.getPort()));   
        } catch (UnresolvedAddressException ex) {
            bundle.setErrorMessage("Could not resolve host address.");
            channel.close();
            throw new AsyncIOCreationException();
        }
        registered.attach(bundle);
    }
    
    private void connect(SocketChannel channel, SelectionKey key) throws IOException, AsyncIOException{
        if (channel.isConnectionPending()){
            try {
                channel.finishConnect();
            } catch (ConnectException ex) {
                throw new AsyncIOException("Connection refused.");                
            } catch (NoRouteToHostException ex) {
                throw new AsyncIOException("No route to host");
            }
        }
        key.interestOps(SelectionKey.OP_WRITE);
    }
    
    private void write(SocketChannel channel, AsyncIOBundle bundle, SelectionKey key) throws IOException {
        ByteBuffer tbw = bundle.getToBeWritten();
        channel.write(tbw);

        if (!tbw.hasRemaining()) {
            bundle.setDoneWriting();
            key.interestOps(SelectionKey.OP_READ);
        }
    }
    
    private void read(SocketChannel channel, AsyncIOBundle bundle) throws IOException, AsyncIOShutdownException {
        ByteBuffer readBuffer = ByteBuffer.allocate(8192);       
        int length;
        try {
            length = channel.read(readBuffer);
        } catch (IOException ex) {
            throw new AsyncIOShutdownException();
        }

        if (length >= 4 && bundle.bytesLeftToRead() == -1) {//just started read, therefore get amount to be read first
            readBuffer.flip();
            int readsize = readBuffer.getInt();
            bundle.setReadSize(readsize);
            byte[] arr = new byte[length-4];
            readBuffer.get(arr);
            bundle.addResponseBytes(arr);
        } else if (length > 0 ){
            readBuffer.flip();
            byte[] buff = new byte[length];
            readBuffer.get(buff);
            bundle.addResponseBytes(buff);
            if (bundle.bytesLeftToRead() <= 0 ){ //avoid additional iteration
                throw new AsyncIOShutdownException();
            }
        } else if ((length == 0 && bundle.bytesLeftToRead() <= 0 ) || length == -1) {  //end of stream or end or finished reading
            throw new AsyncIOShutdownException();
        }
    }
    
    /**
     * A total failure, indicating that the comm should be shut down for a 
     * given host.
     */
    private class AsyncIOShutdownException extends AsyncIOException {
        public AsyncIOShutdownException() {
            super(null);
        }
    }
    
    private class AsyncIOException extends Exception {
        AsyncIOException(String error) {
            super(error);
        }
    }
    
    private class AsyncIOCreationException extends AsyncIOException {
        public AsyncIOCreationException () {
            super(null);
        }
    }
    
    
    /**
     * DON'T USE THIS. Retained for reference only
     * @param outgoingBundles
     * @return
     * @throws Exception
     * @deprecated
     */
    @Deprecated //in favor of execute 
    private List<AsyncIOBundle> executeOld(List<AsyncIOBundle> outgoingBundles) throws Exception {
        long start = System.nanoTime();
        for (AsyncIOBundle bundle : outgoingBundles) {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            SelectionKey registered = channel.register(selector, SelectionKey.OP_CONNECT);
            try {
                channel.connect(new InetSocketAddress(bundle.getAddress(), bundle.getPort()));   
            } catch (UnresolvedAddressException ex) {
                bundle.setErrorMessage("Could not resolve host address.");
                channel.close();
                continue;
            }
            registered.attach(bundle);
//            bundles.add(bundle);
        }
        try {
            while (!selector.keys().isEmpty() && (System.nanoTime()-start < 15000000000L)) {  //fifteen second cap before this thing is killed
                selector.select(2);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    if (!key.isValid()) continue;
                    SocketChannel channel = (SocketChannel) key.channel();
                    AsyncIOBundle bundle = (AsyncIOBundle) key.attachment();

                    if (key.isConnectable()) { 
                        try {
                            connect(channel, key);
                        } catch (AsyncIOException aex) {
                            bundle.setErrorMessage(aex.getMessage());
                            key.cancel();
                            keys.remove();
                            channel.close();
                            continue;      
                        }
                    }

                    if (key.isWritable() && !bundle.isDoneWriting()) {
                        write(channel, bundle, key);
                    } 

                    if (key.isReadable() && bundle.isDoneWriting()) {
                        try {
                            read(channel, bundle);
                        } catch (AsyncIOShutdownException shutdown) {
                            key.cancel();
                            keys.remove();
                            channel.close();
                        }
                    }
                } 
            }
//            System.out.println("Async IO Time cost (ms): " + (System.nanoTime() - start)/1000000);
            return outgoingBundles;
        } finally {
            selector.close();
            for (AsyncIOBundle bundle : outgoingBundles) {
                bundle.forceComplete();
            }
        }
    }
}
