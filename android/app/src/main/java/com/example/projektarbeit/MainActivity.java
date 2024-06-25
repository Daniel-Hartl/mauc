package com.example.projektarbeit;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

//für MQTT
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.content.Context;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.example.projektarbeit.databinding.ActivityMainBinding;





public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MqttHelper mqttHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        // MQTT-Initialisierung und Verbindung
        mqttHelper = new MqttHelper(this, this);
        mqttHelper.connect();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttHelper != null) {
            mqttHelper.disconnect();
        }

    }


    public class MqttHelper {

        private MqttAndroidClient mqttAndroidClient;
        private String brokerIp;
        private String brokerPort;
        private String clientId;
        private String username;
        private String password;
        private String topic;
        private Context context;
        private MqttMessageListener messageListener;

        public interface MqttMessageListener {
            void onMessageReceived(String topic, MqttMessage message);
        }

        public MqttHelper(Context context, MqttMessageListener listener) {
            this.context = context;
            this.messageListener = listener;
            loadConfig(context);
            String brokerUrl = "tcp://" + brokerIp + ":" + brokerPort;
            mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId);
        }

        private void loadConfig(Context context) {
            Properties properties = new Properties();
            try {
                InputStream inputStream = context.getAssets().open("mqtt_config.properties");
                properties.load(inputStream);
                brokerIp = properties.getProperty("broker_ip");
                brokerPort = properties.getProperty("broker_port");
                clientId = properties.getProperty("client_id");
                username = properties.getProperty("username");
                password = properties.getProperty("password");
                topic = properties.getProperty("topic");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void connect() {
            try {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(username);
                options.setPassword(password.toCharArray());
                IMqttToken token = mqttAndroidClient.connect(options);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // Erfolgreich verbunden
                        subscribeToTopic();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Verbindung fehlgeschlagen
                        exception.printStackTrace();
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        private void subscribeToTopic() {
            try {
                mqttAndroidClient.subscribe(topic, 1, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // Erfolgreich abonniert
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Abonnement fehlgeschlagen
                        exception.printStackTrace();
                    }
                });

                mqttAndroidClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        // Verbindung verloren
                        showToast("Verbindung verloren: " + cause.getMessage());
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        // Nachricht empfangen
                        if (messageListener != null) {
                            messageListener.onMessageReceived(topic, message);
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // Nachricht erfolgreich zugestellt
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        public void disconnect() {
            try {
                IMqttToken token = mqttAndroidClient.disconnect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // Erfolgreich getrennt
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Trennung fehlgeschlagen
                        exception.printStackTrace();
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        private void showToast(final String message) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}