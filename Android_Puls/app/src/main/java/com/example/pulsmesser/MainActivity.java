package com.example.pulsmesser;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;

import com.example.pulsmesser.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MqttHelper.MqttMessageListener {

    private ActivityMainBinding binding;
    private MqttHelper mqttHelper;

    private static final String TAG = "MainActivity";

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
        try {
            // MQTT-Initialisierung und Verbindung
            mqttHelper = new MqttHelper(this, this);
            mqttHelper.connect();
        } catch (Exception e) {
            Log.e(TAG, "MQTT initialisation or connection failed", e);
        }
    }

    public void onMessageReceived(String topic, String message) {
        //runOnUiThread(() -> updateUI(message));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttHelper != null) {
            mqttHelper.disconnect();
        }
    }

}
