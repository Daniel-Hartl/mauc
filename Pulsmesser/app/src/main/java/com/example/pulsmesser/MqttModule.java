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

/**
 * The MQTT interface of this application.
 */
public class MqttModule {
    /**
     * The brokertopic to post ctrl messages to. (part of connection params)
     */
    private static String ctrlTopic;

    /**
     * The brokertopic to receive data messages from. (part of connection params)
     */
    private static String dataTopic;

    /**
     * The used name to connect to the broker. (part of connection params)
     */
    private static String clientName;

    /**
     * A array containing the active message subscriptions.
     */
    private static final ISubscribe[] subscribers = new ISubscribe[3];

    /**
     * The used paho client.
     */
    private static MqttClient client;

    /**
     * A boolean indicating whether the client is currently connected.
     */
    private static boolean isConnected;

    private static String uri;

    /**
     * Initializes the connection params.
     * @param newCtrlTopic the new ctrlTopic.
     * @param newDataTopic the new dataTopic.
     * @param newClientName the new clientName.
     */
    public static void init(String newCtrlTopic, String newDataTopic, String newClientName){
        ctrlTopic = newCtrlTopic;
        dataTopic = newDataTopic;
        clientName = newClientName;
    }

    /**
     * Checks whether the connection params are correctly initialized.
     * @return whether ctrlTopic, dataTopic and clientName arent null or empty.
     */
    public static boolean isInit(){
        return !(ctrlTopic == null || ctrlTopic.isEmpty()) &&
                !(dataTopic == null || dataTopic.isEmpty()) &&
                !(clientName == null || clientName.isEmpty());
    }

    /**
     * Tries to establish a connection to the broker.
     * @param brokerUrl the brokers url.
     * @param brokerPort the port the broker listens to.
     * @return whether the connection attempt was successful.
     */
    public static boolean connect(String brokerUrl, int brokerPort){
        uri = "tcp://" + brokerUrl + ":" + String.valueOf(brokerPort);
        return connect();
    }

    private static boolean connect(){
        // check if the connection params are set and return false
        if(!isInit())
            return false;

        // check whether a connection was already established and return true, as the client is connected.
        if(isConnected)
            return true;

        try{
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(uri,
                    clientName, persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            client.connect(options);
            isConnected = true;
        }
        catch (MqttException exc){
            Log.w("MqttModule.connect", exc);
            isConnected = false;
        }

        subscribeData();
        return isConnected;
    }

    /**
     * Combines connect(string, int) and init().
     * @param brokerUrl the brokers url.
     * @param brokerPort the port the broker listens to.
     * @param ctrlTopic the new ctrlTopic.
     * @param dataTopic the new dataTopic.
     * @param clientName the new clientName.
     */
    public static void connect(String brokerUrl, int brokerPort, String ctrlTopic, String dataTopic, String clientName){
        init(ctrlTopic, dataTopic, clientName);
        connect(brokerUrl, brokerPort);
    }

    /**
     * disconnects the client from the broker
     * @return whether the disconnection was successful.
     */
    public static boolean disconnect() {
        try {
            client.disconnect();
            isConnected = false;
        } catch (MqttException exc) {
            Log.w("MqttModule.disconnect", exc);
        }

        return !isConnected;
    }

    /**
     * publishes a message to the ctrl topic of the connected broker
     * @param message The enum value for the selected view.
     * @return a boolean value indicating whether the message has bin sent successfully.
     */
    public static boolean publishCtrlMessage(SelectedView message) {
        if(!isConnected || !isInit())
            return false;
        try {
            MqttMessage mqttMessage = new MqttMessage(String.valueOf(message.ordinal()).getBytes());
            client.publish(ctrlTopic, mqttMessage);
            return true;
        }
        catch (MqttException exc) {
            Log.w("MqttModule.publishCtrlMessage", exc);
            isConnected = false;
            return false;
        }
    }

    /**
     * Subscribes the data topic.
     * @return whether the subscription was completed successfully.
     */
    public static boolean subscribeData(){
        if(!isConnected || !isInit())
            return false;
        try {
            client.subscribe(dataTopic);
            client.setCallback(new MqttCallback() {

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    for (ISubscribe subscriber : subscribers) {
                        if (subscriber != null) {
                            subscriber.onMessageReceived(new String(message.getPayload(), StandardCharsets.UTF_8));
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
            isConnected = false;
            return false;
        }
    }

    /**
     * adds a subscriber.
     * @param newSub the new subscribing object.
     * @return whether the subscription is completed.
     */
    public static boolean addSubscription(ISubscribe newSub){
        for (int i = 0; i < subscribers.length; i++){
            if(subscribers[i] == null) {
                subscribers[i] = newSub;
                return true;
            }
        }

        return false;
    }

    /**
     * removes a subscriber.
     * @param deleteSub the subscribing object to be removed.
     * @return whether the unsubscription is completed.
     */
    public  static boolean removeSubscription(ISubscribe deleteSub){
        for (int i = 0; i < subscribers.length; i++){
            if(subscribers[i] == deleteSub) {
                subscribers[i] = null;
                return true;
            }
        }

        return false;
    }

    public static boolean getConnected(){
        return isConnected;
    }
}
