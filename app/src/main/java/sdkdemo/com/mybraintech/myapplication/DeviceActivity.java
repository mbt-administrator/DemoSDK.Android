package sdkdemo.com.mybraintech.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.Objects;

import config.StreamConfig;
import core.bluetooth.BtState;
import core.device.model.MbtDevice;
import core.eeg.storage.MbtEEGPacket;
import engine.MbtClient;

import engine.SimpleRequestCallback;
import engine.clientevents.BaseError;
import engine.clientevents.BluetoothStateListener;
import engine.clientevents.DeviceBatteryListener;
import engine.clientevents.EegListener;

import static utils.MatrixUtils.invertFloatMatrix;

/**
 *  View displayed when a headset is connected to the application.
 *  EEG streaming is plotted as a graph here.
 */
public class DeviceActivity extends AppCompatActivity {

    /**
     * TAG used for logging messages in the console
     */
    private static String TAG = DeviceActivity.class.getName();

    /**
     * The graph window displays 2 seconds of EEG streaming.
     */
    private static final int TIME_WINDOW = 2;

    /**
     * Instance of SDK client used to access all the SDK features
     */
    private MbtClient sdkClient;

    /**
     * TextView used to display the connected headset name and QR code
     */
    private TextView deviceTextView;

    /**
     * Graph used to plot the EEG raw data in real time.
     */
    private LineChart eegGraph;

    /**
     * Text view used to display the qualities of the channels in real time
     */
    private TextView channelQualities;

    /**
     * Button used start or stop the real time EEG streaming.
     * A streaming is started if you click on this button whereas no streaming was in progress.
     * The current streaming is stopped if you click on this button whereas a streaming was in progress.
     */
    private Button startStopStreamingButton;

    /**
     * Button used to disconnect the connected headset.
     * It also disconnects audio if the headset is connected in Bluetooth for audio streaming.
     */
    private Button disconnectButton;

    /**
     * Button used to get the current battery charge level of the connected headset.
     */
    private Button readBatteryButton;

    /**
     * Boolean value stored for the current Bluetooth connection state of the SDK.
     * {@link DeviceActivity#isConnected} is true if a headset is connected to the SDK, false otherwise.
     */
    private boolean isConnected = false;

    /**
     * Boolean value stored for the current EEG streaming state of the SDK.
     * {@link DeviceActivity#isStreaming} is true if a EEG streaming from the headset to the SDK is in progress, false otherwise.
     * */
    private boolean isStreaming = false;

    /**
     * Listener used to receive a notification when the Bluetooth connection state changes
     * If you just want to know when a headset is connected or disconnected,
     * you can replace the BluetoothStateListener listener with a ConnectionStateListener<BaseError> listener.
     */
    private BluetoothStateListener bluetoothStateListener;

    /**
     * Listener used to retrieve the EEG raw data when a streaming is in progress
     */
    private EegListener<BaseError> eegListener;

    /**
     * Instance of myBrain Technologies headset device connected in Bluetooth
     */
    private MbtDevice connectedDevice;


    /**
     * Method called by default when the Activity is started
     * It initializes all the views, SDK client, and permissions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device); // associate the XML view
        sdkClient = MbtClient.getClientInstance();//initialize the SDK

        //initialize the listeners
        initConnectionStateListener();
        initEegListener();
        sdkClient.setConnectionStateListener(bluetoothStateListener);

        //initialize the graphic elements
        initToolBar();
        initDeviceTextView();
        initDisconnectButton();
        initReadBatteryButton();
        initStreamingButton();
        initEegGraph();
    }

    /**
     * Method used to initialize the top tool bar view
     */
    public void initToolBar(){
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.light_blue)));
        }
    }

    /**
     * Method called to initialize the EEG raw data listener.
     * This listener provides a callback used to receive a notification when a new packet of EEG data is received
     */
    private void initEegListener() {
        eegListener = new EegListener<BaseError>() {
            /**
             * Callback used to receive a notification if the EEG streaming is aborted because the SDK returned an error
             */
            @Override
            public void onError(BaseError error, String additionalInfo) {
                String errorMessage = error.getMessage()+ (additionalInfo != null ? additionalInfo : "");
                Toast.makeText(DeviceActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                if(isStreaming) {
                    stopStream(); //streaming is stopped if an error occurred
                    updateStreamButton();
                }
            }

            /**
             * Callback used to receive a notification when a new packet of EEG data is received and retrieve its values.
             * The EEG data are returned as a MbtEEGPacket Object that contains a matrix of EEG data acquired during a time interval equals to the notification period.
             * Each column of the matrix contains all the EEG data values acquired by one channel during the whole period.
             * For example, one line of the matrix contains 2 EEG data as it has 2 channels of acquisition.
             * To get the matrix of EEG data, you need to call the following getter :
             * mbtEEGPackets.getChannelsData()
             * Note : Unit is microvolt.
             * The matrix need to be inverted to plot the EEG data on 2 differents lines on the graph.
             */
            @Override
            public void onNewPackets(@NonNull final MbtEEGPacket mbtEEGPackets) {
                if(invertFloatMatrix(mbtEEGPackets.getChannelsData()) != null) //EEG data matrix is inverted to facilitate the display in a graph
                    mbtEEGPackets.setChannelsData(invertFloatMatrix(mbtEEGPackets.getChannelsData()));

                if(isStreaming){
                    if(eegGraph != null){
                        addDataToGraph(mbtEEGPackets.getChannelsData(), mbtEEGPackets.getStatusData());
                        updateQualitiesView(mbtEEGPackets.getQualities());
                    }
                }
            }

        };
    }

    /**
     * Method called to initialize the connection state listener.
     * This listener provides a callback used to receive a notification when the Bluetooth connection state changes
     */
    private void initConnectionStateListener() {
        bluetoothStateListener = new BluetoothStateListener(){
            /**
             * Callback used to receive a notification when the Bluetooth connection state changes
             */
            @Override
            public void onNewState(BtState newState) {

            }

            /**
             * Callback used to receive a notification when the Bluetooth connection is established
             */
            @Override
            public void onDeviceConnected() {
                isConnected = true;
                sdkClient.requestCurrentConnectedDevice(new SimpleRequestCallback<MbtDevice>() {
                    @Override
                    public void onRequestComplete(MbtDevice device) {
                        connectedDevice = device;
                    }
                });
            }

            /**
             * Callback used to receive a notification when a connected headset is disconnected
             */
            @Override
            public void onDeviceDisconnected() {
                isConnected = false;
                connectedDevice = null;
                returnOnPreviousActivity();
            }

            /**
             * Callback used to receive a notification if the Bluetooth connection is aborted because the SDK returned an error
             */
            @Override
            public void onError(BaseError error, String additionalInfo) {
                notifyUser(error.getMessage()+(additionalInfo != null ? additionalInfo : ""));
            }
        };
    }


    /**
     * Method called to initialize the TextView used to display the connected headset name and QR code
     */
    private void initDeviceTextView() {
        deviceTextView = findViewById(R.id.deviceNameTextView);
        sdkClient.requestCurrentConnectedDevice(new SimpleRequestCallback<MbtDevice>() {

            /**
             * Callback used to get the connected headset informations
             * @param device is the connected headset
             */
            @Override
            public void onRequestComplete(MbtDevice device) {
                connectedDevice = device;

                if (connectedDevice != null){
                    String deviceName = connectedDevice.getSerialNumber();
                    String deviceQrCode = connectedDevice.getExternalName();
                    deviceTextView.setText(deviceName + " | " + deviceQrCode);
                }
            }
        });
    }

    /**
     * Method called to initialize the Button used to disconnect the connected headset on a click.
     * A click on this button also disconnects audio if the headset is connected in Bluetooth for audio streaming.
     */
    private void initDisconnectButton() {
        disconnectButton = findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStreaming)
                    stopStream();

                sdkClient.disconnectBluetooth();
            }
        });
    }

    /**
     * Method called to initialize the Button used to get the battery charge level of the connected headset
     */
    private void initReadBatteryButton() {
        readBatteryButton = findViewById(R.id.readBatteryButton);
        readBatteryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sdkClient.readBattery(new DeviceBatteryListener<BaseError>() {

                    /**
                     * Callback used to get the battery level of the connected headset
                     * @param newLevel is the current battery charge level
                     */
                    @Override
                    public void onBatteryChanged(String newLevel) {
                        notifyUser("Current battery level : "+newLevel+" %");
                    }

                    /**
                     * Callback used to receive a notification if the battery reading operation is aborted because the SDK returned an error
                     */
                    @Override
                    public void onError(BaseError error, String additionalInfo) {
                        notifyUser(getString(R.string.error_read_battery));
                    }
                });
            }
        });
    }

    /**
     * Method called to initialize the Button used start or stop the real time EEG streaming.
     * A streaming is started if you click on this button whereas no streaming was in progress.
     * The current streaming is stopped if you click on this button whereas a streaming was in progress.
     */
    private void initStreamingButton(){
        startStopStreamingButton = findViewById(R.id.startStopStreamingButton);
        startStopStreamingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isStreaming) {
                    startStream(new StreamConfig.Builder(eegListener)
                            .useQualities()
                            .create());
                }else  //streaming is in progress : stopping streaming
                    stopStream(); // set false to isStreaming et null to the eegListener

                updateStreamButton(); //update the UI text in both case according to the new value of isStreaming
            }
        });
    }

    /**
     * Method used to start a EEG raw data streaming.
     * Some parameters related to the streaming can be configured using the StreamConfig builder.
     * @param streamConfig is the streaming configuration
     */
    private void startStream(StreamConfig streamConfig){
        isStreaming = true;
        sdkClient.startStream(streamConfig);
    }

    /**
     * Method used to stop a EEG raw data streaming in progress.
     *
     */
    private void stopStream(){
        isStreaming = false;
        sdkClient.stopStream();
    }

    /**
     * Method called to update the text of the stream button according to the streaming state
     * The stream button text is changed into "Stop Streaming" if streaming is started
     * or into "Start Streaming" if streaming is stopped
     */
    private void updateStreamButton(){
        startStopStreamingButton.setText((isStreaming ?
                R.string.stop_streaming : R.string.start_streaming));
    }

    /**
     * Method called to initialize the Graph used to plot the raw EEG data
     */
    public void initEegGraph(){
        eegGraph = findViewById(R.id.eegGraph);
        LineData eegLineData = new LineData();

        int[] colors = new int[]{ //each channel is displayed as a colored curve
                Color.rgb(3,32,123),
                Color.rgb(99,186,233),
                Color.rgb(23,52,143),
                Color.rgb(119,206,233),
                Color.rgb(43,72,163),
                Color.rgb(139,226,233),
                Color.rgb(63,92,183),
                Color.rgb(159,233,233)
        };

        for (int channel = 0 ; channel < connectedDevice.getNbChannels(); channel++){ //each channel line is initialized
            LineDataSet lineDataSet = new LineDataSet(new ArrayList<Entry>(connectedDevice.getSampRate()), getString(R.string.channel)+" "+ (channel+1));
            lineDataSet.setDrawValues(false);
            lineDataSet.disableDashedLine();
            lineDataSet.setDrawCircleHole(false);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setColor(colors[channel]);
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            eegLineData.addDataSet(lineDataSet); //add all the lines on the graph for EEG
        }

        LineDataSet status = new LineDataSet(new ArrayList<Entry>(connectedDevice.getSampRate()), getString(R.string.status));//Object used to bundle all the triggers data to plot on the graph
        status.setDrawValues(false);
        status.disableDashedLine();
        status.setDrawCircleHole(false);
        status.setDrawCircles(false);
        status.setColor(Color.GREEN);
        status.setDrawFilled(true);
        status.setFillColor(Color.GREEN);
        status.setFillAlpha(40);
        status.setAxisDependency(YAxis.AxisDependency.RIGHT);
        eegLineData.addDataSet(status); //add a line on the graph for the status

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

        channelQualities = findViewById(R.id.qualities);
    }

    /**
     * Method called to update the label that display the values of the EEG signal quality of each channel
     * @param qualitiesList the qualities of all channels
     */
    private void updateQualitiesView(ArrayList<Float> qualitiesList) {
        StringBuilder qualities = new StringBuilder();
        for (int qualityTextView = 0 ; qualityTextView < connectedDevice.getNbChannels() ; qualityTextView++){
            if(qualityTextView != 0)//the channel qualities are separated by a vertical line
                qualities.append("   ");

            qualities.append(getString(R.string.quality))
                    .append( "(")
                    .append(qualityTextView+1)
                    .append( ") : ")
                    .append( qualitiesList != null ?
                            qualitiesList.get(qualityTextView)
                            : "--")
                    .append("   ");

            if(qualityTextView != connectedDevice.getNbChannels()-1)//the channel qualities are separated by a vertical line
                qualities.append(" | ");
        }
        channelQualities.setText(qualities.toString());
    }

    /**
     * Method called to add the entries to the graph every second
     * @param channelData the matrix of raw EEG data of the last second
     * @param statusData the list of triggers
     */
    private void addDataToGraph(ArrayList<ArrayList<Float>> channelData, ArrayList<Float> statusData) {

        LineData data = eegGraph.getData();
        if (data != null) {

            if(channelData.size()< connectedDevice.getNbChannels()){
                throw new IllegalStateException("Incorrect matrix size, one or more channel are missing");
            }else{
                if(channelsHasTheSameNumberOfData(channelData)){
                    for(int currentEegData = 0; currentEegData< channelData.get(0).size(); currentEegData++){ //for each number of eeg data
                        //plot the EEG signal
                        for (int currentChannel = 0; currentChannel < connectedDevice.getNbChannels() ; currentChannel++){
                            data.addEntry(new Entry(data.getDataSets().get(currentChannel).getEntryCount(), channelData.get(currentChannel).get(currentEegData) *1000000),currentChannel);
                        }

                        if(statusData != null) //plot the triggers
                            data.addEntry(new Entry(data.getDataSets().get(data.getDataSetCount()-1).getEntryCount(),
                                    statusData.get(currentEegData).isNaN() ? //received NaN are EEG packets that have been lost during Bluetooth transfer from the headset to the mobile
                                            Float.NaN : statusData.get(currentEegData)),
                                    data.getDataSetCount()-1);
                    }
                }else
                    throw new IllegalStateException("Channels do not have the same amount of data");
            }

            data.notifyDataChanged();
            eegGraph.notifyDataSetChanged();// let the chart know it's data has changed
            eegGraph.setVisibleXRangeMaximum(TIME_WINDOW * connectedDevice.getSampRate());// limits the number of visible entries. The graph window displays 2 seconds of EEG data. As the sampling frequency is 250 Hz, 250 new points are added to the graph every second
            eegGraph.moveViewToX(((float)data.getEntryCount() / 2 ));// move to the latest entry : previous entries are saved so that you can scroll on the left to visualize the previous seconds of acquisition.

        }else{
            throw new IllegalStateException("Graph not correctly initialized");
        }
    }

    /**
     * Method called to check that all the channels contains the same number of EEG data
     * @param channelData the matrix of raw EEG data of the last second
     * @return true if the channels contains the same number of EEG data, false otherwise
     */
    private boolean channelsHasTheSameNumberOfData(ArrayList<ArrayList<Float>> channelData){
        boolean hasTheSameNumberOfData = true;

        int size = channelData.get(0).size();
        for (int i = 1 ; i < connectedDevice.getNbChannels() ; i++){
            if(channelData.get(i).size() != size){
                hasTheSameNumberOfData = false;
            }
        }
        return hasTheSameNumberOfData;
    }

    /**
     * Method used to notify the user by showing a temporary message on the foreground
     * @param message is the temporary message to show
     */
    private void notifyUser(String message){
        Toast.makeText(DeviceActivity.this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Method called by default when the Android device back buttton is clicked.
     * All the listeners are set to null to avoid memory leaks.
     */
    @Override
    public void onBackPressed() {
        disconnectButton.performClick();
        returnOnPreviousActivity();
    }

    /**
     * Method called to return on the {@link HomeActivity} when the {@link DeviceActivity} is closed
     */
    private void returnOnPreviousActivity(){
        notifyUser(getString(R.string.disconnected_headset));
        //as the application goes back to the previous activity, we avoid memory leaks by setting the SDK listeners defined in this activity to null
        sdkClient.setConnectionStateListener(null);
        sdkClient.setEEGListener(null);
        eegListener = null;
        bluetoothStateListener = null;

        finish();
        Intent intent = new Intent(DeviceActivity.this, HomeActivity.class);
        intent.putExtra(HomeActivity.PREVIOUS_ACTIVITY_EXTRA, DeviceActivity.TAG); //the HomeActivity will check if it has been started from the Device Activity
        startActivity(intent);
    }
}
