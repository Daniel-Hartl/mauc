package com.example.pulsmesser;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pulsmesser.databinding.ActivityMainBinding;
import com.example.pulsmesser.ui.home.HomeFragment;
import com.example.pulsmesser.ui.notifications.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallback;

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.charset.StandardCharsets;





public class MainActivity extends AppCompatActivity implements MqttHelper.MessageListener {


    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };
    private ActivityMainBinding binding;
    private MqttHelper mqttHelper;
    private DatabaseManager databaseManager;

    private MediaPlayer mediaPlayer;
    private Handler heartbeatHandler;
    private Runnable heartbeatRunnable;

    private static final String TAG = "MainActivity"; //Für Fehlererkennung

    private double pulse = 100; //Variable für den Puls-Wert
    private double oxygen = 20; //Variable für den Sauerstoffgehalt

    private Handler handler = new Handler(Looper.getMainLooper());
    private Timer timer;
    private boolean dataUpdated = false;
    private boolean isSavingEnabled = true;
    private boolean isSoundEnabled = true;

    private String username;
    private String wifiSsid;
    private String wifiPassword;
    private  String brokerUrl;
    private  int brokerPort;
    private  String topic;
    private String topicRecieve;

    private int mode=0;


    private Mqtt3AsyncClient mqttClient;

    enum selectedView {
        puls,
        graph,
        sauerstoff
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadConfig();
        //connectToWifi();

        databaseManager = new DatabaseManager(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                sendMessage(selectedView.puls.ordinal());
                mode = selectedView.puls.ordinal();
            } else if (itemId == R.id.navigation_dashboard) {
                sendMessage(selectedView.graph.ordinal());
                mode = selectedView.graph.ordinal();
            } else if (itemId == R.id.navigation_notifications) {
                sendMessage(selectedView.sauerstoff.ordinal());
                mode = selectedView.sauerstoff.ordinal();
            }
            // Perform navigation
            return NavigationUI.onNavDestinationSelected(item, navController) || onOptionsItemSelected(item);
        });

        // Add a destination changed listener to update the view after navigation
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Adding a delay to ensure the fragment is fully in the foreground
            new Handler(Looper.getMainLooper()).postDelayed(this::updateView, 50);
        });

        // MQTT-Initialisierung und Verbindung, bzw. Fehlermeldung
        //initializeMqttInBackground();
        //startDataAggregation();
        mqttHelper = MqttHelper.getInstance(this, brokerUrl, brokerPort, topic, topicRecieve);
        // Connect to MQTT broker
        mqttHelper.connect(result -> {
            if (result) {
                Log.i(TAG, "MQTT connected successfully");
                startDataAggregation(); // Example method to start data aggregation
            } else {
                Log.e(TAG, "MQTT connection failed");
                Toast.makeText(MainActivity.this, "MQTT connection failed", Toast.LENGTH_LONG).show();
            }
        });
        mqttHelper.subscribeToReceiveTopic();
        mqttHelper.setMessageListener(this);

        // Initialisiere den MediaPlayer mit der Herzschlag-Sounddatei
        mediaPlayer = MediaPlayer.create(this, R.raw.puls_herzschlag);

        // Initialisiere den Handler und Runnable für die Herzschlag-Simulation
        heartbeatHandler = new Handler();
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSoundEnabled) {
                    playHeartbeat();
                }
                heartbeatHandler.postDelayed(this, getHeartbeatInterval());
            }
        };

        // Starte die Simulation des Herzschlags
        startHeartbeatSimulation();
    }






    public void onMessageReceived(String topic, String message) {
        Log.e(TAG, "Nachricht erhalten: " +message);
        runOnUiThread(() -> processMessage(message));
        updateView();
    }

    private void processMessage(String message) {
        if(mode == selectedView.puls.ordinal()){
            try {

                    double puls = Double.parseDouble(message);


                    // Verarbeiten der empfangenen Daten
                    Log.d(TAG, "Received: Pulse=" + puls + "bpm");

                    // Beispiel für die Aktualisierung der UI (falls notwendig)
                    this.pulse = puls;


                    // Beispiel für die Aktualisierung der UI nach einer Verzögerung
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        updateView(); // Hier wird Ihre Methode zum Aktualisieren der UI aufgerufen
                    }, 50);

                    dataUpdated = true; // Setzen Sie dataUpdated auf true, wenn die Daten aktualisiert wurden

            }catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing message: " + message, e);
            }

        } else if(mode == selectedView.sauerstoff.ordinal()){
            try {

                double oxygen = Double.parseDouble(message);


                // Verarbeiten der empfangenen Daten
                Log.d(TAG, "Received: Sauerstoffgehalt=" + oxygen + "%");

                // Beispiel für die Aktualisierung der UI (falls notwendig)
                this.oxygen = oxygen;


                // Beispiel für die Aktualisierung der UI nach einer Verzögerung
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    updateView(); // Hier wird Ihre Methode zum Aktualisieren der UI aufgerufen
                }, 50);

                dataUpdated = true; // Setzen Sie dataUpdated auf true, wenn die Daten aktualisiert wurden

            }catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing message: " + message, e);
            }
        }


    }

    private void updateView() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if (fragment != null) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            int currentDestinationId = navController.getCurrentDestination().getId();
            Log.d(TAG, "Current Destination ID: " + currentDestinationId);

            Fragment currentFragment = fragment.getChildFragmentManager().getPrimaryNavigationFragment();

            if (currentDestinationId == R.id.navigation_home && currentFragment instanceof HomeFragment) {
                HomeFragment homeFragment = (HomeFragment) currentFragment;
                homeFragment.updatePulseValue(pulse);
                Log.d(TAG, "Puls selected");
            } else if (currentDestinationId == R.id.navigation_notifications && currentFragment instanceof NotificationsFragment) {
                NotificationsFragment notificationsFragment = (NotificationsFragment) currentFragment;
                notificationsFragment.updateOxygenValue(oxygen);
                Log.d(TAG, "Sauerstoff selected");
            } else {
                Log.d(TAG, "Current fragment is not HomeFragment or NotificationsFragment");
            }
        }
    }

    private void sendMessage(int message) {
        try {
            mqttHelper.publishMessage(message);
            Log.d(TAG, "Message sent: " + message);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send message: " + message, e);
        }
    }
    private void startDataAggregation() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (dataUpdated) {
                    if (isSavingEnabled) {
                        // Save data to database
                        databaseManager.insertPulseData(username, pulse, oxygen);
                        dataUpdated = false; // Reset flag
                    }
                }
            }
        }, 0, 10000); // Run every 10 seconds
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_save) {
            isSavingEnabled = !item.isChecked();
            item.setChecked(isSavingEnabled);
            Toast.makeText(this, isSavingEnabled ? "Speichern aktiviert" : "Speichern deaktiviert", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_toggle_sound) {
            isSoundEnabled = !item.isChecked();
            item.setChecked(isSoundEnabled);
            Toast.makeText(this, isSoundEnabled ? "Herzschlag-Sound aktiviert" : "Herzschlag-Sound deaktiviert", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startHeartbeatSimulation() {
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.post(heartbeatRunnable);
        }
    }

    private void playHeartbeat() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private long getHeartbeatInterval() {
        // Berechne das Intervall basierend auf dem Pulswert
        return (long)(60000 / pulse); // Intervall in Millisekunden (60 Sekunden / bpm)
    }

    private void restartHeartbeatSimulation() {
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
            heartbeatHandler.post(heartbeatRunnable);
        }
    }

    private void loadConfig() {
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("mqtt_config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            username = properties.getProperty("username", "defaultUser");
            wifiSsid = properties.getProperty("wifi_ssid", "IoT");
            wifiPassword = properties.getProperty("wifi_password", "fnZ998wRP9");
            brokerPort = Integer.parseInt(properties.getProperty("broker_port", "1883"));
            brokerUrl = properties.getProperty("mqtt_broker_url", "broker.hivemq.com");
            topic = properties.getProperty("topic", "TestTopic");
            topicRecieve = properties.getProperty("topicRecieve", "TestTopic");

        } catch (IOException e) {
            Log.e(TAG, "Failed to load config file", e);
            e.printStackTrace();
            username = "defaultUser"; // Fallback or default value
        }
    }

    private void connectToWifi() {

            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
            builder.setSsid(wifiSsid);
            builder.setWpa2Passphrase(wifiPassword);

            WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

            NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();
            networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier);

            NetworkRequest networkRequest = networkRequestBuilder.build();

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    connectivityManager.bindProcessToNetwork(network);
                    Toast.makeText(MainActivity.this, "Connected to WiFi", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    Toast.makeText(MainActivity.this, "Failed to connect to WiFi", Toast.LENGTH_SHORT).show();
                }
            });

    }

    private void requestPermissions() {
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        } else {
            connectToWifi();
        }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (hasPermissions(this, REQUIRED_PERMISSIONS)) {
                connectToWifi();
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttHelper != null) {
            mqttHelper.disconnect();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (timer != null) {
            timer.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (heartbeatHandler != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
            heartbeatHandler = null;
        }
    }
}






