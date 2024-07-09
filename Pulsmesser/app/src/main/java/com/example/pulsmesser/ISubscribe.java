package com.example.pulsmesser;

public interface ISubscribe {
    void OnMessageReceived(String message);
    void Unsubscribe();
}
