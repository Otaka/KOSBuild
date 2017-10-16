package org.visualeagle.utils;

import java.util.List;

/**
 * @author Dmitry
 */
public class Utils {

    public static int[] generateRangeIntArray(int start, int size) {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = start + i;
        }

        return result;
    }
    
    public static <T> T first(List<T>array){
        if(array.isEmpty()){
            throw new IllegalArgumentException("Array is empty. Cannot get first element");
        }

        return array.get(0);
    }
    
    public static <T> T first(T[]array){
        if(array.length==0){
            throw new IllegalArgumentException("Array is empty. Cannot get first element");
        }
        return array[0];
    }
    
    public static int first(int[]array){
        if(array.length==0){
            throw new IllegalArgumentException("Array is empty. Cannot get first element");
        }
        return array[0];
    }
    
    public static <T> T last(List<T>array){
        if(array.isEmpty()){
            throw new IllegalArgumentException("Array is empty. Cannot get last element");
        }
        return array.get(array.size()-1);
    }
    
    public static <T> T last(T[]array){
        if(array.length==0){
            throw new IllegalArgumentException("Array is empty. Cannot get last element");
        }
        return array[array.length-1];
    }
    
    public static int last(int[]array){
        if(array.length==0){
            throw new IllegalArgumentException("Array is empty. Cannot get last element");
        }
        return array[array.length-1];
    }
}