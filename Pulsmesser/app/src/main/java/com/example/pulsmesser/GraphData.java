package com.example.pulsmesser;

public class GraphData {
    public Coordinate[] data;

    /**
     * recieves the raw data from MQTT and refines it into an array of coordinates
     * @param coords
     */
    public GraphData(String[] coords){
        data = new Coordinate[coords.length];
        for(int i = 0; i < coords.length; i++){
            String[] temp = coords[i].replaceAll("\\}$|^\\{", "").split(",");
            data[i] = new Coordinate(temp[0].substring(4),
                                     temp[1].substring(4));
        }
    }
}

