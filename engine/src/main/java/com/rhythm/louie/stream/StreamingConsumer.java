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
package com.rhythm.louie.stream;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.SettableFuture;

import org.slf4j.LoggerFactory;

/**
 *
 * @author cjohnson
 * @param <T>
 */
public class StreamingConsumer<T> extends Consumer<T>{

    private final SettableFuture<Integer> count = SettableFuture.create();
    private final StreamList list;
    private final BlockingQueue<T> queue;
    
    public StreamingConsumer(int bufferSize) {
        if (bufferSize <= 0)
            throw new IllegalArgumentException("bufferSize must be >0");
        queue = new ArrayBlockingQueue<>(bufferSize);
        list = new StreamList();
    };
    
    @Override
    public void informMessageCount(int count) {
        this.count.set(count);
    }
    
    @Override
    public void consume(T item) {
        try {
            queue.put(item);
        } catch (InterruptedException ex) {
            LoggerFactory.getLogger(StreamingConsumer.class)
                        .error("Error consuming item", ex);
        }
    }
    
    public List<T> getStreamList() {
        return list;
    }
    
    
    private class StreamList implements Collection<T>, List<T> {
        public StreamList() {}

        @Override
        public int size() {
            try {
                return count.get();
            } catch (InterruptedException | ExecutionException ex) {
                return 0;
            }
        }

        @Override
        public boolean isEmpty() {
            return size()==0;
        }

        @Override
        public Iterator<T> iterator() {
            return new StreamItr();
        }

        /**
         * An iterator that returns the items in their original order, but
         * blocks if necessary
         */
        private class StreamItr implements Iterator<T> {
            int itemCount;
            int index = 0;

            public StreamItr() {
                itemCount = size();
            }

            @Override
            public boolean hasNext() {
                return index<itemCount;
            }

            @Override
            public T next() {
                T item = null;
                try {
                    item = queue.take();
                } catch (InterruptedException ex) {
                    LoggerFactory.getLogger(StreamingConsumer.class)
                        .error("Error Iterating over items", ex);
                }
                index++;
                
                return item;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        @Override
        public T get(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public T remove(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        public boolean add(T e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public T set(int index, T element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void add(int index, T element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean containsAll(Collection c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean equals(Object o) {
            return this==o;
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int indexOf(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int lastIndexOf(Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ListIterator<T> listIterator() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
