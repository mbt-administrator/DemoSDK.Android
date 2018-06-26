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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import core.bluetooth.BtState;
import engine.ConnectionConfig;
import engine.MbtClient;
import engine.clientevents.ConnectionException;
import engine.clientevents.ConnectionStateListener;
import features.MbtFeatures;

import static features.MbtFeatures.DEVICE_NAME_MAX_LENGTH;
import static features.MbtFeatures.MELOMIND_DEVICE_NAME_PREFIX;
import static features.MbtFeatures.VPRO_DEVICE_NAME_PREFIX;

public class HomeActivity extends AppCompatActivity{

    private static String TAG = HomeActivity.class.getName();
    private final static int SCAN_DURATION = 30000;
    public final static String DEVICE_NAME = "DEVICE_NAME";
    public final static String BT_STATE = "BT_STATE";

    private MbtClient client;

    private EditText deviceNameField;
    private String deviceName;

    private Spinner devicePrefixSpinner;
    private String devicePrefix;

    private Button scanButton;

    private boolean isScanning = false;

    private Toast toast;

    private ConnectionStateListener connectionStateListener = new ConnectionStateListener<ConnectionException>() {
        @Override
        public void onStateChanged(@NonNull BtState newState) {
            Log.i(TAG, "Current state updated "+newState);
            //if(isScanning){
            if (newState.equals(BtState.CONNECTING) ) {
                notifyUser("Connecting to ' " + deviceName +" '");
            }if (newState.equals(BtState.CONNECTED) ) {
                notifyUser("Device ' " + deviceName + " ' connected but not ready. Please be patient");
            }else if (newState.equals(BtState.CONNECTED_AND_READY) ){
                notifyUser("Device ' " + deviceName + " ' connected");
                deinitCurrentActivity(newState);
            }else if (newState.equals(BtState.SCAN_TIMEOUT)||(newState.equals(BtState.CONNECT_FAILURE))){
                notifyUser(getString(R.string.connect_failed) + " "+deviceName);
                updateScanning(false);
            }else if (newState.equals(BtState.SCAN_STARTED)){
                notifyUser(getString(R.string.connect_in_progress));
                updateScanning(true);
            }else if (newState.equals(BtState.DISCONNECTED)){
                notifyUser(getString(R.string.disconnected_headset));
                updateScanning(false);
            }else if (newState.equals(BtState.INTERRUPTED))
                updateScanning(false);
            //}
        }

        @Override
        public void onError(ConnectionException exception) {
            notifyUser(exception.toString());
            updateScanning(false);
            exception.printStackTrace();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initToolBar();
        toast= Toast.makeText(HomeActivity.this, "", Toast.LENGTH_SHORT);
        client = MbtClient.init(getApplicationContext());


        initDeviceNameField();
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

    private void initScanButton(){
        scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devicePrefix = String.valueOf(devicePrefixSpinner.getSelectedItem()); //get the prefix chosed by the user in the Spinner
                deviceName = devicePrefix+deviceNameField.getText().toString(); //get the name entered by the user in the EditText
                if(isScanning){ //Scan in progress : a second click means that the user is trying to cancel the scan
                    cancelScan();
                }else{ // Scan is not in progress : starting a new scan in order to connect to a Mbt Device
                    startScan();
                }
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

        if(deviceName.equals(MELOMIND_DEVICE_NAME_PREFIX) || deviceName.equals(VPRO_DEVICE_NAME_PREFIX) ){ //no name entered by the user
            //findAvailableDevice();
            notifyUser("Please enter the name of the device");
        }else{ //the user entered a name
            if( isMbtDeviceName() && deviceName.length() == DEVICE_NAME_MAX_LENGTH ) { //check the device name format
                client.connectBluetooth(new ConnectionConfig.Builder(connectionStateListener).deviceName(deviceName).maxScanDuration(SCAN_DURATION).create());
            }else{ //if the device name entered by the user is empty or is not starting with a mbt prefix
                notifyUser(getString(R.string.wrong_device_name));
            }
        }

    }

    private void cancelScan(){
        client.cancelConnection();
    }

    /**
     * Updates the scanning state boolean and the Scan button text
     * The Scan button text is changed into into "Cancel" if scanning is launched
     * or into "Find a device" if scanning is cancelled
     * @param newIsScanning
     */
    private void updateScanning(boolean newIsScanning){
        isScanning = newIsScanning;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scanButton.setBackgroundColor((isScanning ? Color.LTGRAY : getColor(R.color.light_blue)));
        }
        scanButton.setText((isScanning ? R.string.cancel : R.string.scan));
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
        this.connectionStateListener = null;
    }


    private void deinitCurrentActivity(BtState newState){
        connectionStateListener = null;
        final Intent intent = new Intent(HomeActivity.this, DeviceActivity.class);
        intent.putExtra(DEVICE_NAME, deviceName);
        intent.putExtra(BT_STATE, newState);
        startActivity(intent);
        finish();
    }
}
