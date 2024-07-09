package com.example.pulsmesser;

import java.util.Date;

public class Coordinate {
    public Date x;
    public float y;

    /**
     * Converts the String inputs to the correct types and saves them
     * @param x
     * @param y
     */
    public Coordinate(String x, String y){
        this.x = new Date(Long.parseLong(x));
        this.y = Float.parseFloat(y);
    }
}
