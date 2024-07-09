package com.example.pulsmesser;

public interface ISubscribe {
    /**
     * Called when a message from the MQTT-Subscription is received.
     * @param message The stringified message.
     */
    void onMessageReceived(String message);

    /**
     * Should be called when the object is no longer needed, to free
     * up the subscription slot and should contain a call of the method
     * MqttModule.removeSubscription(this).
     */
    void unsubscribe();
}

