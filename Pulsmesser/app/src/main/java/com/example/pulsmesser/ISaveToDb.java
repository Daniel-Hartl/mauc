package com.example.pulsmesser;

public interface ISaveToDb{
    float[] getBuffer();
    int addToBuffer(float element);
    void saveBuffer();
    void setDatabaseManager(DatabaseManager databaseManager);
}