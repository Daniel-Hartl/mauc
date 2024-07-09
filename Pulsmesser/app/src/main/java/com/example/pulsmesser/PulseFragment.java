package com.example.pulsmesser;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PulseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PulseFragment extends Fragment implements ISubscribe, ISaveToDb {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    int elementsInBuffer;
    float[] Buffer = new float[10];

    DatabaseManager databaseManager;

    private AudioPlayer audioModule;

    public PulseFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PulseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PulseFragment newInstance(String param1, String param2) {
        PulseFragment fragment = new PulseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        elementsInBuffer = 0;
        MqttModule.addSubscription(this);
        MqttModule.publishCtrlMessage(SelectedView.heartRate);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    View root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        root =  inflater.inflate(R.layout.fragment_pulse, container, false);
        return root;
    }

    @Override
    public void onMessageReceived(String message) {
        TextView textPulse = root.findViewById(R.id.text_pulse);
        if (textPulse != null) {
            new Handler(Looper.getMainLooper()).post(()
                    -> textPulse.setText(new StringBuilder().append("Puls: ")
                    .append(message).append(" bpm").toString()));
        }

        // if message is no float => return
        if(!message.matches("[-+]?[0-9]*\\.?[0-9]+")) {
            audioModule.stopAudio();
            return;
        }

        if(addToBuffer(Float.parseFloat(message)) > 9)
            saveBuffer();

        audioModule.setPulse((int)Float.parseFloat(message));

    }



    @Override
    public void unsubscribe() {
        MqttModule.removeSubscription(this);
        saveBuffer();

    }

    @Override
    public int addToBuffer(float element) {
        Buffer[elementsInBuffer] = element;
        elementsInBuffer++;
        return elementsInBuffer;
    }

    @Override
    public void saveBuffer() {
        float erg = databaseManager.average(Buffer, elementsInBuffer);
        databaseManager.insertPulseData_PULSE(erg);
        elementsInBuffer = 0;
    }

    @Override
    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void setAudioModule(AudioPlayer module){
        if(module != null)
            audioModule = module;
    }

    public void toggleAudio(boolean isOn){
        if(isOn){
            audioModule.startAudio();
        }
        else
            audioModule.stopAudio();
    }
}