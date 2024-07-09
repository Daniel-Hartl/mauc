package com.example.pulsmesser;

import java.util.Date;

public class Coordinate {
    public Date x;
    public float y;

    public Coordinate(String x, String y){
        this.x = new Date(Long.parseLong(x));
        this.y = Float.parseFloat(y);
    }
}
