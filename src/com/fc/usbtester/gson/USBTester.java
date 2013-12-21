/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fc.usbtester.gson;

/**
 *
 * @author wgarrido
 */
public class USBTester{
    
    private voltage v;
    private amp a;
    
    private Double shunt;
    
    private Double mah;
    private Double mwh;
    
    private Double dp;
    private Double dm;
    
    private int ram;
    
    public Double getV(){
        return v.getAvg();
    }
    
    public Double getVMax(){
        return v.getMax();
    }
    
    public Double getVMin(){
        return v.getMin();
    }
    
    public Double getA(){
        return a.getAvg();
    }
    
    public Double getAMax(){
        return a.getMax();
    }
    
    public Double getAMin(){
        return a.getMin();
    }
    
    public Double getShunt(){
        return shunt;
    }
    
    public Double getmah(){
        return mah;
    }
    
    public Double getmwh(){
        return mwh;
    }
    
    public Double getDp(){
        return dp;
    }
    
    public Double getDm(){
        return dm;
    }

    public int getRAM(){
        return ram;
    }
    
}




