/*
 * FutureList.java
 * 
 * Copyright (c) 2014 Rhythm & Hues Studios. All rights reserved.
 */

package com.rhythm.util;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cjohnson
 * @param <E>
 */
public class FutureList<E> implements Collection<E>, List<E> {
    List<Future<E>> futures;
    public FutureList(List<Future<E>> futures) {
        this.futures = futures;
    }

    @Override
    public int size() {
        return futures.size();
    }

    @Override
    public boolean isEmpty() {
        return futures.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E get(int index) {
        try {
            return futures.get(index).get();
        } catch (InterruptedException ex) {
            Logger.getLogger(FutureList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(FutureList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        Iterator<Future<E>> futureIter = futures.iterator();
        
        @Override
        public boolean hasNext() {
            return futureIter.hasNext();
        }

        @Override
        public E next() {
            try {
                return futureIter.next().get();
            } catch (InterruptedException ex) {
                Logger.getLogger(FutureList.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(FutureList.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }

        @Override
        public void remove() {
            futureIter.remove();
        }
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
    public boolean add(Object e) {
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
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addAll(int index, Collection c) {
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
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(int index, E element) {
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
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
  
}
