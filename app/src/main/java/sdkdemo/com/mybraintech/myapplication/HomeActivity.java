package sdkdemo.com.mybraintech.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

import config.ConnectionConfig;
import core.bluetooth.BtState;
import engine.MbtClient;
import engine.clientevents.BaseError;
import engine.clientevents.BluetoothStateListener;
import features.MbtDeviceType;
import features.MbtFeatures;

import static features.MbtFeatures.MELOMIND_DEVICE_NAME_PREFIX;
import static features.MbtFeatures.VPRO_DEVICE_NAME_PREFIX;

public class HomeActivity extends AppCompatActivity{

    private static String TAG = HomeActivity.class.getName();
    private final static int SCAN_DURATION = 30000;
    public final static String DEVICE_NAME = "DEVICE_NAME";
    public final static String DEVICE_TYPE = "DEVICE_TYPE";
    public final static String PREVIOUS_ACTIVITY = "PREVIOUS_ACTIVITY";

    private MbtClient client;

    private EditText deviceNameField;
    private String deviceName;

    private Switch connectAudioSwitch;
    private boolean connectAudio = false;

    private Spinner devicePrefixSpinner;
    private String devicePrefix;

    private Button scanButton;

    private boolean isCancelled = false;

    private Toast toast;

    private BluetoothStateListener bluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onNewState(BtState newState) {

        }

        @Override
        public void onError(BaseError error, String additionnalInfo) {
            Log.e(TAG, "onError received "+error.getMessage()+ (additionnalInfo != null ? additionnalInfo : ""));
            updateScanning(false);
            toast = Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }

        @Override
        public void onDeviceConnected() {
            toast.cancel();
            deinitCurrentActivity(true);
        }

        @Override
        public void onDeviceDisconnected() {
            if(!toast.getView().isShown())
                notifyUser(getString(R.string.no_connected_headset));
            if(isCancelled)
                updateScanning(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initToolBar();
        toast = Toast.makeText(HomeActivity.this, "", Toast.LENGTH_LONG);
        client = MbtClient.init(getApplicationContext());
        isCancelled = false;

        if(getIntent().hasExtra(HomeActivity.PREVIOUS_ACTIVITY)){
            if(getIntent().getStringExtra(HomeActivity.PREVIOUS_ACTIVITY)!=null)
                client.setConnectionStateListener(bluetoothStateListener);
        }

        initDeviceNameField();
        initConnectAudioSwitch();
        initScanButton();
        initDevicePrefix();
    }

    private void initToolBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.light_blue)));
        }
    }

    private void initDeviceNameField() {
        deviceNameField = findViewById(R.id.deviceNameField);
    }

    private void initConnectAudioSwitch() {
        connectAudioSwitch = findViewById(R.id.connectAudio);
    }

    private void initScanButton(){
        scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyUser(getString(R.string.scan_in_progress));
                devicePrefix = String.valueOf(devicePrefixSpinner.getSelectedItem()); //get the prefix chosed by the user in the Spinner
                deviceName = devicePrefix+deviceNameField.getText().toString(); //get the name entered by the user in the EditText
                connectAudio = connectAudioSwitch.isChecked();
                if(isCancelled){ //Scan in progress : a second click means that the user is trying to cancel the scan
                    cancelScan();
                }else{ // Scan is not in progress : starting a new scan in order to connect to a Mbt Device
                    startScan();
                }
                updateScanning(!isCancelled);

            }
        });
    }

    private void initDevicePrefix() {
        devicePrefixSpinner = findViewById(R.id.devicePrefix);
        ArrayList<String> prefixList = new ArrayList<>();
        prefixList.add(MELOMIND_DEVICE_NAME_PREFIX);
        prefixList.add(VPRO_DEVICE_NAME_PREFIX);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, prefixList);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        devicePrefixSpinner.setAdapter(arrayAdapter);
        devicePrefixSpinner.setSelection(arrayAdapter.getPosition(MELOMIND_DEVICE_NAME_PREFIX));
    }

    private void startScan() {
        client.connectBluetooth(new ConnectionConfig.Builder(bluetoothStateListener)
                .deviceName((deviceName != null) && (deviceName.equals(MELOMIND_DEVICE_NAME_PREFIX)) ?
                        null : deviceName )
                .maxScanDuration(SCAN_DURATION)
                .scanDeviceType(MbtDeviceType.MELOMIND)
                .connectAudioIfDeviceCompatible(connectAudio)
                .create());
    }

    private void cancelScan(){
        client.cancelConnection();
    }

    /**
     * Updates the scanning state boolean and the Scan button text
     * The Scan button text is changed into into "Cancel" if scanning is launched
     * or into "Find a device" if scanning is cancelled
     * @param newIsCancelled
     */
    private void updateScanning(boolean newIsCancelled){
        isCancelled = newIsCancelled;
        if(!isCancelled)
            toast.cancel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            scanButton.setBackgroundColor((isCancelled ? Color.LTGRAY : getColor(R.color.light_blue)));

        scanButton.setText((isCancelled ? R.string.cancel : R.string.scan));
    }

    /**
     * Returns true if the device name contains "melo_" or "vpro_", false otherwise
     * @return true if the device name contains "melo_" or "vpro_", false otherwise
     */
    private boolean isMbtDeviceName(){
        return isMelomindDevice() || isVproDevice();
    }

    private boolean isMelomindDevice(){
        return deviceName.startsWith(MbtFeatures.MELOMIND_DEVICE_NAME_PREFIX);
    }

    private boolean isVproDevice(){
        return deviceName.startsWith(MbtFeatures.VPRO_DEVICE_NAME_PREFIX);
    }

    private void notifyUser(String message){
        toast.setText("");
        toast.show();
        toast.setText(message);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        bluetoothStateListener = null;
    }


    private void deinitCurrentActivity(boolean isConnected){
        bluetoothStateListener = null;
        final Intent intent = new Intent(HomeActivity.this, DeviceActivity.class);
        intent.putExtra(DEVICE_NAME, deviceName);
        intent.putExtra(DEVICE_TYPE, MbtDeviceType.MELOMIND);
        startActivity(intent);
        finish();
    }
}
