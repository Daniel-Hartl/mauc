package com.example.pulsmesser;

import android.media.MediaPlayer;
import java.util.Timer;

import android.os.Handler;

public class AudioPlayer {

    private boolean isEnabled;
    private MediaPlayer mediaPlayer;
    private int pulse;
    private Handler heartbeatHandler;
    private Runnable heartbeatRunnable;

    public AudioPlayer(MediaPlayer context) {
        mediaPlayer = context;
        isEnabled = true;
        heartbeatHandler = new Handler();
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if(!isEnabled) {
                    context.pause();
                    return;
                }
                mediaPlayer.start();
                if(pulse != 0)
                    heartbeatHandler.postDelayed(this, getHeartbeatInterval());
                else if (mediaPlayer.isPlaying()) {
                    context.pause();
                }
            }
        };
    }


    public void startAudio() {
        isEnabled = true;
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.post(heartbeatRunnable);
        }
    }
    private long getHeartbeatInterval() {

        // Berechne das Intervall basierend auf dem Pulswert
        return 60000 / pulse; // Intervall in Millisekunden (60 Sekunden / bpm)
    }
    public void setPulse(int newPulse) {
        this.pulse = newPulse;
        if(pulse != 0)
            restartHeartbeatSimulation();
    }
    private void restartHeartbeatSimulation() {
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
            heartbeatHandler.post(heartbeatRunnable);
        }
    }
    public void stopAudio() {
        isEnabled = false;
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
}


