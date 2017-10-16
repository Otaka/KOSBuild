package org.visualeagle.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry
 */
public class FlowArrayList<T> extends ArrayList<T>{

    public FlowArrayList(T...objects) {
        for(T t:objects){
            add(t);
        }
    }
    
    public FlowArrayList(List<T> objects) {
        for(T t:objects){
            add(t);
        }
    }
    
    public FlowArrayList<T>putObject(T object){
        add(object);
        return this;
    }
}
