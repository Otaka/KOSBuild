package org.visualeagle.gui.small.longrunning;

/**
 * @author sad
 */
public class LongTaskReporter {
    
    boolean interrupted=false;
    public void setUnknown(){
        
    }
    
    public void setMax(long max){
    
    }
    
    public void setCurrent(long current){
    
    }
    
    public void setMessage(String message){
    
    }
    public void setError(String message){
    
    }
    
    public boolean isInterrupted(){
        return interrupted;
    }
    
    public void interrupt(){
        interrupted=true;
    }
}
