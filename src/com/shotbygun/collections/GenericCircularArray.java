/*
 * 
 */
package com.shotbygun.collections;

import java.lang.reflect.Array;

/**
 *
 * @author shotbygun
 * @param <T>
 * @see Not thread safe
 */
public class GenericCircularArray<T> {
    
    private final Class<T> type;
    private final int size;
    private final T[] arrayData;
    
    private int offset;
    
    public GenericCircularArray(Class<T> type, int size) {
        this.type = type;
        this.size = size;
        this.arrayData = (T[])Array.newInstance(type, size);
    }
    
    public void write(T[] data) {
            
        // For every input int
        for(int i = 0; i < data.length; i++) {

            arrayData[offset++] = data[i];

            // Roll over offset
            if(offset >= size)
                offset = 0;

        }
    }
    
    public T[] read(T[] localBuffer, int length) {
            
        int localOffset = getLocalOffset(offset - 1);

        for(int i = 0; i < length; i++) {
            localBuffer[i] = arrayData[localOffset--];
            localOffset = getLocalOffset(localOffset);
        }

        return localBuffer;
    }
    
    public int getLocalOffset(int i) {
        if(i > size)
            i -= size;
        if(i < 0)
            i += size;
        return i;
    }
    
}
