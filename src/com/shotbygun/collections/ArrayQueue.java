package com.shotbygun.collections;

import java.lang.reflect.Array;

/**
 *
 * @author shotbygun
 * @param <T> Object type to be stored
 */
public class ArrayQueue<T> {
    
    private final T[] array;
    private final int size;
    private int setter = 0;
    private int getter = 0;
    
    public ArrayQueue(Class<T> type, int size) {
        this.array = (T[]) Array.newInstance(type, size);
        this.size = size;
    }
    
    /**
     * 
     * @param item
     * @return true if insert successful
     */
    public boolean put(T item) {
        
        // if full, return false
        if(setter >= size)
            return false;
        
        // Insert item
        array[setter++] = item;
        
        // Insert successful
        return true;
    }
    
    public T pull() {
        if(getter >= setter)
            return null;
        
        return array[getter++];
    }
    
    /**
     * Nothing is actually removed, pointers are just set to 0
     */
    public void clear() {
        setter = 0;
        getter = 0;
    }
    
    /**
     * 
     * @return setter = amount of "live" objects
     */
    public int size() {
        return setter;
    }
    
}
