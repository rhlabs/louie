/* 
 * Copyright 2015 Rhythm & Hues Studios.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rhythm.louie.async;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.Message;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A data package to be used in conjunction with AsyncIO or AsyncIOCallable
 * These should be created on a per target host basis.
 * The message field in the constructor is the pb Message to be sent to the host
 * Any response will be captured in the response field and is accessible externally
 * via getResponseBytes(). 
 * It is also recommended to override doneReading as a callback for processing the 
 * read bytes into the responseMsg field, to optimize any extra wait time 
 * incurred by other hosts
 * 
 * For advanced usage, see setDoneReading() and getResponse() javadoc
 * @author eyasukoc
 * @param <T>
 */
public class AsyncIOBundle <T extends Message> {
    
    private final Message msg;
    private final ByteBuffer writeBuffer;
    private final String address;
    private final int port;
    private boolean doneWriting = false;
    private final List<Byte> response = new ArrayList<Byte>(50);
    protected T responseMsg = null;
    private String errorMsg;
    private int readSize = -1;
    private SettableFuture<T> sf = null;

    public AsyncIOBundle(Message message, String address, int port){
        this.msg = message;
        this.address = address;
        this.port = port;
        if (message != null) {
            int length = message.getSerializedSize();
            writeBuffer = ByteBuffer.allocate(4 + length);
            writeBuffer.putInt(length);
            writeBuffer.put(message.toByteArray());
            writeBuffer.flip();
        } else {
            writeBuffer = null;
        }
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public ListenableFuture<T> getFuture() {
        if (sf == null) {
            sf = SettableFuture.create();
        }
        return sf;
    }
    
    public void forceComplete() {
        if (readSize - response.size() != 0 || responseMsg == null) { //prevent duplicate calls to doneReading()
            doneReading();
            if (sf != null) sf.set(responseMsg);
        }
    }
    
    ///////////// WRITING //////////////
    
    public Message getOutgoingMessage() {
        return msg;
    }
    
    ByteBuffer getToBeWritten() {
        return this.writeBuffer;
    }
    
    void setDoneWriting() {
        doneWriting = true;
    }

    boolean isDoneWriting() {
        return doneWriting;
    }

    ///////////// READING //////////////
    
    int bytesLeftToRead() {
        return this.readSize - response.size();
    }
    
    void setReadSize(int size) {
        this.readSize = size;
    }

    void addResponseBytes(byte[] bytes) {
        for (byte b : bytes) {
            response.add(b);
        }
        if (readSize - response.size() <= 0) {
            doneReading();
            if (sf != null) sf.set(responseMsg);
        } 
    }

    public byte[] getResponseBytes() {
        byte[] ret = new byte[response.size()];                             // AAAAAAAAAAARGGHH
        for (int i = 0; i < response.size(); i++) {
            ret[i] = response.get(i);
        }
        return ret;
    }
    
    public synchronized boolean isDoneReading() {
        return (readSize - response.size() <= 0);
    }
    
    /**
     * Called when finished reading. Meant to be overriden.
     * This is for processing the response into a Message, so you can cache as you go
     * Then, the overriding method should set super.responseMsg = parsed message
     * to be retrieved by getResponse() to avoid redundant message parsing.
     */
    protected void doneReading() {}
    
    /**
     * Used in conjunction with setDoneReading, see above
     * @return a parsed Message, if available
     */
    public synchronized Message getResponse() {
        if (responseMsg == null) doneReading();
        return responseMsg;
    }
    
    ///////////// ERROR HANDLING //////////////////
    
    protected void setErrorMessage(String msg) {
        errorMsg = msg;
        doneReading();
        if (sf != null) sf.set(responseMsg);
    }
    
    public String getErrorMessage() {
        return errorMsg;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.msg != null ? this.msg.hashCode() : 0);
        hash = 79 * hash + (this.address != null ? this.address.hashCode() : 0);
        hash = 79 * hash + this.port;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AsyncIOBundle other = (AsyncIOBundle) obj;
        if (this.msg != other.msg && (this.msg == null || !this.msg.equals(other.msg))) {
            return false;
        }
        if ((this.address == null) ? (other.address != null) : !this.address.equals(other.address)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        return true;
    }
        
        
}
