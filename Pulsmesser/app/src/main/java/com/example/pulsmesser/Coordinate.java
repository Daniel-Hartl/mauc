package com.example.pulsmesser;

import java.util.Date;

public class Coordinate {
    public Date x;
    public int y;

    public Coordinate(String x, String y){
        this.x = new Date(Long.parseLong(x));
        this.y = Integer.parseInt(y);
    }
}
