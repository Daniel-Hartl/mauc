package com.example.pulsmesser;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private String username;
    private String wifiSsid;
    private String wifiPassword;
    private String brokerUrl;
    private int brokerPort;
    private String topic;
    private String topicRecieve;

    public void loadConfig(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            username = properties.getProperty("username", "defaultUser");
            wifiSsid = properties.getProperty("wifi_ssid", "IoT");
            wifiPassword = properties.getProperty("wifi_password", "fnZ998wRP9");
            brokerPort = Integer.parseInt(properties.getProperty("broker_port", "1883"));
            brokerUrl = properties.getProperty("broker_url", "broker.hivemq.com");
            topic = properties.getProperty("topic", "TestTopic");
            topicRecieve = properties.getProperty("topicRecieve", "TestTopic");

        } catch (IOException e) {
            Log.e("ConfigReader", "Failed to load config file", e);
            e.printStackTrace();
            username = "defaultUser"; // Fallback or default value
        }
    }

    /**
     * returns the username from config.properties
     * @return username
     */
    public String getUsername(){
        return username;
    }

    /**
     * returns the MQTT-Broker-URL from config.properties
     * @return brokerUrl
     */
    public String getUrl(){
        return brokerUrl;
    }

    /**
     * returns the MQTT-Broker-Port from config.properties
     * @return brokerPort
     */
    public int getPort(){
        return brokerPort;
    }

    /**
     * returns the MQTT-Topic for sending from config.properties
     * @return topic
     */
    public String  getTopicSend(){
        return topic;
    }

    /**
     * returns the MQTT-Topic for recieving from config.properties
     * @return topicRecieve
     */
    public String  getTopicRecieve(){
        return topicRecieve;
    }
}
