package com.example.pulsmesser;

import android.media.MediaPlayer;
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
import androidx.annotation.NonNull;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Fragment currentFragment;
    private boolean isSavingEnabled = true;
    private boolean isSoundEnabled = true;

    private boolean isAudioPlaying = false;

    private ConfigReader configReader;
    private DatabaseManager databaseManager;
    private AudioPlayer audioPlayer;

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
        configReader = new ConfigReader();
        configReader.loadConfig(this);

        audioPlayer = new AudioPlayer(MediaPlayer.create(this, R.raw.herz)) ;
        databaseManager = new DatabaseManager(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new PulseFragment());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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


        MqttModule.connect(configReader.getUrl(), configReader.getPort(),
                configReader.getTopicSend(), configReader.getTopicRecieve(),
                configReader.getUsername());

    }

    /**
     * Changes the view based on the selected fragment
     * @param fragment
     */
    private void replaceFragment(Fragment fragment){
        if (!MqttModule.getConnected())
            MqttModule.connect(configReader.getUrl(), configReader.getPort(),
                    configReader.getTopicSend(), configReader.getTopicRecieve(),
                    configReader.getUsername());
        if (currentFragment instanceof ISubscribe && currentFragment != null)
            ((ISubscribe)currentFragment).unsubscribe();
        if(fragment instanceof PulseFragment) {
           // ((PulseFragment) fragment).setMainActivity(this);
            ((PulseFragment) fragment).setAudioModule(audioPlayer);
        }
        if(fragment instanceof ISaveToDb)
            { ((ISaveToDb) fragment).setDatabaseManager(databaseManager);}
        FragmentManager mng = getSupportFragmentManager();
        FragmentTransaction transaction = mng.beginTransaction();
        transaction.add(R.id.contentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        currentFragment = fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_save) {
            isSavingEnabled = !item.isChecked();
            item.setChecked(isSavingEnabled);
            databaseManager.setSavingEnabled(isSavingEnabled);

            return true;
        }
        else if (item.getItemId() == R.id.action_toggle_audio) {
            isSoundEnabled = !item.isChecked();
            item.setChecked(isSoundEnabled);
            if(currentFragment instanceof PulseFragment)
                ((PulseFragment) currentFragment).toggleAudio(isSoundEnabled);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}