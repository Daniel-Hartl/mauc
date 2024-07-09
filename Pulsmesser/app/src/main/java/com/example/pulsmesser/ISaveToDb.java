package com.example.pulsmesser;

public interface ISaveToDb{
    int addToBuffer(float element);
    void saveBuffer();
    void setDatabaseManager(DatabaseManager databaseManager);
}