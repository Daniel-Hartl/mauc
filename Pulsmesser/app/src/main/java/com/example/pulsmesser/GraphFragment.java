package com.example.pulsmesser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.lang.reflect.*;

import com.google.gson.Gson;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GraphFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GraphFragment extends Fragment implements ISubscribe, ISaveToDb{

    private LineChart lineChart;
    View root;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GraphFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GraphFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GraphFragment newInstance(String param1, String param2) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MqttModule.addSubscription(this);
        MqttModule.publishCtrlMessage(SelectedView.graph);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        root =  inflater.inflate(R.layout.fragment_graph, container, false);
        lineChart = root.findViewById(R.id.PulsGraph);
        return root;
    }

    @Override
    public void onMessageReceived(String message) {
        Object obj = new Gson().fromJson(message, Object.class);
        message = message.substring(message.indexOf("[[") + 2);
        GraphData data = new GraphData(message.substring(0, message.indexOf("]]")).split("\\},\\{"));
        setupLineChart();
        loadDataIntoChart(data.data);
        float lastY = data.data[message.substring(0, message.indexOf("]]")).split("\\},\\{").length].y;
        if(addToBuffer(lastY)>9) saveBuffer();
    }

    @Override
    public void unsubscribe() {
        MqttModule.removeSubscription(this);
        saveBuffer();
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                return mFormat.format(new Date((long) value));
            }
        });
    }

    private void loadDataIntoChart(Coordinate[] data) {
        List<Entry> entries = new ArrayList<>();

        for (Coordinate coordinate : data) {
            entries.add(new Entry(coordinate.x.getTime(), coordinate.y));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(ColorTemplate.COLORFUL_COLORS[0]);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart
        Log.d("GraphFragment", "Data loaded into chart");
    }



    int elementsInBuffer=0;
    float[] Buffer = new float[10];
    DatabaseManager databaseManager;
    @Override
    public float[] getBuffer() {
        return Buffer;
    }

    @Override
    public int addToBuffer(float element) {
        if(!databaseManager.isSavingEnabled()) return elementsInBuffer;
        Buffer[elementsInBuffer] = element;
        elementsInBuffer++;
        return elementsInBuffer;
    }

    @Override
    public void saveBuffer() {
        if(!databaseManager.isSavingEnabled()) return;
        if(elementsInBuffer < 0 || elementsInBuffer>10 || Buffer == null || databaseManager == null)return;
        float total=0;
        for(int i=0; i<elementsInBuffer; i++){
            total += Buffer[i];
        }
        databaseManager.insertPulseData_PULSE(total/elementsInBuffer);
        elementsInBuffer = 0;
    }

    @Override
    public void setDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
}


