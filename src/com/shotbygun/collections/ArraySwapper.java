package com.shotbygun.collections;

import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author shotbygun
 * @param <T> classdef you want to store
 */
public class ArraySwapper<T> {
    
    // Locks
    private final ReentrantLock clientLock;
    private final Object masterLock;
    private Object notifyObject;
    
    // Major variables
    private boolean masterHasAlpha;
    private final ArrayQueue alpha, beta;
    
    // Client variables
    private ArrayQueue clientPointer;
    
    public ArraySwapper(Class<T> type, int arraySize) {
        alpha = new ArrayQueue(type, arraySize);
        beta = new ArrayQueue(type, arraySize);
        
        masterLock = new Object();
        clientLock = new ReentrantLock(false);
        notifyObject = new Object();
        
        masterHasAlpha = true;
        clientPointer = beta;
    }
    
    public ArraySwapper(Class<T> type, int arraySize, Object notifyObject) {
        this(type, arraySize);
        this.notifyObject = notifyObject;
    }
    
    public void put(T item) {
        
        try {
            
            // Obtain lock
            clientLock.lock();
            
            // Wait if queue is full
            
            /*
                Only one can of clients can try to obtain masterLock
                because if all threads were competing for masterLock, 
                then the consumer would have to compete vs everyone...
                And this class is supposed to be optimized for the one consumer
            */
            synchronized(masterLock) {
                while(!clientPointer.put(item)) {
                    notifyObject.notify();
                    masterLock.notify();
                    masterLock.wait();
                }
            }
            
            
        } catch (Exception ex) {
            //Log.critical("networking", this.getClass().getSimpleName(), "internal error on ArraySwapper", ex);
        } finally {
            // Unlock everything
            clientLock.unlock();
        }
        
    }
    
    public ArrayQueue<T> swap() {
        
        synchronized(masterLock) {
            
            if(masterHasAlpha) {
                alpha.clear();
                clientPointer = alpha;
            } else {
                beta.clear();
                clientPointer = beta;
            }

            masterHasAlpha = !masterHasAlpha;
            masterLock.notify();
        }
        
        if(masterHasAlpha)
            return alpha;
        else
            return beta;
    }
    
    /**
     * 
     * This is not thread safe operation
     * which means that values are estimate
     * 
     * @return int[2]
     */
    public int size() {
        int out = 0;
        out += alpha.size();
        out += beta.size();
        return out;
    }
    
}
