package com.example.pulsmesser;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import android.content.res.AssetManager;
import java.io.InputStream;

public class MqttHelper {
    public interface MessageListener {
        void onMessageReceived(String topic, String message);
    }

    private static final String TAG = "MqttHelper";
    private static MqttHelper instance;
    private final Mqtt3AsyncClient client;
    private final String brokerUrl;
    private final int brokerPort;
    private final String topic;
    private final String topicRecieve;

    private MessageListener messageListener;

    // Private constructor to prevent instantiation from outside
    private MqttHelper(Context context, String brokerUrl, int brokerPort, String topic, String topicRecieve) {
        this.brokerUrl = brokerUrl;
        this.brokerPort = brokerPort;
        this.topic = topic;
        this.topicRecieve = topicRecieve;
        client = MqttClient.builder()
                .useMqttVersion3()
                .serverHost(brokerUrl)
                .serverPort(brokerPort)
                .buildAsync();
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    // Method to get the singleton instance
    public static synchronized MqttHelper getInstance(Context context, String brokerUrl, int brokerPort, String topic, String topicRecieve) {
        if (instance == null) {
            instance = new MqttHelper(context.getApplicationContext(), brokerUrl, brokerPort, topic, topicRecieve);
        }
        return instance;
    }

    // Connect to MQTT broker
    public void connect(ConnectCallback callback) {
        CompletableFuture<Mqtt3ConnAck> connectFuture = client.connect();

        connectFuture.whenComplete((connAck, throwable) -> {
            if (throwable != null) {
                Log.e(TAG, "Connection failed: " + throwable.getMessage(), throwable);
                callback.onConnectResult(false);
            } else {
                Log.i(TAG, "Connected successfully");
                callback.onConnectResult(true);
            }
        });
    }

    public void subscribeToReceiveTopic() {
        if (client != null) {
            // Hier setzen Sie die geladene Topic

            client.subscribeWith()
                    .topicFilter(topicRecieve)
                    .callback(this::handleIncomingMessage)
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        if (throwable != null) {
                            Log.e(TAG, "Subscribe failed: " + throwable.getMessage());
                        } else {
                            Log.d(TAG, "Subscribed to topic for receiving: " + topicRecieve);
                        }
                    });
        } else {
            Log.e(TAG, "MQTT client is not initialized, unable to subscribe to receive topic");
        }
    }

    private void handleIncomingMessage(Mqtt3Publish publish) {
        String topic = publish.getTopic().toString();
        String message = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
        Log.d(TAG, "Received message on topic: " + topic + ", message: " + message);
        if (messageListener != null) {
            messageListener.onMessageReceived(topic, message);
        }
    }


    // Publish message to a topic
    public void publishMessage(int message) {
        client.publishWith()
                .topic(topic)
                .payload(String.valueOf(message).getBytes(StandardCharsets.UTF_8))
                .send()
                .whenComplete((publish, publishThrowable) -> {
                    if (publishThrowable != null) {
                        Log.e(TAG, "Publish failed: " + publishThrowable.getMessage(), publishThrowable);
                    } else {
                        Log.i(TAG, "Message published successfully!");
                    }
                });
    }

    // Callback interface for connection result
    public interface ConnectCallback {
        void onConnectResult(boolean success);
    }



    public void disconnect() {
        client.disconnect()
                .whenComplete((voidValue, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Disconnect failed: " + throwable.getMessage(), throwable);
                    } else {
                        Log.i(TAG, "Disconnected successfully");
                    }
                });
    }
}

