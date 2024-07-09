package com.example.pulsmesser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.os.Handler;
import android.os.Looper;

import com.example.pulsmesser.databinding.FragmentO2Binding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link O2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class O2Fragment extends Fragment implements ISubscribe ,ISaveToDb{
    @Override
    public void onMessageReceived(String message) {
        TextView textO2 = root.findViewById(R.id.text_o2);
        if (textO2 != null) {
            new Handler(Looper.getMainLooper()).post(() -> textO2.setText(new StringBuilder().append("Sauerstoffgehalt: ").append(message).append("%").toString()));
        }
        if(addToBuffer(Float.parseFloat(message))>9) saveBuffer();
    }

    @Override
    public void unsubscribe() {
        MqttModule.removeSubscription(this);
        saveBuffer();
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public O2Fragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment O2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static O2Fragment newInstance(String param1, String param2) {
        O2Fragment fragment = new O2Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MqttModule.addSubscription(this);
        MqttModule.publishCtrlMessage(SelectedView.o2);
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
        root =  inflater.inflate(R.layout.fragment_o2, container, false);
        return root;
        //return inflater.inflate(R.layout.fragment_o2, container, false);
    }


    int elementsInBuffer=0;
    float[] Buffer = new float[10];
    DatabaseManager databaseManager;

    @Override
    public int addToBuffer(float element) {
        Buffer[elementsInBuffer] = element;
        elementsInBuffer++;
        return elementsInBuffer;
    }

    @Override
    public void saveBuffer() {
        float erg = databaseManager.average(Buffer, elementsInBuffer);
        databaseManager.insertPulseData_O2(erg);
        elementsInBuffer = 0;
    }

    @Override
    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
}