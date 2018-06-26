package sdkdemo.com.mybraintech.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Objects;

import core.bluetooth.BtState;
import core.eeg.storage.MbtEEGPacket;
import engine.MbtClient;

import engine.StreamConfig;
import engine.clientevents.BaseException;
import engine.clientevents.ConnectionException;
import engine.clientevents.ConnectionStateListener;
import engine.clientevents.DeviceInfoListener;
import engine.clientevents.EEGException;
import engine.clientevents.EegListener;
import features.MbtFeatures;
import utils.MatrixUtils;

import static utils.MatrixUtils.invertFloatMatrix;

public class DeviceActivity extends AppCompatActivity {

    private static String TAG = DeviceActivity.class.getName();
    private final int INDEX_STATUS = 0;


    private MbtClient client;

    private String deviceName;
    private TextView deviceNameTextView;

    private LineChart eegGraph;
    private LineData eegLineData;

    private LineDataSet channel1;
    private LineDataSet channel2;
    private ArrayList<ArrayList<Float>> bufferedChartData;
    private long chartCounter  = 0;
    private TextView channel1Quality;
    private TextView channel2Quality;

    private Button startStopStreamingButton;

    private Button disconnectButton;

    private Button readBatteryButton;
    private String lastReadBatteryLevel = "";

    private boolean isStreaming = false;

    private ConnectionStateListener<ConnectionException> connectionStateListener = new ConnectionStateListener<ConnectionException>() {
        @Override
        public void onError(ConnectionException exception) {
            notifyUser(getString(R.string.disconnect_failed));
        }

        @Override
        public void onStateChanged(@NonNull BtState newState) {
            currentState = newState;
            Log.i(TAG,"current state "+newState);
            if(currentState.equals(BtState.DISCONNECTED) ){
                notifyUser(getString(R.string.disconnected_headset));
                if(isStreaming)
                    notifyUser("Please try to connect again");
                returnOnPreviousActivity();
            }
        }
    };

    private DeviceInfoListener deviceInfoListener = new DeviceInfoListener() {
        @Override
        public void onBatteryChanged(String newLevel) {
            lastReadBatteryLevel = newLevel;
            notifyUser("Current battery level : "+lastReadBatteryLevel+" %");
        }

        @Override
        public void onFwVersionReceived(String fwVersion) {
        }

        @Override
        public void onHwVersionReceived(String hwVersion) {
        }

        @Override
        public void onSerialNumberReceived(String serialNumber) {
        }

        @Override
        public void onError(BaseException exception) {
            notifyUser(getString(R.string.error_read_battery));
        }
    };

    private BtState currentState;

    private EegListener<EEGException> eegListener = new EegListener<EEGException>() {

        @Override
        public void onError(EEGException exception) {
            Toast.makeText(DeviceActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNewPackets(final MbtEEGPacket mbtEEGPackets) {
            if(invertFloatMatrix(mbtEEGPackets.getChannelsData()) != null)
                mbtEEGPackets.setChannelsData(invertFloatMatrix(mbtEEGPackets.getChannelsData()));

            if(isStreaming){
                if(eegGraph!=null){
                    addEegDataToGraph(mbtEEGPackets);

                    //channel1Quality.setText(getString(R.string.channel_1_qc) + ((mbtEEGPackets.getQualities() != null && mbtEEGPackets.getQualities().get(0) != null) ? mbtEEGPackets.getQualities().get(0) : " -- "));
                    //channel2Quality.setText(getString(R.string.channel_2_qc) + ( (mbtEEGPackets.getQualities() != null && mbtEEGPackets.getQualities().get(1) != null ) ? mbtEEGPackets.getQualities().get(1) : " -- "));
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        client = MbtClient.getClientInstance();
        currentState = (BtState) getIntent().getSerializableExtra(HomeActivity.BT_STATE);

        initToolBar();
        initChannelsTextView();
        initDeviceNameTextView();
        initDisconnectButton();
        initReadBatteryButton();
        initStartStopStreamingButton();
        initEegGraph();

        client.setConnectionStateListener(connectionStateListener);


    }

    private void initDisconnectButton() {
        disconnectButton = findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStreaming)
                    stopStream();
                client.disconnectBluetooth();
            }
        });
    }

    private void initDeviceNameTextView() {
        deviceNameTextView = findViewById(R.id.deviceNameTextView);
        if(getIntent().hasExtra(HomeActivity.DEVICE_NAME)){
            deviceName = Objects.requireNonNull(getIntent().getExtras()).getString(HomeActivity.DEVICE_NAME,"");
            deviceNameTextView.setText(deviceName);
        }
    }

    private void initReadBatteryButton() {
        readBatteryButton = findViewById(R.id.readBatteryButton);
        readBatteryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deviceInfoListener != null)
                    client.readBattery(deviceInfoListener);
            }
        });
    }

    private void initChannelsTextView() {
        channel1Quality = findViewById(R.id.channel_1_quality);
        channel2Quality = findViewById(R.id.channel_2_quality);
        channel1Quality.setText(getString(R.string.channel_1_qc) + " -- ");
        channel2Quality.setText(getString(R.string.channel_2_qc) + " -- ");
    }

    private void initStartStopStreamingButton(){
        startStopStreamingButton= findViewById(R.id.startStopStreamingButton);
        startStopStreamingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isStreaming) { //streaming is not in progress : starting streaming
                    startStream(new StreamConfig.Builder(eegListener).setNotificationPeriod(MbtFeatures.DEFAULT_CLIENT_NOTIFICATION_PERIOD).create());
                    notifyUser("Starting streaming");
                }else { //streaming is in progress : stopping streaming
                    stopStream(); // set false to isStreaming et null to the eegListener
                    notifyUser("Stopping streaming");
                }
                updateStreaming(); //update the UI text in both case according to the new value of isStreaming

            }
        });
    }

    /**
     * Updates the streaming state boolean and the Stream button text
     * The Stream button text is changed into into "Stop Streaming" if streaming is started
     * or into "Start Streaming" if streaming is stopped
     */
    private void updateStreaming(){
        startStopStreamingButton.setText((isStreaming ? R.string.stop_streaming : R.string.start_streaming));
    }

    public void initEegGraph(){
        eegGraph = findViewById(R.id.eegGraph);

        channel1 = new LineDataSet(new ArrayList<Entry>(250), getString(R.string.channel1));
        channel2 = new LineDataSet(new ArrayList<Entry>(250), getString(R.string.channel2));

        channel1.setDrawValues(false);
        channel1.disableDashedLine();
        channel1.setDrawCircleHole(false);
        channel1.setDrawCircles(false);
        channel1.setColor(Color.rgb(3,32,123));
        channel1.setAxisDependency(YAxis.AxisDependency.LEFT);

        channel2.setDrawValues(false);
        channel2.disableDashedLine();
        channel2.setDrawCircleHole(false);
        channel2.setDrawCircles(false);
        channel2.setColor(Color.rgb(99,186,233));
        channel2.setAxisDependency(YAxis.AxisDependency.LEFT);

        eegLineData = new LineData();

        eegLineData.addDataSet(channel1);
        eegLineData.addDataSet(channel2);

        eegGraph.setData(eegLineData);

        XAxis xAxis = eegGraph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour

        eegGraph.setDoubleTapToZoomEnabled(false);
        eegGraph.setAutoScaleMinMaxEnabled(true);
        eegGraph.getAxisLeft().setDrawGridLines(false);
        eegGraph.getAxisLeft().setDrawLabels(true);
        eegGraph.getAxisRight().setDrawLabels(true);
        eegGraph.getAxisRight().setDrawGridLines(false);
        eegGraph.getXAxis().setDrawGridLines(false);

        eegGraph.invalidate();
    }

    private void addEntry(ArrayList<ArrayList<Float>> channelData) {

        LineData data = eegGraph.getData();
        if (data != null) {

            if(channelData.size()< MbtFeatures.getNbChannels()){
                throw new IllegalStateException("Incorrect matrix size, one or more channel are missing");
            }else{
                if(channelsHasTheSameNumberOfData(channelData)){
                    for(int currentEegData = 0; currentEegData< channelData.get(0).size(); currentEegData++){ //for each number of eeg data
                        for (int currentChannel = 0; currentChannel < MbtFeatures.getNbChannels() ; currentChannel++){
                            data.addEntry(new Entry(data.getDataSets().get(currentChannel).getEntryCount(), channelData.get(currentChannel).get(currentEegData) *1000000),currentChannel);
                        }

                    }
                }else{
                    throw new IllegalStateException("Channels do not have the same amount of data");
                }
            }
            data.notifyDataChanged();

            eegGraph.notifyDataSetChanged();// let the chart know it's data has changed
            eegGraph.setVisibleXRangeMaximum(500);// limit the number of visible entries
            eegGraph.moveViewToX((data.getEntryCount()/2));// move to the latest entry

        }else{
            throw new IllegalStateException("Graph not correctly initialized");
        }
    }

    private boolean channelsHasTheSameNumberOfData(ArrayList<ArrayList<Float>> data){
        boolean hasTheSameNumberOfData = true;

        int size = data.get(1).size();
        for (int i = 0 ; i < MbtFeatures.getNbChannels() ; i++){
            if(data.get(i).size() != size){
                hasTheSameNumberOfData = false;
            }
        }
        return hasTheSameNumberOfData;
    }

    private void updateEntry(ArrayList<ArrayList<Float>> channelData) {

        LineData lineData = eegGraph.getData();
        if (lineData != null) {
            for (ILineDataSet dataSet : lineData.getDataSets()){
                dataSet.clear();
            }
            if(channelData.size()< MbtFeatures.getNbChannels()){
                throw new IllegalStateException("Incorrect matrix size, one or more channel are missing");
            }else{
                if(channelsHasTheSameNumberOfData(bufferedChartData)){
                    for(int currentEegData = 0; currentEegData< bufferedChartData.get(0).size(); currentEegData++){ //250 loop
                        for (int channelIndex = 0; channelIndex < MbtFeatures.getNbChannels() ; channelIndex++){
                            lineData.addEntry(new Entry(lineData.getDataSetByIndex(channelIndex).getEntryCount(), bufferedChartData.get(channelIndex).get(currentEegData)*1000000),channelIndex);
                        }
                    }
                }else{
                    throw new IllegalStateException("Channels do not have the same amount of data");
                }

                if(channelsHasTheSameNumberOfData(channelData)){
                    for(int currentEegData = 0; currentEegData< channelData.get(0).size(); currentEegData++){

                        for (int channelIndex = 0; channelIndex < MbtFeatures.getNbChannels() ; channelIndex ++){
                            lineData.addEntry(new Entry(lineData.getDataSetByIndex(channelIndex).getEntryCount(), channelData.get(channelIndex).get(currentEegData)*1000000),channelIndex);
                        }
                    }
                }else{
                    throw new IllegalStateException("Channels do not have the same amount of data");
                }
            }
            lineData.notifyDataChanged();

            eegGraph.notifyDataSetChanged();// let the chart know it's data has changed
            eegGraph.setVisibleXRangeMaximum(500); // limit the number of visible entries
            eegGraph.moveViewToX((lineData.getEntryCount()/2));// move the view to the latest entry to avoid manual scrolling
        }else{
            throw new IllegalStateException("Graph not correctly initialized");
        }
    }

    private void addEegDataToGraph(MbtEEGPacket mbtEEGPackets) {
        chartCounter++;
        if(chartCounter <= 2)
            addEntry(mbtEEGPackets.getChannelsData());
        else
            updateEntry(mbtEEGPackets.getChannelsData());

        bufferedChartData = mbtEEGPackets.getChannelsData();
    }

    private void notifyUser(String message){
        Toast.makeText(DeviceActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        client.disconnectBluetooth();
        client.setConnectionStateListener(null);
        returnOnPreviousActivity();
    }

    private void returnOnPreviousActivity(){
        eegListener = null;
        connectionStateListener = null;
        deviceInfoListener = null;
        finish();
        startActivity(new Intent(DeviceActivity.this,HomeActivity.class));
    }

    public void initToolBar(){
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.light_blue)));
        }
    }

    private void stopStream(){
        isStreaming = false;
        client.stopStream();

    }

    private void startStream(StreamConfig streamConfig){
        isStreaming = true;
        client.startStream(streamConfig);
    }
}
