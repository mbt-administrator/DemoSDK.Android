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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import core.bluetooth.BtState;
import config.ConnectionConfig;
import core.device.model.MbtDevice;
import engine.MbtClient;
import engine.SimpleRequestCallback;
import engine.clientevents.BaseError;

import engine.clientevents.BluetoothStateListener;

import static features.MbtFeatures.MELOMIND_DEVICE_NAME_PREFIX;
import static features.MbtFeatures.QR_CODE_NAME_PREFIX;

/**
 * First View displayed when you launch the application.
 * Headset Bluetooth connection is established here.
 */
public class HomeActivity extends AppCompatActivity{

    /**
     * Maximum duration allocated to find a headset
     */
    private final static int SCAN_DURATION = 20000;

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
     * Possible device name prefix values for {@link HomeActivity#deviceNamePrefixSpinner}
     */
    private ArrayList<String> prefixNameList;

    /**
     * Adapter that uses {@link HomeActivity#prefixNameList} to initialize the {@link HomeActivity#deviceNamePrefixSpinner}
     */
    private ArrayAdapter<String> prefixNameArrayAdapter;

    /**
     * Spinner used to select one of the possible Melomind device QR code prefixs
     */
    private Spinner deviceQrCodePrefixSpinner;

    /**
     * Device QR code prefix value stored from the value of the {@link HomeActivity#deviceQrCodePrefixSpinner}
     */
    private String deviceQrCodePrefix;

    /**
     * Possible QR code prefix values for deviceQrCodePrefixSpinner
     */
    private ArrayList<String> prefixQrCodeList;

    /**
     * Adapter that uses {@link HomeActivity#prefixQrCodeList} to initialize the {@link HomeActivity#deviceQrCodePrefixSpinner}
     */
    private ArrayAdapter<String> prefixQrCodeArrayAdapter;

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
    private Button scanButton;

    /**
     * Boolean value stored for Bluetooth connection cancel :
     * A Bluetooth connection in progress can be cancelled by the user within the {@link HomeActivity#SCAN_DURATION} duration by clicking on the {@link HomeActivity#scanButton}
     * If no Bluetooth connection is in progress, clicking on the {@link HomeActivity#scanButton} starts a Bluetooth connection
     */
    private boolean isCancelled = false;

    /**
     * Boolean value stored for Bluetooth connection error :
     * A Bluetooth connection in progress can be cancelled by the SDK if it returns an error
     */
    private boolean isErrorRaised = false;

    /**
     * Toast used to notify the user by displaying a temporary message on the foreground of the screen
     */
    private Toast toast;

    /**
     * Listener used to receive a notification when the Bluetooth connection state changes
     * If you just want to know when a headset is connected or disconnected,
     * you can replace the BluetoothStateListener listener with a ConnectionStateListener<BaseError> listener.
     */
    private BluetoothStateListener bluetoothStateListener = new BluetoothStateListener() {
        /**
         * Callback used to receive a notification when the Bluetooth connection state changes
         */
        @Override
        public void onNewState(BtState newState) {
            if(newState.equals(BtState.READING_SUCCESS)){
                sdkClient.requestCurrentConnectedDevice(new SimpleRequestCallback<MbtDevice>() {
                    @Override
                    public void onRequestComplete(final MbtDevice melomindDevice) {
                        if(melomindDevice != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDeviceName(melomindDevice);
                                    showDeviceQrCode(melomindDevice);
                                }
                            });
                        }
                    }
                });
            }
        }

        /**
         * Callback used to receive a notification when the Bluetooth connection is aborted if the SDK returns an error
         */
        @Override
        public void onError(BaseError error, String additionnalInfo) {
            isErrorRaised = true;
            updateScanning(false);
            toast = Toast.makeText(HomeActivity.this, error.getMessage()+ (additionnalInfo != null ? additionnalInfo : ""), Toast.LENGTH_LONG);
            toast.show();
        }

        /**
         * Callback used to receive a notification when the Bluetooth connection is established
         */
        @Override
        public void onDeviceConnected() {
            toast.cancel();
            deinitCurrentActivity();
        }

        /**
         * Callback used to receive a notification when a connected headset is disconnected
         */
        @Override
        public void onDeviceDisconnected() {
            if(!toast.getView().isShown())
                notifyUser(getString(R.string.no_connected_headset));
            if(isCancelled)
                updateScanning(false);
        }
    };

    /**
     * Method call to display the name of the connecting headset in the device name field
     */
    private void showDeviceName(final MbtDevice melomindDevice){
        deviceName = melomindDevice.getSerialNumber();
        String deviceNameToDisplay = deviceName.replace(MELOMIND_DEVICE_NAME_PREFIX,"");
        deviceNameField.setText(deviceNameToDisplay);
        for(String prefix : prefixNameList){
            if(melomindDevice.getSerialNumber() != null && melomindDevice.getProductName().startsWith(prefix))
                deviceNamePrefixSpinner.setSelection(prefixNameArrayAdapter.getPosition(prefix));
        }
    }

    /**
     * Method called to display the QR code of the connecting headset in the QR code field
     */
    private void showDeviceQrCode(final MbtDevice melomindDevice){
        deviceQrCode = melomindDevice.getExternalName();
        String deviceQrCodeToDisplay = deviceQrCode.replace(QR_CODE_NAME_PREFIX,"");
        deviceQrCodeField.setText(deviceQrCodeToDisplay);
        for(String prefix : prefixQrCodeList){
            if(melomindDevice.getExternalName() != null && melomindDevice.getExternalName().startsWith(prefix))
                deviceQrCodePrefixSpinner.setSelection(prefixQrCodeArrayAdapter.getPosition(prefix));
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

        toast = Toast.makeText(HomeActivity.this, "", Toast.LENGTH_LONG);
        sdkClient = MbtClient.init(getApplicationContext());
        isCancelled = false;

        if(getIntent().hasExtra(HomeActivity.PREVIOUS_ACTIVITY_EXTRA)){
            if(getIntent().getStringExtra(HomeActivity.PREVIOUS_ACTIVITY_EXTRA) != null)
                sdkClient.setConnectionStateListener(bluetoothStateListener);
        }

        initToolBar();
        initDeviceNameField();
        initDeviceNamePrefix();
        initDeviceQrCodeField();
        initDeviceQrCodePrefix();
        initConnectAudioSwitch();
        initScanButton();
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
    }

    /**
     * Method used to initialize the device name prefix spinner
     */
    private void initDeviceNamePrefix() {
        deviceNamePrefixSpinner = findViewById(R.id.deviceNamePrefix);
        prefixNameList = new ArrayList<>();
        prefixNameList.add(MELOMIND_DEVICE_NAME_PREFIX);
        prefixNameArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, prefixNameList);
        prefixNameArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        deviceNamePrefixSpinner.setAdapter(prefixNameArrayAdapter);
        deviceNamePrefixSpinner.setSelection(prefixNameArrayAdapter.getPosition(MELOMIND_DEVICE_NAME_PREFIX));
    }

    /**
     * Method used to initialize the device QR code field
     */
    private void initDeviceQrCodeField() {
        deviceQrCodeField = findViewById(R.id.deviceQrCodeField);
    }

    /**
     * Method used to initialize the device QR code prefix spinner
     */
    private void initDeviceQrCodePrefix() {
        deviceQrCodePrefixSpinner = findViewById(R.id.deviceQrCodePrefix);
        prefixQrCodeList = new ArrayList<>(Arrays.asList(QR_CODE_NAME_PREFIX));
        prefixQrCodeArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, prefixQrCodeList);
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
     * Method used to initialize the scan button
     */
    private void initScanButton(){
        scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyUser(getString(R.string.scan_in_progress));
                deviceNamePrefix = String.valueOf(deviceNamePrefixSpinner.getSelectedItem()); //get the prefix chosen by the user in the Spinner
                deviceName = deviceNamePrefix+deviceNameField.getText().toString(); //get the name entered by the user in the EditText

                deviceQrCodePrefix = String.valueOf(deviceQrCodePrefixSpinner.getSelectedItem()); //get the prefix chosen by the user in the Spinner
                deviceQrCode = deviceQrCodePrefix+deviceQrCodeField.getText().toString(); //get the name entered by the user in the EditText

                connectAudio = connectAudioSwitch.isChecked();

                if(isCancelled) //Scan in progress : a second click means that the user is trying to cancel the scan
                    cancelScan();

                else // Scan is not in progress : starting a new scan in order to connect to a Mbt Device
                    startScan();
                if(!isErrorRaised)
                    updateScanning(!isCancelled);

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
     * The SDK stops the scan after {@link HomeActivity#SCAN_DURATION} seconds if no headset is found
     */
    private void startScan() {
        isErrorRaised = false;
        ConnectionConfig.Builder connectionConfigBuilder = new ConnectionConfig.Builder(bluetoothStateListener)
                .deviceName((deviceName != null && (deviceName.equals(MELOMIND_DEVICE_NAME_PREFIX)) ? //if no name has been entered by the user, the default device name is the headset prefix
                        null : deviceName )) //null is given in parameters if no name has been entered by the user
                .deviceQrCode(((deviceQrCode != null) && (deviceQrCode.equals(QR_CODE_NAME_PREFIX)) ) ? //if no QR code has been entered by the user, the default device name is the headset prefix
                        null : deviceQrCode )
                .maxScanDuration(SCAN_DURATION);
        if(connectAudio)
            connectionConfigBuilder.connectAudio();

        sdkClient.connectBluetooth(connectionConfigBuilder.create());
    }

    /**
     * Method used to cancel a Bluetooth scan or connection in progress
     */
    private void cancelScan(){
        sdkClient.cancelConnection();
    }

    /**
     * Method used to update the scanning state boolean and the Scan button text
     * The Scan button text is changed into into "Cancel" if scanning is launched
     * or into "Find a device" if scanning is cancelled
     */
    private void updateScanning(boolean isCancelled){
        this.isCancelled = isCancelled;
        if(!isCancelled)
            toast.cancel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            scanButton.setBackgroundColor((isCancelled ? Color.LTGRAY : getColor(R.color.light_blue)));

        scanButton.setText((isCancelled ? R.string.cancel : R.string.scan));
    }

    /**
     * Method used to notify the user by showing a temporary message on the foreground
     * @param message is the temporary message to show
     */
    private void notifyUser(String message){
        toast.setText("");
        toast.show();
        toast.setText(message);
        toast.show();
    }

    /**
     * Method called by default when the Android device back buttton is clicked :
     * the listener is set to null to avoid memory leaks
     */
    @Override
    public void onBackPressed() {
        bluetoothStateListener = null;
    }

    /**
     * Method called when the {@link HomeActivity} is closed
     */
    private void deinitCurrentActivity(){
        bluetoothStateListener = null;
        final Intent intent = new Intent(HomeActivity.this, DeviceActivity.class);
        startActivity(intent);
        finish();
    }
}
