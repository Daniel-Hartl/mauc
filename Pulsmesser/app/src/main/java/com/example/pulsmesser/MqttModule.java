package com.example.pulsmesser;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MqttModule {
    private static final String ctrlTopic = "afkdcdjkcnks/sensor/ctrl/raw/sensor";
    private static final String dataTopic = "afkdcdjkcnks/sensor/data/raw/sensor";

    private static ISubscribe[] subscribers = new ISubscribe[3];

    private static MqttClient client;
    private static boolean isConnected;

    public static boolean connect(String brokerUrl, int brokerPort, boolean forceReinit){

        if(isConnected && !forceReinit)
            return true;

        try{
            MemoryPersistence persistence = new MemoryPersistence();

            client = new MqttClient(brokerUrl + ":" + String.valueOf(brokerPort),
                    "app", persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            client.connect(options);
            isConnected = true;
        }
        catch (MqttException exc){
            Log.w("MqttModule", exc);
            isConnected = false;
        }

        return isConnected;
    }

    public static void connect(String brokerUrl, int brokerPort){
        connect(brokerUrl, brokerPort,false);
    }

    public static boolean disconnect() {
        try {
            client.disconnect();
            isConnected = false;
        } catch (MqttException exc) {
            Log.w("MqttModule", exc);
        }

        return !isConnected;
    }

    public static boolean publishCtrlMessage(SelectedView message) {
        if(!isConnected)
            return false;
        try {
            MqttMessage mqttMessage = new MqttMessage(String.valueOf(message.ordinal()).getBytes());
            client.publish(ctrlTopic, mqttMessage);
            return true;
        }
        catch (MqttException exc) {
            Log.w("MqttModule.publishCtrlMessage", exc);
            return false;
        }
    }

    public static boolean addSubscription(ISubscribe newSub){
        for (int i = 0; i < subscribers.length; i++){
            if(subscribers[i] == null) {
                subscribers[i] = newSub;
                return true;
            }
        }

        return false;
    }

    public  static boolean removeSubscription(ISubscribe deleteSub){
        for (int i = 0; i < subscribers.length; i++){
            if(subscribers[i] == deleteSub) {
                subscribers[i] = null;
                return true;
            }
        }

        return false;
    }

    public static boolean subscribeData(){
        if(!isConnected)
            return false;
        try {
            client.subscribe(dataTopic);
            client.setCallback(new MqttCallback() {

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    for (ISubscribe subscriber : subscribers) {
                        if (subscriber != null) {
                            subscriber.OnMessageReceived(new String(message.getPayload(), StandardCharsets.UTF_8));
                        }
                    }
                }

                // only overwritten to fulfill contract.
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}

                // only overwritten to fulfill contract.
                @Override
                public void connectionLost(Throwable cause) {}
            });
            return  true;
        } catch (MqttException exc) {
            Log.w("MqttModule.subscribeMessage", exc);
            return false;
        }
    }
}
