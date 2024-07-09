package com.example.pulsmesser;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.pulsmesser.databinding.ActivityMainBinding;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Fragment currentFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new PulseFragment());


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if(itemId == R.id.pulseMenuItem)
                replaceFragment(new PulseFragment());
            else if(itemId == R.id.graphMenuItem)
                replaceFragment(new GraphFragment());
            else if(itemId == R.id.o2menuItem)
                replaceFragment(new O2Fragment());
            return true;
        });

        MqttModule.connect("tcp://broker.hivemq.com", 1883);
        MqttModule.subscribeData();
        MqttModule.publishCtrlMessage(SelectedView.heartRate);

    }

    private void replaceFragment(Fragment fragment){
        if (currentFragment instanceof ISubscribe && currentFragment != null)
            ((ISubscribe)currentFragment).Unsubscribe();
        FragmentManager mng = getSupportFragmentManager();
        FragmentTransaction transaction = mng.beginTransaction();
        transaction.add(R.id.contentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        currentFragment = fragment;
    }
}