/*
    Designed to be ItemTransferQueue backed by two generic arrays
    - Optimized for multiple provider & one consumer use-case
*/

package com.shotbygun.collections;

import java.util.concurrent.CyclicBarrier;
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
    private CyclicBarrier notifyLatch;
    
    // Major variables
    private boolean masterHasAlpha;
    private final ArrayQueue alpha, beta;
    
    // Client variables
    private ArrayQueue queuePointer;
    
    public ArraySwapper(Class<T> type, int arraySize) {
        alpha = new ArrayQueue(type, arraySize);
        beta = new ArrayQueue(type, arraySize);
        
        masterLock = new Object();
        clientLock = new ReentrantLock(false);
        
        masterHasAlpha = true;
        queuePointer = beta;
    }
    
    /**
     * 
     * @param type
     * @param arraySize
     * @param notifyLatch optional, providers will reset() CyclicBarrier if backing ArrayQueue is full
     */
    public ArraySwapper(Class<T> type, int arraySize, CyclicBarrier notifyLatch) {
        this(type, arraySize);
        this.notifyLatch = notifyLatch;
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
                while(!queuePointer.put(item)) {
                    // put has failed, backing queue is full
                    
                    // Notify waiting consumer, by resetting CyclicBarrier
                    if(notifyLatch != null)
                        notifyLatch.reset();
                    
                    // Notify consumer if consumer is waiting us
                    masterLock.notify();
                    
                    // Wait until consumer has called swap()
                    masterLock.wait();
                }
            }
            
            
        } catch (InterruptedException ex) {
            //We expect high amount of interruptions
        } finally {
            // Unlock everything
            clientLock.unlock();
        }
        
    }
    
    public ArrayQueue<T> swap() {
        
        synchronized(masterLock) {
            
            if(masterHasAlpha) {
                alpha.reset();
                queuePointer = alpha;
            } else {
                beta.reset();
                queuePointer = beta;
            }

            masterHasAlpha = !masterHasAlpha;
            masterLock.notify();
        }
        
        if(masterHasAlpha)
            return alpha;
        else
            return beta;
    }
    
    public void clear() {
        alpha.clear();
        beta.clear();
    }
    
    /**
     * 
     * This is not thread safe operation
     * which means that values are estimate
     * 
     * @return integer, combined amount of items stored
     */
    public int size() {
        int out = 0;
        out += alpha.size();
        out += beta.size();
        return out;
    }
    
    /**
     * get capacity
     * @return integer, combined capacity of arrayQueues
     */
    public int length() {
        return alpha.length() + beta.length();
    }
    
}
