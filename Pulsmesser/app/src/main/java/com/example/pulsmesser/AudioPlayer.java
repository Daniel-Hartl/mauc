package com.example.pulsmesser;

import android.media.MediaPlayer;
import java.util.Timer;

import android.os.Handler;

public class AudioPlayer {
    /**
     * states if the audioplayer is enabled.
     */
    private boolean isEnabled;

    /**
     * the mediaplayer the audioPlayer wrapps.
     */
    private MediaPlayer mediaPlayer;

    /**
     * the pulse.
     */
    private int pulse;

    /**
     * the handler for the elapse of the timespan between the heartbeats.
     */
    private Handler heartbeatHandler;

    /**
     * the runnable creating an interval between heartbeats.
     */
    private Runnable heartbeatRunnable;

    /**
     * the constructor configuring the Handler and Runnable.
     * @param context the mediaplayer initialized by the caller.
     */
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

    /**
     * (Re)starts the audio after it has been paused and regives running permission.
     */
    public void startAudio() {
        isEnabled = true;
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.post(heartbeatRunnable);
        }
    }

    /**
     * calculates the interval between two heartbeats
     * @return the interval between heartbeats in ms
     */
    private long getHeartbeatInterval() {
        return 60000 / pulse; // Interval (60sec / bpm)
    }

    /**
     * set the pulse and adjust the beat frequence.
     * @param newPulse the new pulse as int.
     */
    public void setPulse(int newPulse) {
        this.pulse = newPulse;
        if(pulse != 0)
            restartHeartbeatSimulation();
    }

    /**
     * Restarts the audio after it has been paused.
     */
    private void restartHeartbeatSimulation() {
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
            heartbeatHandler.post(heartbeatRunnable);
        }
    }

    /**
     * stops the heartbeat and removes running permission.
     */
    public void stopAudio() {
        isEnabled = false;
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
}


