package com.example.pulsmesser;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.nio.charset.StandardCharsets;

public class MqttHelper {

    private Mqtt3AsyncClient mqttClient;
    private String brokerUrl;
    private String brokerPort;
    private String clientId;
    private String username;
    private String password;
    private String topic;
    private Context context;
    private MqttMessageListener messageListener;

    private static final String TAG = "MqttHelper";

    public interface MqttMessageListener {
        void onMessageReceived(String topic, String message);
    }

    public MqttHelper(Context context, MqttMessageListener listener) {
        this.context = context;
        this.messageListener = listener;
        try {
            loadConfig(context);
            mqttClient = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier(clientId)
                    .serverHost(brokerUrl)
                    .serverPort(Integer.parseInt(brokerPort))
                    .simpleAuth()
                    .username(username)
                    .password(password.getBytes())
                    .applySimpleAuth()
                    .buildAsync();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MqttHelper", e);
            throw e;
        }
    }

    private void loadConfig(Context context) { //Konfiguration aus der Konfigurationsdatei auslesen
        Properties properties = new Properties();
        try {
            InputStream inputStream = context.getAssets().open("mqtt_config.properties");
            properties.load(inputStream);
            brokerUrl = properties.getProperty("broker_url");
            brokerPort = properties.getProperty("broker_port");
            clientId = properties.getProperty("client_id");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            topic = properties.getProperty("topic");
        } catch (IOException e) {
            Log.e(TAG, "Error loading MQTT configuration", e);
        }
    }

    public void connect() { //MQTT-Verbndungsaufbau
        mqttClient.connectWith()
                .simpleAuth()
                .username(username)
                .password(password.getBytes())
                .applySimpleAuth()
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Connection failed", throwable);
                        showToast("Verbindung fehlgeschlagen: " + throwable.getMessage());
                    } else {
                        subscribeToTopic();
                        showToast("Erfolgreich verbunden");
                    }
                });
    }

    private void subscribeToTopic() {
        mqttClient.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    String payload = new String(publish.getPayloadAsBytes());
                    if (messageListener != null) {
                        messageListener.onMessageReceived(publish.getTopic().toString(), payload);
                    }
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Subscription failed", throwable);
                        showToast("Abonnement fehlgeschlagen: " + throwable.getMessage());
                    } else {
                        showToast("Erfolgreich abonniert");
                    }
                });
    }

    public void publishMessage(String message) {
        mqttClient.publishWith()
                .topic(topic)
                .payload(message.getBytes(StandardCharsets.UTF_8))
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Publishing failed", throwable);
                        showToast("Veröffentlichung fehlgeschlagen: " + throwable.getMessage());
                    } else {
                        showToast("Nachricht gesendet: " + message);
                    }
                });
    }

    public void disconnect() {
        mqttClient.disconnect()
                .whenComplete((disconnectAck, throwable) -> {
                    if (throwable != null) {
                        Log.e(TAG, "Disconnection failed", throwable);
                        showToast("Trennung fehlgeschlagen: " + throwable.getMessage());
                    } else {
                        showToast("Erfolgreich getrennt");
                    }
                });
    }

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }
}