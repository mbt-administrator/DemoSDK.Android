package sdkdemo.com.mybraintech.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import core.bluetooth.BtState;
import config.ConnectionConfig;
import core.device.model.MbtDevice;
import core.device.model.MelomindDevice;
import core.device.model.VProDevice;
import engine.MbtClient;
import engine.SimpleRequestCallback;
import engine.clientevents.BaseError;

import engine.clientevents.BasicError;
import engine.clientevents.BluetoothStateListener;
import engine.clientevents.ConfigError;
import features.MbtDeviceType;


import static features.MbtFeatures.MELOMIND_DEVICE_NAME_PREFIX;
import static features.MbtFeatures.QR_CODE_NAME_PREFIX;
import static features.MbtFeatures.VPRO_DEVICE_NAME_PREFIX;

/**
 * First View displayed when you launch the application.
 * Headset Bluetooth connection is established here.
 */
public class HomeActivity extends AppCompatActivity{

    /**
     * Maximum duration allocated to find a headset
     */
    private final static int MAXIMUM_SCAN_DURATION = 20000;

    /**
     * Extra key used to share data to the next started activity
     */
    public final static String PREVIOUS_ACTIVITY_EXTRA = "PREVIOUS_ACTIVITY_EXTRA";

    /**
     * Instance of SDK client used to access all the SDK features
     */
    private MbtClient sdkClient;

    /**
     * Device name field used to enter a specific headset name on the application for Bluetooth connection
     */
    private EditText deviceNameField;

    /**
     * Device name value stored from the value of the {@link HomeActivity#deviceNameField}
     */
    private String deviceName;

    /**
     * Device QR code field used to enter a specific headset QR code on the application for Bluetooth connection
     */
    private EditText deviceQrCodeField;

    /**
     * Device QR code value stored from the value of the {@link HomeActivity#deviceQrCodeField}
     */
    private String deviceQrCode;

    /**
     * Spinner used to select one of the possible Melomind device name prefixs
     */
    private Spinner deviceNamePrefixSpinner;

    /**
     * Device name prefix value stored from the value of the {@link HomeActivity#deviceNamePrefixSpinner}
     */
    private String deviceNamePrefix;

    /**
     * Spinner used to select one of the possible Melomind device QR code prefixs
     */
    private Spinner deviceQrCodePrefixSpinner;

    /**
     * Device QR code prefix value stored from the value of the {@link HomeActivity#deviceQrCodePrefixSpinner}
     */
    private String deviceQrCodePrefix;

    /**
     * Switch used to enable or disable Bluetooth audio connection.
     */
    private Switch connectAudioSwitch;

    /**
     * Boolean value stored from the value of the {@link HomeActivity#connectAudioSwitch} :
     * Audio Bluetooth connection is enabled if {@link HomeActivity#connectAudio} is true.
     * Audio Bluetooth connection is disabled if {@link HomeActivity#connectAudio} is false.
     */
    private boolean connectAudio = false;

    /**
     * Button used to initiate the Bluetooth connection with a Melomind headset on click
     */
    private Button connectButton;

    /**
     * Boolean value stored for Bluetooth connection cancel :
     * A Bluetooth connection in progress can be cancelled by the user within the {@link HomeActivity#MAXIMUM_SCAN_DURATION} duration by clicking on the {@link HomeActivity#connectButton}
     * If no Bluetooth connection is in progress, clicking on the {@link HomeActivity#connectButton} starts a Bluetooth connection
     */
    private boolean isCancel = false;

    /**
     * Boolean value stored for Bluetooth connection error management:
     * A Bluetooth connection in progress can be cancelled by the SDK if it returns an error
     */
    private boolean hasError = false;

    /**
     * Toast used to notify the user by displaying a temporary message on the foreground of the screen
     */
    private Toast toast;

    /**
     * Listener used to receive a notification when the Bluetooth connection state changes.
     * If you just want to know when a headset is connected or disconnected,
     * you can replace the {@link BluetoothStateListener} listener with a {@link engine.clientevents.ConnectionStateListener} listener.
     */
    private BluetoothStateListener bluetoothStateListener = new BluetoothStateListener() {
        /**
         * Callback used to receive a notification when the Bluetooth connection state changes.
         * If a device is connecting (or is connected), its data are bundled in the {@link MbtDevice} device object.
         */
        @Override
        public void onNewState(BtState newState, MbtDevice device) {
            if(newState.equals(BtState.READING_SUCCESS)){
                sdkClient.requestCurrentConnectedDevice(new SimpleRequestCallback<MbtDevice>() {
                    @Override
                    public void onRequestComplete(MbtDevice device) {
                        showDeviceName(device);
                        showDeviceQrCode(device);
                    }
                });

            }
        }

        /**
         * Callback used to receive a notification when the Bluetooth connection is aborted if the SDK returns an error
         */
        @Override
        public void onError(BaseError error, String additionalInfo) {
            hasError = true;
            updateView(false);
            toast = Toast.makeText(HomeActivity.this, error.getMessage()+ (additionalInfo != null ? additionalInfo : ""), Toast.LENGTH_LONG);
            toast.show();
        }

        /**
         * Callback used to receive a notification when the Bluetooth connection is established
         * The connected device data are bundled in the {@link MbtDevice} device object.
         */
        @Override
        public void onDeviceConnected(MbtDevice connectedDevice) {
            toast.cancel();
            closeCurrentActivity();
        }

        /**
         * Callback used to receive a notification when a connected headset is disconnected
         * The disconnected device data are bundled in the {@link MbtDevice} device object.
         */
        @Override
        public void onDeviceDisconnected(MbtDevice disconnectedDevice) {
            if(!toast.getView().isShown())
                notifyUser(getString(R.string.no_connected_headset));
            if(isCancel)
                updateView(false);
        }
    };

    /**
     * Method call to display the name of the connecting headset in the device name field
     */
    private void showDeviceName(final MbtDevice device){
        deviceName = device.getSerialNumber();

        if(deviceName != null) {
            if (device instanceof MelomindDevice)
                deviceNameField.setText(deviceName.replace(MELOMIND_DEVICE_NAME_PREFIX, ""));//a device is found: its name is displayed in the device name edit text
            else if (device instanceof VProDevice)
                deviceNameField.setText(deviceName.replace(VPRO_DEVICE_NAME_PREFIX, ""));//a device is found: its name is displayed in the device name edit text
        }
    }

    /**
     * Method called to display the QR code of the connecting headset in the QR code field
     */
    private void showDeviceQrCode(final MbtDevice device){
        deviceQrCode = device.getExternalName();

        if(deviceQrCode != null){
            String deviceQrCodeToDisplay = deviceQrCode.replace(QR_CODE_NAME_PREFIX,"");
            deviceQrCodeField.setText(deviceQrCodeToDisplay);
        }
    }

    /**
     * Method called by default when the Activity is started
     * It initializes all the views, SDK client, and permissions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(MbtClient.getClientInstance() == null)
            MbtClient.init(getApplicationContext()); //initialize the SDK

        sdkClient = MbtClient.getClientInstance();

        if(getIntent().hasExtra(HomeActivity.PREVIOUS_ACTIVITY_EXTRA)){ //if the home activity is opened from an other activity, we need to register the bluetooth listener defined in this activity in order to catch connection and disconnection events
            if(getIntent().getStringExtra(HomeActivity.PREVIOUS_ACTIVITY_EXTRA) != null)
                sdkClient.setConnectionStateListener(bluetoothStateListener);
        }

        toast = Toast.makeText(HomeActivity.this, "", Toast.LENGTH_LONG); //toast initialized to be shown later if an error is raised
        isCancel = false;

        //initialize the view elements
        initToolBar();
        initDeviceNameField();
        initDeviceQrCodeField();
        initConnectAudioSwitch();
        initConnectButton();
        initPermissions();
    }

    /**
     * Method used to initialize the top tool bar view
     */
    private void initToolBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.light_blue)));
        }
    }

    /**
     * Method used to initialize the device name field
     */
    private void initDeviceNameField() {
        deviceNameField = findViewById(R.id.deviceNameField);
        initDeviceNamePrefix();
    }

    /**
     * Method used to initialize the device name prefix spinner
     */
    private void initDeviceNamePrefix() {
        deviceNamePrefixSpinner = findViewById(R.id.deviceNamePrefix);

        ArrayList<String> deviceNamePrefixList = new ArrayList<>(Arrays.asList(//Possible device name prefix values for {@link HomeActivity#deviceNamePrefixSpinner
                MELOMIND_DEVICE_NAME_PREFIX,
                VPRO_DEVICE_NAME_PREFIX));

        //Adapter that uses {@link HomeActivity#deviceNamePrefixList} to initialize the {@link HomeActivity#deviceNamePrefixSpinner}
        ArrayAdapter<String> deviceNamePrefixArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceNamePrefixList);
        deviceNamePrefixArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        deviceNamePrefixSpinner.setAdapter(deviceNamePrefixArrayAdapter);
        deviceNamePrefixSpinner.setSelection(deviceNamePrefixArrayAdapter.getPosition(MELOMIND_DEVICE_NAME_PREFIX));
        deviceNamePrefixSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(deviceNamePrefixSpinner.getSelectedItem().equals(VPRO_DEVICE_NAME_PREFIX)) {
                    deviceQrCodeField.setEnabled(false);
                    deviceQrCodePrefixSpinner.setEnabled(false);
                    connectAudioSwitch.setEnabled(false);
                }else if (deviceNamePrefixSpinner.getSelectedItem().equals(MELOMIND_DEVICE_NAME_PREFIX)){
                    deviceQrCodeField.setEnabled(true);
                    deviceQrCodePrefixSpinner.setEnabled(true);
                    connectAudioSwitch.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    /**
     * Method used to initialize the device QR code field
     */
    private void initDeviceQrCodeField() {
        deviceQrCodeField = findViewById(R.id.deviceQrCodeField);
        initDeviceQrCodePrefix();
    }

    /**
     * Method used to initialize the device QR code prefix spinner
     */
    private void initDeviceQrCodePrefix() {
        deviceQrCodePrefixSpinner = findViewById(R.id.deviceQrCodePrefix);

        ArrayList<String> deviceQrCodePrefixList = new ArrayList<>(Collections.singletonList(QR_CODE_NAME_PREFIX));//Possible QR code prefix values for deviceQrCodePrefixSpinner

        //Adapter that uses {@link HomeActivity#deviceQrCodePrefixList} to initialize the {@link HomeActivity#deviceQrCodePrefixSpinner}
        ArrayAdapter<String> prefixQrCodeArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceQrCodePrefixList);
        prefixQrCodeArrayAdapter.setDropDownViewResource(R.layout.spinner_item);

        deviceQrCodePrefixSpinner.setAdapter(prefixQrCodeArrayAdapter);
        deviceQrCodePrefixSpinner.setSelection(prefixQrCodeArrayAdapter.getPosition(QR_CODE_NAME_PREFIX));
    }

    /**
     * Method used to initialize the audio connection switch
     */
    private void initConnectAudioSwitch() {
        connectAudioSwitch = findViewById(R.id.connectAudio);
    }

    /**
     * Method used to initialize the connect button
     */
    private void initConnectButton(){
        connectButton = findViewById(R.id.connectButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyUser(getString(R.string.scan_in_progress));

                deviceNamePrefix = String.valueOf(deviceNamePrefixSpinner.getSelectedItem()); //get the prefix chosen by the user in the Spinner
                deviceName = deviceNamePrefix+deviceNameField.getText().toString(); //get the name entered by the user in the EditText

                deviceQrCodePrefix = String.valueOf(deviceQrCodePrefixSpinner.getSelectedItem()); //get the prefix chosen by the user in the Spinner
                deviceQrCode = deviceQrCodePrefix+deviceQrCodeField.getText().toString(); //get the name entered by the user in the EditText

                connectAudio = connectAudioSwitch.isChecked();

                if(isCancel) //Connect in progress : a second click means that the user is trying to cancel the connect
                    cancelConnection();
                else // Connection is not in progress : starting a new scan in order to find & connect to a Mbt Device
                    startConnection();

                if(!hasError)
                    updateView(!isCancel);

            }
        });
    }

    /**
     * Method used to initialize the required application permissions :
     * A system popup appears on the foreground if the permissions are not granted
     * /!\ Bluetooth Low Energy requires Location permission to find an available device
     */
    private void initPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if(!hasPermissions(getApplicationContext(), PERMISSIONS))
            ActivityCompat.requestPermissions(HomeActivity.this, PERMISSIONS, PERMISSION_ALL);
    }

    /**
     * Method used to check if required permissions are granted :
     * it returns true if permissions are granted, false otherwise
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Method used to start a Bluetooth scan if order to find an available headset and initiate connection
     * If a device name is entered, the SDK connects the corresponding headset.
     * If no device name is entered, the SDK connects the first found available headset.
     * The SDK stops the scan after {@link HomeActivity#MAXIMUM_SCAN_DURATION} seconds if no headset is found
     */
    private void startConnection() {
        hasError = false;

        ConnectionConfig.Builder builder = new ConnectionConfig.Builder(bluetoothStateListener)
                .deviceName(
                        ((deviceName != null) && (deviceName.equals(MELOMIND_DEVICE_NAME_PREFIX) || deviceName.equals(VPRO_DEVICE_NAME_PREFIX))) ? //if no name has been entered by the user, the default device name is the headset prefix
                        null : deviceName ) //null is given in parameters if no name has been entered by the user
                .deviceQrCode(
                        ((deviceQrCode != null) && (deviceQrCode.equals(QR_CODE_NAME_PREFIX)) ) ? //if no QR code has been entered by the user, the default device name is the headset prefix
                        null : deviceQrCode )
                .maxScanDuration(MAXIMUM_SCAN_DURATION);

        if(connectAudio)
            builder.connectAudio();

        MbtDeviceType deviceType = null;
        if(deviceName.startsWith(MELOMIND_DEVICE_NAME_PREFIX))
            deviceType = MbtDeviceType.MELOMIND;
        if(deviceName.startsWith(VPRO_DEVICE_NAME_PREFIX))
            deviceType = MbtDeviceType.VPRO;

        if(deviceType != null)
            sdkClient.connectBluetooth(builder.createForDevice(deviceType));
        else
            notifyUser(ConfigError.ERROR_INVALID_PARAMS.getMessage()+ getString(R.string.error_unknown_device));
    }

    /**
     * Method used to cancel a Bluetooth connection in progress
     */
    private void cancelConnection(){
        sdkClient.cancelConnection();
    }

    /**
     * Method used to update the connecting state boolean and the Connection button text
     * The Connection button text is changed into into "Cancel" if connection is launched
     * or into "Find a device" if connection is cancelled
     */
    private void updateView(boolean isCancel){
        this.isCancel = isCancel;

        if(!this.isCancel)
            toast.cancel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            connectButton.setBackgroundColor((this.isCancel ?
                    Color.LTGRAY : getColor(R.color.light_blue)));

        connectButton.setText((this.isCancel ?
                R.string.cancel : R.string.find_device));
    }

    /**
     * Method used to notify the user by showing a temporary message on the foreground
     * @param message is the temporary message to show
     */
    private void notifyUser(String message){
        showToast("");//clear the toast
        showToast(message);//display a message
    }

    private void showToast(String message){
        toast.setText(message);
        toast.show();
    }

    /**
     * Method called when the {@link HomeActivity} is closed
     */
    private void closeCurrentActivity(){
        bluetoothStateListener = null;
        sdkClient.setConnectionStateListener(null);
        final Intent intent = new Intent(HomeActivity.this, DeviceActivity.class);
        startActivity(intent);
        finish();
    }
}
