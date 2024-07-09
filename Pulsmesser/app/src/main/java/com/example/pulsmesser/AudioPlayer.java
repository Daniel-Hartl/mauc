package com.example.pulsmesser;

import android.content.Context;
import android.media.MediaPlayer;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class AudioPlayer {

    private MediaPlayer mediaPlayer;
    private Timer heartbeatTimer;
    private int pulse = 100;
    private Handler heartbeatHandler;
    private Runnable heartbeatRunnable;

    public AudioPlayer(Context context) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.create(context, R.raw.herzschlag);
        heartbeatHandler = new Handler();
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                playHeartbeat();
                heartbeatHandler.postDelayed(this, getHeartbeatInterval());
            }
        };
        startAudio();
    }


    public void startAudio() {
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
        return 60000 / pulse; // Intervall in Millisekunden (60 Sekunden / bpm)
    }
    public void setPulse(int newPulse) {
        this.pulse = newPulse;
        restartHeartbeatSimulation();
    }
    private void restartHeartbeatSimulation() {
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
            heartbeatHandler.post(heartbeatRunnable);
        }
    }
    public void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}


