package com.example.pulsmesser;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
public class MainActivity extends AppCompatActivity implements MqttHelper.MqttMessageListener {

    private ActivityMainBinding binding;
    private MqttHelper mqttHelper;
    private DatabaseManager databaseManager;

    private static final String TAG = "MainActivity"; //Für Fehlererkennung

    private double pulse = 100; //Variable für den Puls-Wert
    private double oxygen = 20; //Variable für den Sauerstoffgehalt

    private Handler handler = new Handler(Looper.getMainLooper());
    private Timer timer;
    private boolean dataUpdated = false;
    private boolean isSavingEnabled = true;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

         username = loadUsernameFromConfig();

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
                sendMessage("puls");
            } else if (itemId == R.id.navigation_dashboard) {
                sendMessage("graph");
            } else if (itemId == R.id.navigation_notifications) {
                sendMessage("sauerstoff");
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
        try {
            mqttHelper = new MqttHelper(this, this);
            mqttHelper.connect();
        } catch (Exception e) {
            Log.e(TAG, "MQTT initialisation or connection failed", e);
        }
        startDataAggregation();
    }

    public void onMessageReceived(String topic, String message) {
        runOnUiThread(() -> processMessage(message));
        updateView();
    }

    private void processMessage(String message) {
        try {
            String[] parts = message.split(";");
            for (String part : parts) {
                if (part.trim().startsWith("Puls:")) {
                    String pulseValue = part.split(":")[1].trim().replace("bpm", "").trim();
                    pulse = Double.parseDouble(pulseValue);
                    new Handler(Looper.getMainLooper()).postDelayed(this::updateView, 50);
                } else if (part.trim().startsWith("Sauerstoff:")) {
                    String oxygenValue = part.split(":")[1].trim().replace("%", "").trim();
                    oxygen = Double.parseDouble(oxygenValue);
                    new Handler(Looper.getMainLooper()).postDelayed(this::updateView, 50);
                }
            }
            Log.d(TAG, "Pulse: " + pulse + " bpm, Oxygen: " + oxygen + "%");
            dataUpdated = true;
        } catch (Exception e) {
            Log.e(TAG, "Error processing message: " + message, e);
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

    private void sendMessage(String message) {
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

    private String loadUsernameFromConfig() {
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("config.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty("username");
        } catch (IOException e) {
            Log.e(TAG, "Failed to load username from config file", e);
            return "defaultUser"; // Fallback or default value
        }
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
        }
        return super.onOptionsItemSelected(item);
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
    }
}





