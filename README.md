



# USER GUIDE

# How to install and use the myBrain Technologies’ SDK in your projects 

------



## I. Overview 

------

This document aims at helping you to install and use the My Brain Technologies’ SDK in your own mobile project. It is arranged in a way to guide you through the various steps in your development process. We recommend following each of the steps outlined below and reading the documentation in the order displayed.

**What is the myBrain Technologies’ SDK ?** 
The myBrain Technologies’ SDK is a closed source library developed by My Brain Technologies. Its main purpose is to allow the development of external Android applications connected to myBrain Technologies’ electroencephalography headsets.  It also provides code examples, and technical and use case documentation for developers.

**What is electroencephalography  ?** 
Electroencephalography (also known as EEG) is a technique used to measure the electrical activity in the brain. Brain cells communicate with each other through electrical impulses that fluctuate rhythmically in distinct patterns.

**What are the myBrain Technologies’ headsets ?**
My Brain Technologies’ headsets includes Melomind and VPro headsets:

- Melomind is an audio headset composed of 2 electrodes that record electrical activity of the brain. These 2 channels of EEG acquisition are located on P3 and P4 parietal position.
- VPro is a headset composed of 8 electrodes that record electrical activity of the brain. These channels of EEG acquisition can be located at customized position of the brain

This library is currently distributed as a .aar file. Its content is obfuscated. Only the public content is accessible to external applications. 

 To this day, this document and the associated SDK are available for Android platform. An iOS version will be soon available.

## II.  Versions

------

The current version of the SDK is 2.0.5.1 Further updates will be released in the following months with more features. 

Using the My Brain Technologies’ SDK requires to install an IDE for developing Android applications. 
*Note : this document explains how to install the SDK on Android Studio IDE only.*

The myBrain Technologies’ SDK is compiled with Android API version 28 and is compatible with Android API version 22 and higher. Older versions will not work.  Please adjust your application’s minimum API version accordingly. 

The minimum Bluetooth version of the Android device (smartphone or tablet) required to connect to a Melomind headset is the 4.0 version. Any higher version is compatible and any lower version is incompatible.

## III. Features

------

The main features offered by the SDK are listed below. 

### Bluetooth

- Bluetooth connection with a Melomind headset
- Bluetooth data transfer from the headset to the SDK
- Bluetooth command & data transfer from the SDK to the headset*

### EEG

- Real time EEG raw data streaming
- Signal quality computation*
- Muscular artefacts detection*
- DC Offset & Saturation measurement*
- Relaxation index computation*


### Device 

 Configuration of the following customizable parameters for the Melomind headset :
- Maximum Transmission Unit (maximum size of the data sent by the headset to the SDK)
- Notch filter* 
- Gain* 
- P300* 
- DC offset*
- Saturation* 

### Synchronisation
- External triggers synchronisation*
- OSC data streaming to a PC or Mac*

## IV.  Tutorial

------

This tutorial contains instructions on how to create your own project, install the SDK and use its features. It assumes that you are using the Android Studio development environment and a supported Android device.

### 1.	Requirements

##### Create a new Android Studio Project

- Open the Android Studio IDE.
- On the top bar, click on File, select New, then New Project... 
- On the Create New Project dialog, enter your Application name, your Company domain, your project location in the file system, your package name and click on Next button
- Select “Phone and Tablet” in the form factors and select the lowest version of the Android SDK you wish to support and click on Next. Here you should choose API 22 : Android 5.1. (Lollipop).
- add an Empty Activity and click on Next.
- Enter your activity name (for example “MainActivity”) and your layout name. (For example “activity_main”) then click on Next and Finish.

##### Request the mandatory permissions

The SDK relies on Bluetooth Low Energy scanner. From Android 6, the Bluetooth is considered as a location technology so it is necessary to request the location permission for using Bluetooth features. To do so, add the following code :

```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

To do so, you must request permission when you want the user to connect to a remote Bluetooth device. 


### 2. How to install the SDK 

Inside the *build.gradle* file located at the **root** of your project folder, add the following code :

```
repositories{
    maven{
        url 'https://package.mybraintech.com/repository/maven-public/'
        credentials {
            username 'sdk'
            password 'MBTSDK2019'
        }
    }
}
```

Inside the *build.gradle* file located in the **app** folder of your project, make sure that the following versions are matching :

```
android {
    compileSdkVersion 28
    defaultConfig {
            minSdkVersion 22
            targetSdkVersion 28
    }
}
```

Add the following dependency to your dependencies list. If the `2.0.5` version is not the last available version, replace `2.0.5` with the last version of the SDK:

```
implementation 'mybraintech.com:sdk-lite:2.0.5.1'
implementation 'mybraintech.com:sdk-lite:2.0.5.1:javadoc'
implementation 'com.android.support:appcompat-v7:28.0.0'
```

Build with ./gradlew build command or “Sync now” option on Android Studio.
That’s it, the SDK should be available.

### 3. How to use the SDK 

##### Communication between SDK & application

Once the SDK is installed on the application project, you can use its features for developing your own application. 

The SDK communicates with the application through a client: the `MbtClient`.

The first step is to create an instance of the `MbtClient` Object and initialize this client inside the `OnCreate()` method of the main activity of the application.

```
private MbtClient sdkClient;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    sdkClient = MbtClient.init(getApplicationContext());
}
```

_Note: If you forget to initialize a `MbtClient` instance, you won’t have access to the SDK features. This initialization can also be done in the `onCreate()` method of your Application subclass._

To use your client inside several Activities, you can call the following method on the other Activities:

```
sdkClient = MbtClient.getClientInstance();
```

You just need to be sure that you have called 
`MbtClient.init()` at least once. This method do not change the value of the client instance if it has already been initialized.

The application is now set up to interact with the SDK features.

##### Bluetooth Features

The Bluetooth communication between the headset and the application is managed through the use of the following features. It allows Bluetooth devices scanning, connection, disconnection, reading data transmitted by the headset and EEG data streaming.

###### CONNECTION

To connect a headset, you need to call the following method:

```
sdkClient.connectBluetooth(connectionConfig);
```

**Parameters** 

> `connectionConfig` is the connection configuration Object. It provides some options to specify the connection parameters and registers a listener that will notify you when the headset is connected or disconnected. Use the `ConnectionConfig.Builder` to create an instance.

Before calling this method, you need to create a non null instance of a `BluetoothStateListener` or `ConnectionStateListener` Object. 

The `ConnectionStateListener`Object provides 3 callbacks :

```
ConnectionStateListener connectionStateListener = new ConnectionStateListener(){ 

@Override 
public void onDeviceConnected() {}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}
```

The `onDeviceConnected` callback notifies you every time a new Bluetooth connection is established with a headset.

The `onDeviceDisconnected` callback notifies you every time a new Bluetooth connection is interrupted with a headset. This callback can be called in several cases :

- You requested disconnection by calling the SDK client`disconnectBluetooth` method
- The connection is lost because
  - the headset has been turned off while a headset was connected
  - the Bluetooth on the mobile device has been disabled while a headset was connected
  - the headset is too far from the connected mobile device
  - the headset encountered a problem

The `onError` callback notifies you in case a Bluetooth connection has failed and returns information about the origin of the failure. All error cases are listed in the Appendix (cf Errors).

The `BluetoothStateListener` Object provided 4 callbacks :

```
BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}
} 
```

The `onDeviceConnected`, `onDeviceDisconnected` and `onError` callbacks are common with the  `ConnectionStateListener` callbacks (cf description above).

Contrary to the `ConnectionStateListener` , the `BluetoothStateListener` has an additional `onNewState` callback. Its role is to notify you every time the Bluetooth connection state change. In other words, it returns the current state for each step of the connection process. To better understand the value and the meaning of the current state, a list of states is available in the Appendix (cf Bluetooth States). 

By default, the current state is `BtState.IDLE`.

 The `onError` callback is fired if an error that affects the transfer of the current connection state. All error cases are listed in the Appendix (cf Errors).

Then you need to initialize the connection configuration by creating an instance of the `ConnectionConfig` Object. This instance must be initialized by calling the `ConnectionConfig` Builder whose only mandatory parameter is a non null instance of a`ConnectionStateListener` or `BluetoothStateListener` Object.

```
ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener).create();

 or 
 
 ConnectionConfig connectionConfig = new ConnectionConfig.Builder(connectionStateListener).create();
```

 Moreover, you can specify some optional parameters :

- `deviceName(String deviceName)` : the parameter is a `String` Object that contains the name of the headset to connect. A Melomind name always starts with `melo_` followed by 10 digits. If you do not specify `deviceName` or set the`deviceName` parameter to `null` in your `ConnectionConfig` builder, the SDK will connect the first available Melomind found no matter its name. 

- `deviceQrCode(String deviceQrCode)` : the parameter is a `String` Object that contains the QR code identifier of the headset to connect. A Melomind QR code always starts with `MM` followed by 7 or 8 digits. If you do not specify `deviceQrCode` neither `deviceName`, or set the`deviceQrCode` and `deviceName` parameters to `null` in your `ConnectionConfig` builder, the SDK will connect the first available Melomind found. If you want to connect a specific headset, you can specify only `deviceQrCode` or only`deviceName`. As every QR code and name are unique, if you do specify `deviceQrCode` and`deviceName` in your `ConnectionConfig` builder, the SDK will check if the QR code matches the name. If they don't match, an error is returned on the `onError()` callback. 

- `maxScanDuration(long durationInMillis)` : the maximum scanning duration is the time within the SDK is allowed to look for an available headset to connect. The scan is stopped if the connection operation time out and no headset is found within the permitted time. A minimum value of **10000** milliseconds is mandatory, and there is no maximum value. If you do not specify `maxScanDuration`in your `ConnectionConfig` builder, the maximum duration is set to 30000 milliseconds.

  *Note: This value is in milliseconds.*

- `connectAudio()`:  as the Melomind headset is an audio headset, you can enable Bluetooth connection for audio streaming by specifying `useAudio`. If you do not specify `connectAudio`, the headset is only connected for data streaming and no audio stream can be broadcasted to it.  

  *Note : This option must be disabled if you connect the headset with a Jack cable. To disable it, do not specify `connectAudio` in your `StreamConfig` builder.* 

Here is a full example of connection to a Melomind headset whose name is `melo_0123456789`, with audio stream option enabled :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

} 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);
```

###### DISCONNECTION

To end the current connection with a connected headset, you need to call the following method:

```
sdkClient.disconnectBluetooth()
```

This method ends the current Bluetooth connection with a connected headset so that no more communication is stopped between the headset and the application. If a data streaming is in progress, it stops transmitting the notifications that return the acquired EEG data of the headset to the application. If audio is connected in Bluetooth, the method also disconnects the audio stream.

*Tip: To check that a Melomind headset has been correctly disconnected, you can take a look at the LED located next to the ON/OFF button. If the LED is emitting a flashing blue light, it means that the headset has been disconnected. If the LED is emitting a blue light but is not flashing, it means that the headset is still connected.*

*Note : You should not call this method if the application is not connected to a headset.*

##### EEG Features

The EEG data acquisition and signal processing are managed through the use of the EEG features. It allows starting and stopping EEG data streaming.

###### STARTING AN EEG STREAM

For starting the EEG acquisition, you needs to call the following method:

```
sdkClient.startStream(streamConfig)
```

**Parameters**

> `streamConfig` is the streaming configuration Object.  It provides some methods to specify the streaming parameters and registers a listener that will notify you when the EEG data are received. Use the `StreamConfig.Builder` to create the instance.

This method sends a request to the headset to get the EEG data measured by the headset. The headset response triggers the `onNewPackets` callback, that returns the EEG data in the `mbtEEGPackets` variable. EEG data are sent until the `sdkClient.stopStream()` method is called. By default, the period (time interval) of notification is **1** second. It means that EEG data will be sent every second. You can choose the value of this period when you initialize the streaming configuration (see below).

You can handle the EEG data received and use them according to their specific needs. For example, you can display the EEG data values as a text, or plot them in a chart, or save them in a database.

Before calling this method, you need to create a non null instance of a `EegListener` Object. This listener will notify you when the EEG data are received. The `EegListener` implements 2 methods :

```
EegListener<BaseError> eegListener = new EegListener<BaseError>() {
            @Override
            public void onError(BaseError error, String additionnalInfo) {}

            @Override
            public void onNewPackets(@NonNull final MbtEEGPacket mbtEEGPackets) {}
                
        };
```

 The `onError` callback will be fired if an error that affects the transfer of EEG data occurs. The different error cases are listed in the Appendix (cf Errors). 

The `onNewPackets` callback returns the EEG data acquired during a period (1 second by default) in the `mbtEEGPackets` variable. The `MbtEEGPacket` Object is explained below (cf UNDERSTAND THE EEG DATA)

Then you need to create a non null instance of the `StreamConfig` Object. This instance must be initialized by calling the `StreamConfig` Builder whose only mandatory parameter is a non null instance of a `EegListener`Object. 

```
     StreamConfig streamConfig = new StreamConfig.Builder(eegListener).create();
```

Moreover, you can specify some optional parameters :

- `useQualities()` * : the SDK computes the signal quality from the raw EEG signal acquired for every acquisition channel if you specify `computesQualities` . This computation is performed asynchronously in order not to congest the EEG flow. The computed qualities are associated to the raw EEG data in the `MbtEEGPacket` Object returned by the `onNewPackets` callback, and can be retrieved by calling `mbtEEGPackets.getQualities()`. This getter returns a list whose size is equal to the number of acquisition channels of the headset. For example, the Melomind headset has 2 channels, so the first value corresponds to the first channel (brain position P3) and the second value corresponds to the second channel (brain position P4). The signal quality is not computed if you do not specify `useQualities` in your `StreamConfig` builder. 
- `setNotificationPeriod(int periodInMillis) ` : the SDK sends the acquired EEG data to the SDK every second by default. You can increase or decrease this notification period by setting the value of your choice to the `periodInMillis` parameter. This option can be useful to improve the accuracy of a real time acquisition. The minimum value is 200 milliseconds if the signal quality is not computed, and there is no maximum value. A notification period of 1000 milliseconds is required to compute the signal quality. If you do not specify any `setNotificationPeriod` in your `StreamConfig` builder, the default period (1 second) is used.

*Note : This value is in milliseconds.*

- `configureAcquisitionFromDeviceCommand(DeviceStreamingCommands... deviceCommands)` *: you can send commands to change the headset device notch filter, gain, MTU, or enable DC Offset and Triggers detection before starting the stream. You can send one or more commands by separating them with a comma in the method parameters. If you do not specify any `configureAcquisitionFromDeviceCommand` in your `StreamConfig` builder, the default filter, gain and MTU are used and DC offset, saturation and triggers are not detected. 

Here is a full example of streaming EEG data when a headset is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

EegListener<BaseError> eegListener = new EegListener<BaseError>() {
            @Override
            public void onError(BaseError error, String additionnalInfo) {}

            @Override
            public void onNewPackets(@NonNull final MbtEEGPacket mbtEEGPackets) {}
                
        };
        
StreamConfig streamConfig = new StreamConfig.Builder(eegListener)
.setNotificationPeriod(20000)
.useQualities()
.configureAcquisitionFromDeviceCommand(
    new DeviceStreamingCommands.Mtu(47),
    new DeviceStreamingCommands.DcOffset(true),
    new DeviceStreamingCommands.AmplifierGain(AmpGainConfig.AMP_GAIN_X12_DEFAULT),
    new DeviceStreamingCommands.NotchFilter(FilterConfig.NOTCH_FILTER_50HZ)
    new DeviceStreamingCommands.Triggers(true))
.create();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
    sdkClient.startStream(streamConfig);
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```

###### UNDERSTANDING THE EEG DATA

The EEG data are returned as a `MbtEEGPacket` Object that contains a matrix of EEG data acquired during a time interval equals to the notification period. Each column of the matrix contains all the EEG data values acquired by one channel during the whole period. 

*Note : Unit is microvolt.*

 Considering that all the channels are sending the same number of data, all the columns must have the same number of item. The number of item is equal to the sampling rate x the notification period.  Each line of the matrix contains the acquired EEG data by all the channels at a specific moment. 

For example, one line contains 2 EEG data values with a Melomind headset as it has 2 channels of acquisition /electrodes. As for a Vpro headset, one line contains 8 EEG data values.

To get the matrix of EEG data, you need to call the following getter :

```
mbtEEGPackets.getChannelsData()
```

To get the number of line of the matrix / the number of EEG data acquired by one channel during the whole period, you need to add the following code :

```
mbtEEGPackets.getChannelsData().size()
```

To get the number of column of the matrix / the number of EEG data acquired by all the channels at a specific moment, you need to add the following code :

```
mbtEEGPackets.getChannelsData().get(0).size()
```

As each line has the same number of EEG data, you can also call :

```
mbtEEGPackets.getChannelsData().get(1).size()
```

It is possible to get the same matrix where the lines and columns are inverted (where each  column contains the EEG data acquired by all the channels at a specific moment and the lines contains the EEG data values acquired by one channel during the whole period). To get this inverted matrix, you need to call the following method :

```
ArrayList<ArrayList<Float>> invertedMatrix = MatrixUtils.invertFloatMatrix(mbtEEGPackets.getChannelsData()));
```

To get the status associated to the EEG data, you need to call the following getter:

```
mbtEEGPackets.getStatusData()
```

*Note: the status data is currently not used.*

To display all the information included in the returned `MbtEEGPacket` object, you need to add the following code : 

```
mbtEEGPackets.toString()
```

To determine if the returned `MbtEEGPacket` object contains only empty values, you need to add the following code : 

```
mbtEEGPackets.isEmpty()
```

###### UNDERSTANDING THE QUALITY CHECKER*

The quality checker is a closed source algorithm that is able to assess the quality of the EEG signal. It is based on the recorded EEG signal and the sampling frequency. 
The output is a one-dimension ArrayList<Float> that contains one value per channel. (ie 2 for melomind because melomind has only two EEG channels)
The following values represents the possible output of the quality checker algorithm.

- 1.0 : The EEG signal is very good and perfectly usable. 
- 0.5 : the EEG is almost good but there is a lot of physiological artifacts (blinking, eyes motion, ...)
- 0.25 : the EEG is almost good but there is a lot of muscular artifacts (chewing, head motion,...)
- 0.0 : The EEG is very bad and unusable. This may indicate that the headset is not correctly set on one's head.
- -1.0 : There is no EEG: The headset is probably not on someone's head. 

###### STOPPING A CURRENT EEG STREAM 

For stopping the EEG acquisition, you need to call the following method:

```
sdkClient.stopStream()
```

This method stops to transmit notification to the application so that no EEG data acquired by the headset are received.

*Note: You should only call this method if a streaming has been started and if the application is connected to a headset.*

##### Device Features

Information and configuration relative to the headset are managed through the use of the Device features. It allows read of the battery level and configuration of some filters, gain and other parameters.

###### READING BATTERY LEVEL 

To get the current battery level of the connected headset, you need to call the following method:

```
sdkClient.readBattery(deviceBatteryListener)
```

**Parameters**

> `deviceBatteryListener` is an instance of the `DeviceBatteryListener` Object.  It provides a callback that returns the current battery charge level. Use the `DeviceBatteryListener` constructor to create the instance.

This method sends a request to the headset to get the current battery level. The headset response triggers the `onBatteryChanged` callback, that return the value of the battery level in the `newLevel` variable. The battery level is given as a percentage included between 0 and 100 :

- A value of 0 means that the battery of the headset is empty.
- A value of 100 means that the battery of the headset is totally charged.
- The possible values are 0 %, 15%, 35%, 50%, 65%, 85%, 100%

 Before calling this method, you need to create a non null instance of the `DeviceBatteryListener` Object : 

```
DeviceBatteryListener deviceBatteryListener = new DeviceBatteryListener() {
    @Override
    public void onBatteryChanged(String newLevel) {}
    
    @Override 
    public void onError(BaseError error, String additionnalInfo) {}
};
```

 The `onError()` method will be called if an error that affects the transfer of the battery level. 

You can handle the battery value received in this method and use it according to your specific needs. For example, you can display the battery level as a text, or as a battery icon representing the current percentage.

Here is a full example of battery reading when a headset is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

DeviceBatteryListener deviceBatteryListener = new DeviceBatteryListener() {
    @Override
    public void onBatteryChanged(String newLevel) {
    
    }
    
    @Override 
    public void onError(BaseError error, String additionnalInfo) {}
};


BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
    sdkClient.readBattery(deviceBatteryListener)
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```
###### GET THE CONNECTED HEADSET INFO
To get the connected headset information, you need to call the following method:

```
sdkClient.requestCurrentConnectedDevice(simpleRequestCallback)
```

**Parameters**

> `simpleRequestCallback` is an instance of the `SimpleRequestCallback<MbtDevice>` Object.  It provides a callback that returns the current connected device as a `MbtDevice` Object. Use the `SimpleRequestCallback<MbtDevice>` constructor to create the instance.

This method sends a request to get the connected headset information bundled in a `MbtDevice` Object. The headset response triggers the `onRequestComplete` callback, that return the connected headset in the `device` variable. The 

 Before calling this method, you need to create a non null instance of the `SimpleRequestCallback<MbtDevice>` Object : 

```
SimpleRequestCallback<MbtDevice> simpleRequestCallback = new SimpleRequestCallback<MbtDevice>() {
            @Override
            public void onRequestComplete(MbtDevice device) {
               
            }
};
```

 The `onRequestComplete()` method returns a null `device` if no headset is connected. 

For example, you can get the value of the Bluetooth address or the serial number of the connected headset by calling one of the available getters provided by the `MbtDevice` Object.

Here is a full example to get the connected headset serial number when a headset is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
    sdkClient.requestCurrentConnectedDevice(new SimpleRequestCallback<MbtDevice>() {
            @Override
            public void onRequestComplete(MbtDevice device) {
                String serialNumber = device.getSerialNumber();

            }
        });
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```

###### CHANGING SERIAL NUMBER* 

Each headset has a unique serial number to identify it.
*Warning : If you change the serial number, the associated QR code scan won't work anymore*

To change the serial number of the connected headset, you need to call the following method:

```
sdkClient.updateSerialNumber(serialNumber, requestCallback)
```

**Parameters**

> `serialNumber` is an instance of the `String` Object.  It must contains the new serial number to set to the connected headset.
> `requestCallback` is an instance of the `SimpleRequestCallback<byte[]>` Object.  It provides a callback that returns the response sent by the headset once the update command is received. Use the `SimpleRequestCallback` constructor to create the instance.

This method sends a request to the headset to change its serial number. The headset response triggers the `onRequestComplete` callback, that return the current value of the serial number `object` variable after the update. 

Here is a full example of how to change the serial number to `melo_9876543210` when a headset whose name is `melo_0123456789` is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
  
    sdkClient.updateSerialNumber("melo_987654321", new SimpleRequestCallback<byte[]>() {
            @Override
            public void onRequestComplete(byte[] object) {
            }
        });
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```


###### CHANGING PRODUCT NAME* 

Each headset has a product name that matchs most of the time the serial number.
To change the product name of the connected headset, you need to call the following method:

```
sdkClient.updateProductName(productName, requestCallback)
```

**Parameters**

> `productName` is an instance of the `String` Object.  It must contains the new product name to set to the connected headset.
> `requestCallback` is an instance of the `SimpleRequestCallback<byte[]>` Object.  It provides a callback that returns the response sent by the headset once the update command is received. Use the `SimpleRequestCallback` constructor to create the instance.

This method sends a request to the headset to change its product name. The headset response triggers the `onRequestComplete` callback, that return the current value of the product name `object` variable after the update. 

Here is a full example of how to change the product name to `example` when a headset whose name is `melo_0123456789` is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
  
    sdkClient.updateProductName("example", new SimpleRequestCallback<byte[]>() {
            @Override
            public void onRequestComplete(byte[] object) {
            }
        });
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```

###### CHANGING EXTERNAL NAME* 

Each headset has an external name that matchs the QR code number.
*Warning : If you change the external name, the QR code scan won't work anymore*

To change the external name of the connected headset, you need to call the following method:

```
sdkClient.updateExternalName(productName, requestCallback)
```

**Parameters**

> `externalName` is an instance of the `String` Object.  It must contains the new external name to set to the connected headset.
> `requestCallback` is an instance of the `SimpleRequestCallback<byte[]>` Object.  It provides a callback that returns the response sent by the headset once the update command is received. Use the `SimpleRequestCallback` constructor to create the instance.

This method sends a request to the headset to change its external name. The headset response triggers the `onRequestComplete` callback, that return the current value of the product name `object` variable after the update. 

Here is a full example of how to change the product name to `melo_9876543210` when a headset whose name is `melo_0123456789` is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
  
    sdkClient.updateProductName("melo_9876543210", new SimpleRequestCallback<byte[]>() {
            @Override
            public void onRequestComplete(byte[] object) {
            }
        });
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```

###### CONNECT AUDIO* 

To stream audio using Bluetooth to a connected headset that is not already connected in audio, you need to call the following method:

```
sdkClient.connectAudio(requestCallback)
```

**Parameters**

> `requestCallback` is an instance of the `SimpleRequestCallback<byte[]>` Object.  It provides a callback that returns the response sent by the headset once the update command is received. Use the `SimpleRequestCallback` constructor to create the instance.

This method sends a request to the headset to connect audio in Bluetooth. The headset response triggers the `onRequestComplete` callback, that return a success or failure status in the `object` variable after the connection. 

Here is a full example of how to connect audio when a headset whose name is `melo_0123456789` is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
  
    sdkClient.connectAudio(new SimpleRequestCallback<byte[]>() {
            @Override
            public void onRequestComplete(byte[] object) {
            }
        });
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.create();

sdkClient.connectBluetooth(connectionConfig);

```

###### DISCONNECT AUDIO* 

To disconnect audio on a connected headset, you need to call the following method:

```
sdkClient.disconnectAudio(requestCallback)
```

**Parameters**

> `requestCallback` is an instance of the `SimpleRequestCallback<byte[]>` Object.  It provides a callback that returns the response sent by the headset once the update command is received. Use the `SimpleRequestCallback` constructor to create the instance.

This method sends a request to the headset to disconnect audio in Bluetooth. The headset response triggers the `onRequestComplete` callback, that return a success or failure status in the `object` variable after the disconnection. 

Here is a full example of how to disconnect audio when a headset whose name is `melo_0123456789` is connected in audio :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
  
    sdkClient.disconnectAudio(new SimpleRequestCallback<byte[]>() {
            @Override
            public void onRequestComplete(byte[] object) {
            }
        });
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```

###### REBOOT HEADSET* 

To reboot a connected headset, you need to call the following method:

```
sdkClient.rebootDevice(requestCallback)
```

**Parameters**

> `requestCallback` is an instance of the `SimpleRequestCallback<byte[]>` Object.  It provides a callback that returns the response sent by the headset once the update command is received. Use the `SimpleRequestCallback` constructor to create the instance.

This method sends a request to the headset to reboot. The headset response triggers the `onRequestComplete` callback, that return a success or failure status in the `object` variable after the reboot. 

Here is a full example of how to reboot a connected headset whose name is `melo_0123456789` :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
  
    sdkClient.rebootDevice(new SimpleRequestCallback<byte[]>() {
            @Override
            public void onRequestComplete(byte[] object) {
            }
        });
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```
###### GET SYSTEM STATUS * 

To get the device system status of the connected headset, you need to call the following method:

```
sdkClient.getDeviceSystemStatus(requestCallback)
```

**Parameters**

> `requestCallback` is an instance of the `SimpleRequestCallback<byte[]>` Object.  It provides a callback that returns the response sent by the headset once the update command is received. Use the `SimpleRequestCallback` constructor to create the instance.

This method sends a request to the headset to get the current headset system status. The headset response triggers the `onRequestComplete` callback, that return the value o the status in the `object` variable. 

Here is a full example of how to get the system status of a connected headset whose name is `melo_0123456789` :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
  
    sdkClient.getDeviceSystemStatus(new SimpleRequestCallback<byte[]>() {
            @Override
            public void onRequestComplete(byte[] object) {
            }
        });
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```

##### Synchronisation Features

###### SYNCHRONIZE EXTERNAL TRIGGERS * 

To receive triggers that mark timestamps for significant events in the EEG signal acquired by the connected headset, you need to :
- turn on the headset
- connect a trigger emitter to the USB plug of the headset (the blue LED might stop flashing)
- click about 5 seconds (until the blue LED flashs again) on the + button 
- enable the trigger configuration option when you start a new stream 
- retrieve the triggers sent by calling the `getStatusData()` when the onNewPackets callback is triggered.

```
sdkClient.startStream(new StreamConfig.Builder(eegListener)
                            .configureAcquisitionFromDeviceCommand(new DeviceStreamingCommands.Triggers(true))
                            .create());
```


This method sends a request to the headset to enable the triggers detection. The headset response returns the triggers to the SDK when a EEG streaming is in progress.
To get the triggers, you need to call the following getter on the streamed EEG packet :

```
mbtEEGPackets.getStatusData()

```

Here is a full example of how to receive triggers with a connected headset whose name is `melo_0123456789` :

```
MbtClient sdkClient = MbtClient.getClientInstance();

EegListener<BaseError> eegListener = new EegListener<BaseError>() {
            @Override
            public void onError(BaseError error, String additionnalInfo) {}

            @Override
            public void onNewPackets(@NonNull final MbtEEGPacket mbtEEGPackets) {
                  ArrayList<Float> triggers = mbtEEGPackets.getStatusData();
            }
                
        };
        
BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState) {}

@Override
public void onDeviceConnected() {
  
    sdkClient.startStream(new StreamConfig.Builder(eegListener)
                            .configureAcquisitionFromDeviceCommand(new DeviceStreamingCommands.Triggers(true))
                            .create());
}

@Override 
public void onDeviceDisconnected() {}

@Override 
public void onError(BaseError error, String additionnalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.create();

sdkClient.connectBluetooth(connectionConfig);

```


## V.Appendix

------

### Bluetooth states

Here is the list of all the possible Bluetooth states that the `onNewState` callback provided by the `BluetoothStateListener` Object can send. All the states from `IDLE` to `CONNECTED_AND_READY` are listed  in the chronological order corresponding to the step followed in the connection process.

` IDLE`

 Initial state that corresponds to a standby mode : it represents a state where the mobile device is not connected to any headset and is awaiting order from the user or the SDK.
  The IDLE state is automatically returned few minutes after the DATA_BT_DISCONNECTED state is returned.

------

`READY_FOR_BLUETOOTH_OPERATION`

  All the prerequisites are valid to start a Bluetooth connection operation : the device is not already connected, the Bluetooth is enabled, the location is enabled and the location permission is granted.

------

`SCAN_STARTED`

  A scan is in progress to look for an available headset.

------

` DEVICE_FOUND`

A device has been found during the scan. It can be a specific one if the user entered a device name in its connection configuration, or the first device found.

------

`DATA_BT_CONNECTING`

Currently attempting to connect to a Bluetooth headset in BLE or SPP.

------

`DATA_BT_CONNECTION_SUCCESS`

 Headset has been successfully connected in BLE or SPP.

------

`DISCOVERING_SERVICES`

Retrieving the services that the connected headset deliver. This operation is included in the connection process to ensure that a communication has well been established between the headset and the mobile device.

------

  `DISCOVERING_SUCCESS`

Successfully received the services delivered by the connected headset.

------

 `READING_FIRMWARE_VERSION`

A request has been sent to the headset to get the first Device Information (Firmware version).
This operation is included in the connection process to ensure that the received characteristics can be read by the SDK.

------

 `READING_FIRMWARE_VERSION_SUCCESS`

The Firmware version has been successfully read.

------

`    ` `READING_HARDWARE_VERSION`

A request has been sent to the headset to get the Hardware version.
This operation is included in the connection process to ensure that the received characteristics can be read by the SDK.

------

 `READING_HARDWARE_VERSION_SUCCESS`

The Hardware version has been successfully read.

------

`    ` `READING_SERIAL_NUMBER`

A request has been sent to the headset to get the Serial number.
This operation is included in the connection process to ensure that the received characteristics can be read by the SDK.

------

 `READING_SERIAL_NUMBER_SUCCESS`

The Serial Number has been successfully read.

------

 `READING_MODEL_NUMBER`

A request has been sent to the headset to get the Model number.
This operation is included in the connection process to ensure that the received characteristics can be read by the SDK.

------

`READING_SUCCESS`

The last Device Information (Model Number) has been successfully read.

------

 `BONDING`

 Exchanging and storing of the long term keys for the next times a connection is initiated between a mobile device and a headset.  This operation is included in the connection process only for headsets whose firmware version are higher than or equal to 1.7.0. Headsets whose firmware version are lower than 1.7.0 can not handle this operation so the bonding step is just skipped.

------

  `BONDED`

Headset has been successfully bonded with the mobile device.

------

  `SENDIND_QR_CODE`

Sending the QR Code as an external name to the headset.

------

  `CONNECTED`

Successfully connected for BLE or SPP streaming and ready to connect audio if the user requested it in its connection configuration. 

------

`CONNECTED_AND_READY`

 Connection process is completed. A headset has been successfully connected in BLE/SPP and in audio if user requested audio connection. 

------

`AUDIO_BT_CONNECTION_SUCCESS`

Successfully connected for audio streaming.

------

`AUDIO_BT_DISCONNECTED`

Headset has been disconnected for audio streaming.

------

`DISCONNECTING`

Headset is disconnecting.

------

`DATA_BT_DISCONNECTED`

Headset has been disconnected for in BLE or SPP.

------

`BLUETOOTH_DISABLED`

 Bluetooth is available on your mobile device but is not enabled (turned on).

------

`NO_BLUETOOTH`

The device does not have a Bluetooth interface or does not support Bluetooth Low Energy.

------

`INTERNAL_FAILURE`

 An internal failure occurred.

------

`LOCATION_DISABLED`

  Location is required in order to start the Low Energy scan. Your GPS must be enabled.

------

 `LOCATION_PERMISSION_NOT_GRANTED`

Location is required in order to start the Low Energy scan. You need to grant permissions to access FINE or COARSE location.

------

  `ANOTHER_DEVICE_CONNECTED`

Although android Bluetooth Low Energy supports multiple connection, we currently consider that only one connection at a time is possible. Instead of forcing the disconnection of the first device, it is preferable to notify user  with error state so that the user can choose if he wants to disconnect the already connected device or not.

------

`JACK_CABLE_CONNECTED`

  Audio connection cannot be established if a Jack cable already connects the headset to the mobile device.

------

   `SCAN_FAILED_ALREADY_STARTED`

The SDK failed to start scan as a scan is already running. 

------

  `SCAN_FAILURE`

The SDK failed to start scan operation.

------

 `SCAN_TIMEOUT`

 The SDK failed to find an available device within a defined allocated amount of time. The connection process that was running is automatically cancelled if this state occurs and the device will returned to an `IDLE` state.

------

`SCAN_INTERRUPTED`

The user requested to cancel the scan that is in progress.  The connection process that was running is automatically cancelled if this state occurs and the device will returned to an `IDLE` state.

------

 `CONNECTION_INTERRUPTED`

 The user requested to cancel the connection process that is in progress. The connection process that was running is automatically cancelled if this state occurs and a disconnection is triggered.

------

`CONNECTION_FAILURE`

 The SDK failed to establish Bluetooth connection with the device : this can be a BLE / SPP connection or an audio connection.

------

`AUDIO_CONNECTION_UNSUPPORTED`

The SDK failed to establish Audio Bluetooth connection with an unpaired device whose Android version is higher than or equal to API 28 (Android 9). Read User Guide to pair the device and establish Audio Bluetooth connection

*Note : The only way to connect audio with a mobile device whose Android version is higher than or equal to API 28 (Android 9) is to pair the headset before requesting a connection. To do so, you need to follow these steps :*

- *Open your mobile device Connections Settings*
- *Enable the Bluetooth*
- *Turn on your headset*
- *Click on the name of your headset that should appear in the Available Bluetooth devices list*
- *If a pairing pop up appears, click on ENABLE*
- *Wait until the pairing is done*
- *The name of your headset should now appear in the Paired Bluetooth devices list*
- *Connect your headset by calling the SDK client`connectBluetooth` method*

------

`DISCOVERING_FAILURE`

The SDK failed to retrieve the services of the connected headset. The connection process that was running is automatically cancelled if this state occurs and a disconnection is triggered.

------

`READING_FAILURE`

 The SDK failed to get a device information (Serial number, Firmware version, Hardware version, Model number) from the headset. The connection process that was running is automatically cancelled if this state occurs and a disconnection is triggered.

------

 `BONDING_FAILURE`

The SDK failed to bond the mobile device with the headset. It means that it failed to exchange or store long term keys with the headset. The connection process that was running is automatically cancelled if this state occurs and a disconnection is triggered.

------

 `STREAM_ERROR`

 The SDK failed to start EEG data stream.



### Errors

```
ERROR_NOT_CONNECTED

```

No connected headset.

------

```
ERROR_ALREADY_SCANNING

```

Scanning already started.

------

```
ERROR_SCANNING_INTERRUPTED

```

Bluetooth Scanning has been interrupted.

------

```
ERROR_SCANNING_TIMEOUT

```

Bluetooth scanning could not be completed within the permitted time.

------

```
ERROR_CONNECT_FAILED

```

Bluetooth connection failed.

------

```
ERROR_ALREADY_CONNECTED_ANOTHER

```

Another device is already connected.

------

```
ERROR_ALREADY_CONNECTED_JACK

```

Jack cable already connected.

------

```
ERROR_NOT_SUPPORTED

```

Bluetooth Low Energy not supported for this mobile device (incompatible Android OS version).

------

```
ERROR_SCANNING_FAILED

```

Bluetooth Scanning failed.

------

```
ERROR_CONNECTION_INTERRUPTED

```

Bluetooth Connection has been interrupted.

------

```
ERROR_SETTINGS_INTERFACE_ACTION

```

Bluetooth Audio Connection with an unpaired headset is not supported on your mobile. Please read the User Guide to connect Audio in a different way.

------

```
ERROR_FAIL_START_STREAMING

```

Failed to start streaming.

------

```
ERROR_UNKNOWN

```

Unknown cause.

------

```
ERROR_INVALID_PARAMS

```

Invalid configuration parameters.

------

```
ERROR_TIMEOUT_BATTERY

```

No received Battery Level value within the permitted time.

------

```
ERROR_DECODE_BATTERY

```

Failed to decode battery level value.

------

```
ERROR_PREFIX_NAME

```

Invalid headset name : it must start with melo_ prefix.

------

```
ERROR_VPRO_INCOMPATIBLE

```

Feature not available for VPro headset.

------

```
ERROR_GPS_DISABLED

```

This operation could not be started : GPS disabled.

------

```
ERROR_LOCATION_PERMISSION

```

This operation could not be started : Location permission not granted.

------

```
ERROR_BLUETOOTH_DISABLED

```

This operation could not be started : Bluetooth is disabled.

*Features only available on the Premium SDK version. Please send a message at contact@mybraintech.com to get more informations about the Premium SDK version.
