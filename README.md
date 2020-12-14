# USER GUIDE

# How to install and use the SDK in your project

## I. Overview

------

This document aims at helping you to install and use the SDK in your own mobile project. It is arranged in a way to guide you through the various steps in your development process. We recommend you to follow each step outlined below and read the documentation in the order displayed.

**What is the’ SDK ?**
The SDK is a closed source library developed by myBrain Technologies. Its main purpose is the development of Android applications connected to myBrain Technologies’ electroencephalography headsets. It comes with code examples and technical documentation for developers.

**What is electroencephalography  ?**
Electroencephalography (also known as EEG) is a technique used to measure the electrical activity of the brain, with electrodes placed along the scalp. Brain cells communicate with each other through electrical impulses that fluctuate rhythmically in distinct patterns. An EEG headset measures these electrical impulses.

**What are the myBrain Technologies’ headsets ?**
myBrain Technologies’ headsets are the Melomind and VPro headsets:

- Melomind is an audio headset composed of 2 electrodes that record electrical activity of the brain. These 2 channels of EEG acquisition are located on P3 and P4 position.
- VPro is a headset composed of 8 electrodes that record electrical activity of the brain. These channels of EEG acquisition can be located at customized position of the brain

This library is currently distributed as a .aar file. Its content is obfuscated. Only the public content is accessible to external applications.

 To this day, this document and the associated SDK are available for Android platform. An iOS version will be soon available.

## II.  Versions

------

The current version of the SDK is 2.2.11. Further updates will be released in the following months with more features.

Using the My Brain Technologies’ SDK requires to install an IDE for developing Android applications.
*Note : this document explains how to install the SDK on Android Studio IDE only.*

The SDK is compiled with Android API version 28 and is compatible with Android API version 22 and higher. Older versions will not work.  Please adjust your application’s minimum API version accordingly.

The minimum Bluetooth version of the Android device (smartphone or tablet) required to connect to a Melomind headset is the 4.0 version. Any higher version is compatible and any lower version is incompatible.

## III. Features

------

The main features offered by the SDK are listed below.

### Bluetooth

- Bluetooth connection with a Melomind or Vpro headset
- Bluetooth data transfer from the headset to the SDK
- Bluetooth command sending from the SDK to the headset*
- Configuration of Bluetooth parameters such as the Maximum Transmission Unit (maximum size of the data sent by the headset to the SDK)

### EEG

- Real time EEG raw data streaming
- Signal quality computation*
- Muscular artefacts detection*
- DC Offset & Saturation measurement*
- Relaxation index computation*
- Signal Bandpass filter*

### Device

 Configuration of the following customizable parameters for the Melomind headset :

- Notch filter*
- Gain*
- DC offset*
- Saturation*
- Sampling rate*
- Firmware update*

### Recording

- Recording of the EEG and associated data in JSON files.

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

The SDK relies on Bluetooth Low Energy scanner. From Android 6, the Bluetooth is considered as a location technology and Bluetooth devices addresses can't be scanned without GPS. So it is necessary to request the location permission for using Bluetooth features. To do so, add the following code in your AndroidManifest.xml file :

```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

If you are using the OSC stream feature, you need to request an additional permission. As OSC uses the Wifi network to stream data, you must request the internet permission in your application and add the following code :

```
    <uses-permission android:name="android.permission.INTERNET"/>
```

### 2. How to install the SDK 

Inside the *build.gradle* file located at the **root** of your project folder, add the following code :

```
repositories{
    maven{
        url urlToReplace
        credentials {
            username usernameToReplace
            password passwordToReplace
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

Add the following dependency to your dependencies list. If the `2.2.4` version is not the last available version, replace `2.2.4` with the last version of the SDK:

```
implementation 'mybraintech.com:sdk-lite:2.2.4'
implementation 'mybraintech.com:sdk-lite:2.2.4:javadoc'
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
`MbtClient.init()` at least once. This method does not change the value of the client instance if it has already been initialized.

To reset your client instance, you can call the following method:

```
sdkClient = MbtClient.resetClientInstance();
```

To reset your client instance, you can call the following method:

```
sdkClient = MbtClient.resetClientInstance();
```

The application is now set up to interact with the SDK features.

##### Bluetooth Features

The Bluetooth communication between the headset and the application is possible thanks to the following features. It allows Bluetooth devices scanning, connection, disconnection, reading data transferred by the headset and EEG data streaming.

*Warning : only one headset can be connected in Bluetooth to an Android device. It is not possible to connect several headsets at the same time.*

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
public void onDeviceConnected(Mbtdevice connectedDevice) {}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override
public void onError(BaseError error, String additionalInfo) {}

}
```

The `onDeviceConnected` callback notifies you every time a new Bluetooth connection is established with a headset. The connected device is returned in the `MbtDevice` object.

The `onDeviceDisconnected` callback notifies you every time a new Bluetooth connection is interrupted with a headset. This callback can be called in several cases :

- You requested disconnection by calling the SDK client`disconnectBluetooth` method
- The connection is lost because
  - the headset has been turned off while a headset was connected
  - the Bluetooth on the mobile device has been disabled while a headset was connected
  - the headset is too far from the connected mobile device
  - the headset encountered a problem
    The disconnected device is returned in the `MbtDevice` object.

The `onError` callback notifies you in case a Bluetooth connection has failed and returns information about the origin of the failure. All error cases are listed in the Appendix (cf Errors).

The `BluetoothStateListener` Object provided 4 callbacks :

```
BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device) {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice) {}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override
public void onError(BaseError error, String additionalInfo) {}
}
```

The `onDeviceConnected`, `onDeviceDisconnected` and `onError` callbacks are common with the  `ConnectionStateListener` callbacks (cf description above).

Contrary to the `ConnectionStateListener` , the `BluetoothStateListener` has an additional `onNewState` callback. Its role is to notify you every time the Bluetooth connection state change. In other words, it returns the current state for each step of the connection process. To better understand the value and the meaning of the current state, a list of states is available in the Appendix (cf Bluetooth States).
The connecting device is also returned in the `MbtDevice` object.

By default, the current state is `BtState.IDLE`.

 The `onError` callback is triggered if an error that affects the connection state occurs. All error cases are listed in the Appendix (cf Errors).

Then you need to initialize the connection configuration by creating an instance of the `ConnectionConfig` Object. This instance must be initialized by calling the `ConnectionConfig` Builder whose only mandatory parameter is a non null instance of a`ConnectionStateListener` or `BluetoothStateListener` Object.

```
ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener).createForDevice();

 or

 ConnectionConfig connectionConfig = new ConnectionConfig.Builder(connectionStateListener).createForDevice();
```

 Moreover, you can specify some optional parameters :

- `deviceName(String deviceName)` : the parameter is a `String` Object that contains the name of the headset to connect. A Melomind name always starts with `melo_` followed by 10 digits. If you do not specify `deviceName` or set the`deviceName` parameter to `null` in your `ConnectionConfig` builder, the SDK will connect the first available Melomind found no matter its name.

- `deviceQrCode(String deviceQrCode)` : the parameter is a `String` Object that contains the QR code identifier of the headset to connect. A Melomind QR code always starts with `MM` followed by 7 or 8 digits. If you do not specify `deviceQrCode` neither `deviceName`, or set the`deviceQrCode` and `deviceName` parameters to `null` in your `ConnectionConfig` builder, the SDK will connect the first available Melomind found. If you want to connect a specific headset, you can specify only `deviceQrCode` or only`deviceName`. As every QR code and name are unique, if you do specify `deviceQrCode` and`deviceName` in your `ConnectionConfig` builder, the SDK will check if the QR code matches the name. If they don't match, an error is returned on the `onError()` callback.

- `maxScanDuration(long durationInMillis)` : the maximum scanning duration is the time within the SDK is allowed to look for an available headset to connect. The scan is stopped if the connection operation time out and no headset is found within the permitted time. A minimum value of **10000** milliseconds is mandatory, and there is no maximum value. If you do not specify `maxScanDuration`in your `ConnectionConfig` builder, the maximum duration is set to 30000 milliseconds.

  *Note: This value is in milliseconds.*

- `connectAudio()`:  as the Melomind headset is an audio headset, you can enable Bluetooth connection for audio streaming by specifying `useAudio`. If you do not specify `connectAudio`, the headset is only connected for data streaming and no audio stream can be broadcasted to it.  

  *Note : This option must be disabled if you connect the headset with a Jack cable. To disable it, do not specify `connectAudio` in your `StreamConfig` builder.* 

- `mtu(int mtu)` * : the maximum transmission unit is the maximum size of the data packet sent by the headset to the SDK. This value is updated during the connection procress and can't be changed once the device is connected. A minimum value of **23** and a maximum value of **121** are mandatory. If you do not specify `mtu`in your `ConnectionConfig` builder, the mtu is set to 47.

- `createForDevice(MbtDeviceType deviceType)` * : the device type can be MELOMIND or VPRO.

Here is a full example of connection to a Melomind headset whose name is `melo_0123456789`, with audio stream option enabled :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device) {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice) {}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override
public void onError(BaseError error, String additionalInfo) {}

}

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.createForDevice();

sdkClient.connectBluetooth(connectionConfig);
```

###### DISCONNECTION

To end the current connection with a connected headset, you need to call the following method:

```
sdkClient.disconnectBluetooth()
```

This method ends the current Bluetooth connection with a connected headset so that no more communication is stopped between the headset and the application. If a data streaming is in progress, it stops transmitting the notifications that return the acquired EEG data of the headset to the application. If audio is connected in Bluetooth, the method also disconnects the audio stream.

*Tip: To check whether a Melomind headset has been correctly disconnected, you can take a look at the LED located next to the ON/OFF button. If the LED is emitting a flashing blue light, it means that the headset has been disconnected. If the LED is emitting a blue light but is not flashing, it means that the headset is still connected.*

*Note : Make sure to call this method if the Android device is effectively connected to a headset.*

##### EEG Features

The EEG data acquisition and signal processing are handled by the EEG features. It allows you to start and stop EEG data streaming.

###### STARTING AN EEG STREAM

For starting the EEG acquisition, you needs to call the following method:

```
sdkClient.startStream(streamConfig)
```

**Parameters**

> `streamConfig` is the streaming configuration Object.  It provides some setters to specify the streaming parameters and registers a listener that will notify you when the EEG data are received. Use the `StreamConfig.Builder` to create the instance.

This method sends a request to the headset to get the EEG data measured by the headset. The headset response triggers the `onNewPackets` callback, that returns the EEG data in the `mbtEEGPackets` variable. EEG data are sent until the `sdkClient.stopStream()` method is called. By default, the period (time interval) of notification is **1** second. It means that EEG data will be received every second. You can choose the value of this period when you initialize the streaming configuration (see below).

You can handle the EEG data received and use them according to your specific needs. For example, you can display the EEG data values as a text, or plot them in a chart, or save them in a database.

Before calling this method, you need to create a non null instance of an `EegListener` Object. This listener will notify you when the EEG data are received. The `EegListener` implements 3 callbacks :

```
EegListener<BaseError> eegListener = new EegListener<BaseError>() {
            @Override
            public void onError(BaseError error, String additionalInfo) {}

            @Override
            public void onNewPackets(@NonNull final MbtEEGPacket mbtEEGPackets) {}

            @Override
            public void onNewStreamState(StreamState state) {}

        };
```

 The `onError` callback is triggered if an error that affects the transfer of EEG data occurs. The different error cases are listed in the Appendix (cf Errors).

The `onNewPackets` callback returns the EEG data acquired during a period (1 second by default) in the `mbtEEGPackets` object. The `MbtEEGPacket` Object is explained below (cf UNDERSTAND THE EEG DATA)

The `onNewStreamState` callback returns the current EEG streaming state. The different stream state cases are listed in the Appendix (cf Stream States).



Then you need to create a non null instance of the `StreamConfig` Object. This instance must be initialized by calling the `StreamConfig` Builder whose only mandatory parameter is a non null instance of a `EegListener` Object.

```
     StreamConfig streamConfig = new StreamConfig.Builder(eegListener).createForDevice();
```

Moreover, you can specify some optional parameters :

- `useQualities()` * : the SDK computes the signal quality from the raw EEG signal acquired for every acquisition channel if you specify `useQualities` . This computation is performed asynchronously in order not to congest the EEG flow. The computed qualities are associated to the raw EEG data in the `MbtEEGPacket` Object returned by the `onNewPackets` callback, and can be retrieved by calling `mbtEEGPackets.getQualities()`. This getter returns a list whose size is equal to the number of acquisition channels of the headset. For example, the Melomind headset has 2 channels, so the first value corresponds to the first channel (brain position P3) and the second value corresponds to the second channel (brain position P4). The signal quality is not computed if you do not specify `useQualities` in your `StreamConfig` builder.
- `setNotificationPeriod(int periodInMillis) ` : the SDK sends the acquired EEG data to the SDK every second by default. You can increase or decrease this notification period by setting the value of your choice to the `periodInMillis` parameter. This option can be useful to improve the accuracy of a real time acquisition. The minimum value is 200 milliseconds if the signal quality is not computed, and there is no maximum value. A notification period of 1000 milliseconds is required to compute the signal quality. If you do not specify any `setNotificationPeriod` in your `StreamConfig` builder, the default period (1 second) is used.

*Note : This value is in milliseconds.*

- `configureAcquisitionFromDeviceCommand(DeviceStreamingCommands... deviceCommands)` * : you can send commands to change the headset device notch filter, gain, or enable DC Offset, saturation and triggers detection before starting the stream. You can send one or more commands by separating them with a comma in the parameters. If you do not specify any `configureAcquisitionFromDeviceCommand` in your `StreamConfig` builder, the default filter, gain are used and DC offset, saturation and triggers are not detected.
- `streamOverOSC(SynchronisationConfig.OSC config)` * : you can stream the EEG packet data over OSC. The SynchronisationConfig.OSC `config` is the OSC configuration Object.  It provides some setters to specify the data to stream and the network parameters.
- `createForDevice(MbtDeviceType deviceType)` * : the device type can be MELOMIND or VPRO.

To stream over OSC, you need to create a non null instance of the `SynchronisationConfig.OSC` Object. This instance must be initialized by calling the `SynchronisationConfig.OSC` Builder.
You can specify some parameters :

- `port(int port)` * : the SDK streams the data to the specified port. Default value is 8000 if you don't specify any value.
- `ipAddress(String ipAddress)` * : the SDK streams the data to the specified computer identified by its IP address. If you don't know your computer IP address, you can open a command line window and type "ipconfig".
- `streamRawEeg()` * : the SDK streams the EEG raw data over OSC. Your OSC receiver has to listen the following address to get the raw EEG : /raweeg. 
- `streamQualities()` * : the SDK streams the EEG qualities over OSC. Your OSC receiver has to listen the following address to get the qualities : /quality. 
- `streamStatus()` * : the SDK streams the triggers status over OSC. Your OSC receiver has to listen the following address to get the status : /status. 
- `streamFeature(Feature feature)` * : the SDK streams a single Frequency band feature over OSC. Your OSC receiver has to listen the following address to get the feature : /feature/frequencyBand/featureType. You must replace frequencyBand with the name of the frequency band you're interested in and featureName with the type of feature you're interested in. For example to get the alpha power, the address will be : /feature/alpha/power. 
- `streamFeatures(Features… features)` * : the SDK streams several Frequency band features over OSC. Each feature to stream can be separated with a comma, or an array of Feature object can also be passed.

Here is a full example of streaming EEG data when a Melomind headset is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

EegListener<BaseError> eegListener = new EegListener<BaseError>() {
            @Override
            public void onError(BaseError error, String additionalInfo) {}

            @Override
            public void onNewPackets(@NonNull final MbtEEGPacket mbtEEGPackets) {}

        };

StreamConfig streamConfig = new StreamConfig.Builder(eegListener)
.setNotificationPeriod(20000)
.useQualities()
.configureAcquisitionFromDeviceCommand(
    new DeviceStreamingCommands.DcOffset(true),
    new DeviceStreamingCommands.AmplifierGain(AmpGainConfig.AMP_GAIN_X12_DEFAULT),
    new DeviceStreamingCommands.NotchFilter(FilterConfig.NOTCH_FILTER_50HZ)
    new DeviceStreamingCommands.Triggers(true))
.createForDevice();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState, Mbtdevice device) {}

@Override

public void onDeviceConnected(Mbtdevice connectedDevice) {
    sdkClient.startStream(streamConfig);
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.createForDevice();

sdkClient.connectBluetooth(connectionConfig);

```

###### UNDERSTANDING THE EEG DATA

The EEG data are returned as a `MbtEEGPacket` Object that contains a matrix of EEG data acquired during a time interval equals to the notification period. Each column of the matrix contains all the EEG data values acquired by one channel during the whole period. 

*Note : Unit is microvolt.*

 Considering that all the channels are sending the same number of data, all the columns have the same number of item. The number of item is equal to the sampling rate X the notification period.  Each line of the matrix contains the acquired EEG data by all the channels at a specific moment.

For example, one line contains 2 EEG data values with a Melomind headset as it has 2 channels of acquisition /electrodes. As for a Vpro headset, one line contains 8 EEG data values.

To get the matrix of EEG data, you need to call the following getter :

```
mbtEEGPackets.getChannelsData()
```

To get the number of line of the matrix / the number of EEG data acquired by one channel during the whole period, you need to add the following code :

```
mbtEEGPackets.getChannelsData().size()
```

For example, if the period is 1 second and the sampling rate is 250Hz, then you'll get 250 EEG voltage values per channel in the packet.

To get the number of column of the matrix / the number of EEG data acquired by all the channels (= the number of channels) at a specific moment, you need to add the following code :

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
The output is a one-dimension ArrayList<Float> that contains one value per channel. (ie 2 for Melomind because melomind has only two EEG channels)
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

###### BANDPASS FILTER

For applying a bandpass filter to a EEG signal, you need to call the following method:

```
sdkClient.bandpassFilter(float minFrequency, float maxFrequency, int size, float[] signalToFilter, SimpleRequestCallback<float[]> resultCallback)
```

**Parameters**

> `minFrequency` is the minimum bound of the bandpass filter : frequencies under this minimum will be rejected.
> `maxFrequency` is the maximum bound of the bandpass filter : frequencies above this maximum will be rejected.
> `size` is the size of the input signal data
> `signalToFilter` is the EEG signal of a single channel
> `resultCallback` is a callback that returns the resulting filtered signal. It returns `null` if the `signalToFilter` is null or empty, or if the size is lower than 0.

##### Device Features

Information and configuration relative to the headset are managed through the use of the Device features. It allows you to read the battery level and change configuration of some filters, gain and other parameters.

###### READING BATTERY LEVEL

To get the current battery level of the connected headset, you need to call the following method:

```
sdkClient.readBattery(deviceBatteryListener)
```

**Parameters**

> `deviceBatteryListener` is an instance of the `DeviceBatteryListener` Object.  It provides a callback that returns the current battery charge level. Use the `DeviceBatteryListener` constructor to create the instance.

This method sends a request to the headset to get the current battery level. The headset response triggers the `onBatteryLevelReceived` callback, that return the value of the battery level in the `newLevel` variable. The battery level is given as a percentage included between 0 and 100 :

- A value of 0 means that the battery of the headset is empty.
- A value of 100 means that the battery of the headset is totally charged.
- The possible values are 0 %, 15%, 35%, 50%, 65%, 85%, 100%

 Before calling this method, you need to create a non null instance of the `DeviceBatteryListener` Object :

```
DeviceBatteryListener deviceBatteryListener = new DeviceBatteryListener() {
    @Override
    public void onBatteryLevelReceived(String newLevel) {}

    @Override
    public void onError(BaseError error, String additionalInfo) {}
};

```

 The `onError()` method will be called if an error that affects the transfer of the battery level.

You can handle the battery value received in this method and use it according to your specific needs. For example, you can display the battery level as a text, or as a battery icon representing the current percentage.

Here is a full example of battery reading when a headset is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

DeviceBatteryListener deviceBatteryListener = new DeviceBatteryListener() {
    @Override
    public void onBatteryLevelReceived(String newLevel) {

    }

    @Override
    public void onError(BaseError error, String additionalInfo) {}
};


BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device) {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice) {
    sdkClient.readBattery(deviceBatteryListener)
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.createForDevice();

sdkClient.connectBluetooth(connectionConfig);




```

###### GET THE CONNECTED HEADSET INFO

To get the connected headset information, you need to call the following method:

```
sdkClient.requestCurrentConnectedDevice(simpleRequestCallback)



```

**Parameters**

> `simpleRequestCallback` is an instance of the `SimpleRequestCallback<MbtDevice>` Object.  It provides a callback that returns the current connected device as a `MbtDevice` Object. Use the `SimpleRequestCallback<MbtDevice>` constructor to create the instance.

This method sends a request to get the connected headset information bundled in a `MbtDevice` Object. The headset response triggers the `onRequestComplete` callback, that return the connected headset in the `device` variable.

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
public void onNewState(BtState newState, Mbtdevice device) {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice) {
    sdkClient.requestCurrentConnectedDevice(new SimpleRequestCallback<MbtDevice>() {
            @Override
            public void onRequestComplete(MbtDevice device) {
                String serialNumber = device.getSerialNumber();

            }
        });
}


@Override 
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override 
public void onError(BaseError error, String additionalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.createForDevice();

sdkClient.connectBluetooth(connectionConfig);




```

###### CHANGING SERIAL NUMBER *

Each headset has a unique serial number to identify it.
*Warning : If you change the serial number, the associated QR code scan won't work anymore*

To change the serial number of the connected headset, you need to call the following method:

```
sdkClient.updateSerialNumber(serialNumber, commandCallback)



```

**Parameters**

> `serialNumber` is an instance of the `String` Object.  It must contains a non null & non empty new serial number to set to the connected headset.
> `commandCallback` is an instance of the `CommandCallback<byte[]>` Object.  It provides a `onRequestSent` callback that notifies the client when the request has been successfully sent, a `onResponseReceived` callback that returns a the response sent by the headset once the update command is received, and a `onError` callback triggered if the command sending operation encountered a problem. Use the `CommandCallback` constructor to create the instance.

This method sends a request to the headset to change its serial number. The headset response triggers the `onResponseReceived` callback, that return the current value of the serial number `response` variable after the update.

Here is a full example of how to change the serial number to `9876543210` when a headset whose name is `melo_0123456789` is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device) {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice) {

    sdkClient.updateSerialNumber("987654321", new CommandCallback<byte[]>() {
            @Override
            public void onResponseReceived(MbtCommand request, byte[] response) {
               String serialNumber = new String(response);
            }

            @Override
            public void onError(MbtCommand request, BaseError error, String additionalInfo) { }
            @Override
            public void onRequestSent(MbtCommand request) { }
        });
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio();

sdkClient.connectBluetooth(connectionConfig);




```

###### CHANGING EXTERNAL NAME *

Each headset has an external name that matchs the QR code number.
*Warning : If you change the external name, the QR code scan won't work anymore*

To change the external name of the connected headset, you need to call the following method:

```
sdkClient.updateExternalName(externalName, commandCallback)



```

**Parameters**

> `externalName` is an instance of the `String` Object.  It must contains the new external name to set to the connected headset.
> `commandCallback` is an instance of the `CommandCallback<byte[]>` Object.  It provides a `onRequestSent` callback that notifies the client when the request has been successfully sent, a `onResponseReceived` callback that returns a the response sent by the headset once the update command is received, and a `onError` callback triggered if the command sending operation encountered a problem. Use the `CommandCallback` constructor to create the instance.

This method sends a request to the headset to change its external name. The headset response triggers the `onResponseReceived` callback, that return the current value of the external name `response` variable after the update.

Here is a full example of how to change the external name to `MM12345678` when a headset whose name is `melo_0123456789` is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device) {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice) {

    sdkClient.updateExternalName("MM12345678", new CommandCallback<byte[]>() {
            @Override
            public void onResponseReceived(MbtCommand request, byte[] response) {
               String externalName = new String(response);
            }

            @Override
            public void onError(MbtCommand request, BaseError error, String additionalInfo) { }
            @Override
            public void onRequestSent(MbtCommand request) { }
        });
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio();

sdkClient.connectBluetooth(connectionConfig);




```

###### CONNECT AUDIO *

To stream audio using Bluetooth after the Bluetooth connection, you need to call the following method:

```
sdkClient.connectAudio(commandCallback)



```

**Parameters**

> `commandCallback` is an instance of the `CommandCallback<byte[]>` Object.  It provides a `onRequestSent` callback that notifies the client when the request has been successfully sent, a `onResponseReceived` callback that returns a the response sent by the headset once the update command is received, and a `onError` callback triggered if the command sending operation encountered a problem. Use the `CommandCallback` constructor to create the instance.

This method sends a request to the headset to connect audio in Bluetooth. The headset response triggers the `onResponse` callback, that return a success or failure status in the `response` variable. The possible responses returned are bundled with masks:

- Bad BD Address : 0x02
- Already connected : 0x04
- Connection timeout (after 10 sec) : 0x08
- Invalid Linkkey : 0x10
- Jack cable connected : 0x20
- Success : 0x80

Here is a full example of how to connect audio when a headset whose name is `melo_0123456789` is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device)  {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice)  {

    sdkClient.connectAudio(new CommandCallback<byte[]>() {
           @Override
            public void onResponseReceived(MbtCommand request, byte[] response) {
               boolean isSuccess = ((reponse & 0x80) == 0x80) ; //several response cases can be included so we use a mask
            }

            @Override
            public void onError(MbtCommand request, BaseError error, String additionalInfo) { }
            @Override
            public void onRequestSent(MbtCommand request) { }
        });
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice)  {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000);

sdkClient.connectBluetooth(connectionConfig);




```

###### DISCONNECT AUDIO *

To disconnect audio on a connected headset, you need to call the following method:

```
sdkClient.disconnectAudio(requestCallback)



```

**Parameters**

> `commandCallback` is an instance of the `CommandCallback<byte[]>` Object.  It provides a `onRequestSent` callback that notifies the client when the request has been successfully sent, a `onResponseReceived` callback that returns a the response sent by the headset once the update command is received, and a `onError` callback triggered if the command sending operation encountered a problem. Use the `CommandCallback` constructor to create the instance.

This method sends a request to the headset to disconnect audio in Bluetooth. The headset response triggers the `onResponse` callback, that return a success or failure status in the `response` variable. The possible responses returned are :

- Failure : 0x01
- Success : 0xFF

Here is a full example of how to disconnect audio when a headset whose name is `melo_0123456789` is connected in audio :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device) {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice) {

    sdkClient.disconnectAudio(new CommandCallback<byte[]>() {
           @Override
            public void onResponseReceived(MbtCommand request, byte[] response) {
               boolean isFailure = ((reponse & 0x01) == 0x01);
            }

            @Override
            public void onError(MbtCommand request, BaseError error, String additionalInfo) { }
            @Override
            public void onRequestSent(MbtCommand request) { }
        });
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()

sdkClient.connectBluetooth(connectionConfig);




```

###### REBOOT HEADSET *

To reboot a connected headset, you need to call the following method:

```
sdkClient.rebootDevice(commandCallback)



```

**Parameters**

> `simpleCommandCallback` is an instance of the `SimpleCommandCallback<byte[]>` Object.  It provides a `onRequestSent` callback that notifies the client when the request has been successfully sent, and a `onError` callback triggered if the command sending operation encountered a problem. Use the `SimpleCommandCallback` constructor to create the instance.

This method sends a request to the headset to reboot. The headset doesn't send back any response.

Here is a full example of how to reboot a connected headset whose name is `melo_0123456789` :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device)  {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice)  {

    sdkClient.rebootDevice(new CommandCallback<byte[]>() {
            @Override
            public void onError(MbtCommand request, BaseError error, String additionalInfo) { }
            @Override
            public void onRequestSent(MbtCommand request) { }
        });
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice)  {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()

sdkClient.connectBluetooth(connectionConfig);




```

###### GET SYSTEM STATUS *

To get the device system status of the connected headset, you need to call the following method:

```
sdkClient.getDeviceSystemStatus(requestCallback)



```

**Parameters**

> `commandCallback` is an instance of the `CommandCallback<byte[]>` Object.  It provides a `onRequestSent` callback that notifies the client when the request has been successfully sent, a `onResponseReceived` callback that returns a the response sent by the headset once the update command is received, and a `onError` callback triggered if the command sending operation encountered a problem. Use the `CommandCallback` constructor to create the instance.

This method sends a request to the headset to get the current headset system status. The headset response triggers the `onResponseReceived` callback, that return the value o the status in the `response` variable. The possible responses returned for each element of the array are :

- No info : 0x00
- Ok : 0x01
- Error : 0xFF

Here is a full example of how to get the system status of a connected headset whose name is `melo_0123456789` :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device)  {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice)  {

    sdkClient.getDeviceSystemStatus(new CommandCallback<byte[]>() {
            @Override
            public void onResponseReceived(MbtCommand request, byte[] response) {
               byte processorStatus = response[0];
               byte externalMemoryStatus = response[1];
               byte audioStatus = response[2];
               byte adsStatus = response[3];
            }

            @Override
            public void onError(MbtCommand request, BaseError error, String additionalInfo) { }
            @Override
            public void onRequestSent(MbtCommand request) { }
        });
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice)  {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()

sdkClient.connectBluetooth(connectionConfig);




```

###### UPDATE FIRMWARE *

To upgrade or downgrade the firmware installed on the connected headset, you need to call the following method:

```
sdkClient.updateFirmware(firmwareVersion, stateListener)



```

**Parameters**

> `firmwareVersion` is an instance of the `FirmwareVersion` Object. This object holds the number of the firmware version formatted as a String composed of 3 digits separated by a comma, as the following example : `"1.7.4"`. The FirmwareVersion constructor requires a non null and non empty String and looks like the following example : `new FirmwareVersion("1.7.4")`.

> `stateListener` is an instance of the `OADStateListener<BaseError>` Object. It provides 3 callbacks that inform the client of the update progress :

- the `onStateChanged` callback notifies the client when the a new step of the update is completed. In other words, it returns the current state for each step of the update process. To better understand the value and the meaning of the current state, a list of states is available in the Appendix (cf OAD States).
- the `onProgressPercentChanged` callback is triggered when the firmware transfer progress changes. A progress of 0 means that the transfer has not started yet. A progress of 100 means that the transfer is complete.
- the `onError` callback is triggered if the update operation encountered a problem and returns information about the origin of the failure. All error cases are listed in the Appendix (cf Errors).
  Use the `OADStateListener` constructor to create the instance.

This method downloads and installs a firmware using the Over-the-Air Download principle through Bluetooth. No wifi is required.
An update approximately lasts 4 minutes as it contains several steps that are handled one after another (no parallel tasks).
If the headset is streaming EEG data when the updateFirmware method is called, the SDK stops the streaming to perform the update.
Also, if the headset is connected for audio streaming, the SDK disconnects it, and reconnects it once the update is completed.
*Warning : A disconnection, reconnection, and reset of the Android device Bluetooth occurs at the end of the update.*

Here is a full example of how to install the last available firmware on a connected headset whose name is `melo_0123456789` :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device)  {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice)  {
      sdkClient.updateFirmware(
      new FirmwareVersion("1.7.4"), new OADStateListener<BaseError>() {
             @Override
             public void onStateChanged(OADState newState) {
             }

             @Override
             public void onProgressPercentChanged(final int progress) {
             }

             @Override
             public void onError(BaseError error, String additionalInfo) {

             }
         }
      );
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice)  {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};



```

> `commandCallback` is an instance of the `CommandCallback<byte[]>` Object.  It provides a `onRequestSent` callback that notify the client when the request has been successfully sent, a `onResponseReceived` callback that returns a the response sent by the headset once the update command is received, and a `onError` callback triggered if the command sending operation encountered a problem. Use the `CommandCallback` constructor to create the instance.

This method sends a request to the headset to change its serial number. The headset response triggers the `onResponseReceived` callback, that return the current value of the serial number `response` variable after the update. 

Here is a full example of how to change the serial number to `9876543210` when a headset whose name is `melo_0123456789` is connected :

```
MbtClient sdkClient = MbtClient.getClientInstance();

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){ 

@Override
public void onNewState(BtState newState, Mbtdevice device) {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice) {

    sdkClient.updateSerialNumber("987654321", new CommandCallback<byte[]>() {
            @Override
            public void onResponseReceived(MbtCommand request, byte[] response) {
               String serialNumber = new String(response);
            }
            
            @Override
            public void onError(MbtCommand request, BaseError error, String additionalInfo) { }
            @Override
            public void onRequestSent(MbtCommand request) { }
        });
}

@Override 
public void onDeviceDisconnected(Mbtdevice disconnectedDevice) {}

@Override 
public void onError(BaseError error, String additionalInfo) {}

}; 

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio()
.createForDevice();

sdkClient.connectBluetooth(connectionConfig);



```

##### Recording Features

###### START RECORDING 

To start saving the measured and computed data during in real time in a JSON file, you need to call the following method:

```
sdkClient.startRecord(Context context)


```

**Parameters**

> `context` is the application context.

This method save the data acquired from the moment your application calls `startRecord` to the moment it calls `stopRecord`.

###### STOP RECORDING 

To stop saving the measured and computed data during in real time in a JSON file, you need to call the following method:

```
sdkClient.stopRecord(RecordConfig recordConfig)


```

**Parameters**

> `recordConfig` is the recording configuration Object.  It provides some setters to specify the recording parameters. Use the `RecordConfig.Builder` to create the instance.

This method save the data acquired from the moment your application calls `startRecord` to the moment it calls `stopRecord`. 

The JSON file is created when your application calls stopRecord, if you have called startRecord before.
It means that no data can be recorded if you have not called `startRecord` before `stopRecord`.

*Note: You should only call this method if a streaming has been started and if the application is connected to a headset.*

To record data you need to create a non null instance of the `RecordConfig` Object. This instance must be initialized by calling the `RecordConfig` Builder.

```
     RecordConfig recordConfig = new RecordConfig.Builder().create();


```

Moreover, you can specify some optional parameters :

- `folder(String folder)`  : the JSON file is stored in the folder specified in this parameters. The root directory is used to store the file if you do not specify `folder` in your `RecordConfig` builder.
- `projectName(String projectName)`  : the JSON file name append your application name if you don't specify any `filename`. The name defined in your Manifest file for the attribute android:name is used if you do not specify `projectName` nor `filename` in your `RecordConfig` builder.
- `subjectID(String subjectID)`  : the JSON file can store the subject ID and the file name can also append this ID if you don't specify any `filename`. "-" is used if you do not specify `subjectID` in your `RecordConfig` builder.
- `condition(String condition)`  : the JSON file name can append an additional information that provide more details of the recording condition if you specify a `condition`. It doesn't append anything if you do not specify `condition` in your `RecordConfig` builder.
- `filename(String filename) ` : the JSON file is created and renamed with the name specified in this parameters. The following format is used to name the file if you do not specify any `filename` in your `RecordConfig` builder : yyyy-MM-dd_HH-mm-ss.SSS-projectName-deviceName-subjectId-condition.json . 
- `useInternalStorage() ` : the JSON file is stored in a specific folder (Internal Storage> Android> data > com.yourapplicationpackagename) if you specify `useInternalStorage`, where yourapplicationpackagename is the name of your package. It is also stored in this folder if you do not specify `useExternalStorage` nor `useInternalStorage` in your `RecordConfig` builder.
- `useExternalStorage() ` : the JSON file is stored in the Android mobile root directory if you specify `useExternalStorage` in your `RecordConfig` builder. 
- `timestamp(long timestamp) ` : a timestamp is stored in the JSON file and the file name can also append this timestamp if you specify `timestamp` in your `RecordConfig` builder. A default timestamp is automatically generated when `stopRecord` is called if you do not specify any `timestamp` in your `RecordConfig` builder.
- `duration(int duration) ` : a maximum number of EEG packet can be stored in the JSON file if you specify `duration` in your `RecordConfig` builder. The number of EEG packet recorded is not restricted if you do not specify any `duration` in your `RecordConfig` builder.
- `exerciseType(MelomindExerciseType exerciseType) ` : a type of Melomind exercise is stored in the JSON file if you specify `exerciseType` in your `RecordConfig` builder. No default exercise type is stored if you do not specify any `exerciseType` in your `RecordConfig` builder.
- `recordType(RecordType recordType) ` : a type of task performed by the subject who's EEG is recorded is stored in the JSON file if you specify `recordType` in your `RecordConfig` builder. No default record type is stored if you do not specify any `recordType` in your `RecordConfig` builder.
- `source(MelomindExerciseSource source) ` : a source of Melomind exercise is stored in the JSON file if you specify `source` in your `RecordConfig` builder. No default source is stored if you do not specify any `source` in your `RecordConfig` builder.
- `enableMultipleRecordings() ` : several JSON files can be recorded together and have a common value for the `recordingNb` JSON field that matches the number of recorded files if you specify `enableMultipleRecordings` in your `RecordConfig` builder.
- `bodyParameters(Bundle recordingParameters) ` : several additional data can be stored in the JSON file if you specify `bodyParameters` in your `RecordConfig` builder. The `recordingParameters` input bundles all these additional data to store. No default bundle is stored if you do not specify any `bodyParameters` in your `RecordConfig` builder.
- `headerComments(ArrayList<Comment> comments) ` : a list of additional comments can be stored in the JSON file if you specify `headerComments` in your `RecordConfig` builder. For instance comments could be notes written by the people that have encountered events during the recording. No default comment is stored if you do not specify any `headerComments` in your `RecordConfig` builder.
- `headerComment(Comment... comment) ` : a single comment or an array of comments can be stored in the JSON file if you specify `headerComment` in your `RecordConfig` builder. For instance comments could be notes written by the people that have encountered events during the recording. No default comment is stored if you do not specify any `headerComment` in your `RecordConfig` builder.
- `acquisitionLocations(MbtAcquisitionLocations... acquisitionLocations) ` : the specified EEG electrodes locations are stored in the JSON file if you specify `acquisitionLocations` in your `RecordConfig` builder. Melomind default locations are P3 and P4.
- `groundLocations(MbtAcquisitionLocations... groundLocations) ` : the specified ground electrodes locations are stored in the JSON file if you specify `groundLocations` in your `RecordConfig` builder. Melomind default ground location is M2 (mastoid).
- `referenceLocations(MbtAcquisitionLocations... referenceLocations) ` : the specified reference electrodes locations are stored in the JSON file if you specify `referenceLocations` in your `RecordConfig` builder. Melomind default reference location is M1 (mastoid).



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
                            .createForDevice());



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
            public void onError(BaseError error, String additionalInfo) {}

            @Override
            public void onNewPackets(@NonNull final MbtEEGPacket mbtEEGPackets) {
                  ArrayList<Float> triggers = mbtEEGPackets.getStatusData();
            }

        };

BluetoothStateListener bluetoothStateListener = new BluetoothStateListener(){

@Override
public void onNewState(BtState newState, Mbtdevice device)  {}

@Override
public void onDeviceConnected(Mbtdevice connectedDevice)  {

    sdkClient.startStream(new StreamConfig.Builder(eegListener)
                            .configureAcquisitionFromDeviceCommand(new DeviceStreamingCommands.Triggers(true))
                            .createForDevice(MbtDeviceType.MELOMIND));
}

@Override
public void onDeviceDisconnected(Mbtdevice disconnectedDevice)  {}

@Override
public void onError(BaseError error, String additionalInfo) {}

};

ConnectionConfig connectionConfig = new ConnectionConfig.Builder(bluetoothStateListener)
.deviceName(“melo_0123456789”)
.maxScanDuration(20000)
.connectAudio();

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

`DEVICE_FOUND`

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

`READING_HARDWARE_VERSION`

A request has been sent to the headset to get the Hardware version.
This operation is included in the connection process to ensure that the received characteristics can be read by the SDK.

------

`READING_HARDWARE_VERSION_SUCCESS`

The Hardware version has been successfully read.

------

`READING_SERIAL_NUMBER`

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

### OAD states

Here is the list of all the possible OAD states that the `onStateChanged` callback provided by the `OADStateListener` Object can send. All the states from `INITIALIZING` to `COMPLETED` are listed  in the chronological order corresponding to the step followed in the update process.

` INITIALIZING`

 State triggered when the client requests an OAD firmware update. As the OAD binary file that holds the new firmware is too big to be sent in a single request, the file is chucked into small packets.

------

`INITIALIZED`

State triggered when an OAD request is ready to be submitted for validation by the headset device that is out-of-date.

The SDK is then waiting for a return response that validate or invalidate the OAD request.

------

`READY_TO_TRANSFER`

State triggered once the out-of-date headset device has validated the OAD request to start the OAD packets transfer.

------

`TRANSFERRING`

State triggered once the OAD packets transfer is started.

------

`TRANSFERRED`

State triggered once the transfer is complete (all the packets have been transferred by the SDK to the out-of-date headset device). The SDK is then waiting that the headset device returns a success or failure transfer state. For example, it might return a failure state if any corruption occurred while transferring the binary file.

------

`AWAITING_DEVICE_REBOOT`

State triggered when the SDK has received a success transfer response from the headset device and when it detects that the previously connected headset device is disconnected.

------

`READY_TO_RECONNECT`

State triggered when the SDK has detected that the previously connected headset device is disconnected. The SDK needs to reset the mobile device Bluetooth (disable then enable) and clear the pairing keys of the updated headset device.

------

`RECONNECTING`

State triggered when the SDK is reconnecting the updated headset device.

------

`RECONNECTION_PERFORMED`
State triggered when the headset device is reconnected. The SDK checks that update has succeeded by reading the current firmware version and compare it to the OAD file one.

------

`COMPLETED`
State triggered when an OAD update is completed (final state).

### Stream states

Here is the list of all the possible EEG streaming states that the `onNewStreamState` callback provided by the `EegListener` Object can send.

` IDLE`

 Initial state that corresponds to a standby mode. No stream is in progress and no start and stop operation has been launched.

------

` STARTED`

 Stream has correctly been started.

------

` STOPPED`
Steam has correctly been stopped.

------

` FAILED`
Stream has not correctly been started or stopped. Something went wrong.

------

` DISCONNECTED`

 Device is disconnected: stream cannot start or is interrupted.

------

     

### Errors

`ERROR_NOT_CONNECTED`

No connected headset.

------

`ERROR_ALREADY_SCANNING`

Scanning already started.

------

`ERROR_SCANNING_INTERRUPTED`

Bluetooth Scanning has been interrupted.

------

`ERROR_SCANNING_TIMEOUT`

Bluetooth scanning could not be completed within the permitted time.

------

`ERROR_CONNECT_FAILED`

Bluetooth connection failed.

------

`ERROR_ALREADY_CONNECTED_ANOTHER`

Another device is already connected.

------

`ERROR_ALREADY_CONNECTED_JACK`

Jack cable already connected.

------

`ERROR_NOT_SUPPORTED`

Bluetooth Low Energy not supported for this mobile device (incompatible Android OS version).

------

`ERROR_SCANNING_FAILED`

Bluetooth Scanning failed.

------

`ERROR_CONNECTION_INTERRUPTED`

Bluetooth Connection has been interrupted.

------

`ERROR_SETTINGS_INTERFACE_ACTION`

Bluetooth Audio Connection with an unpaired headset is not supported on your mobile. Please read the User Guide to connect Audio in a different way.

------

`ERROR_FAIL_START_STREAMING`

Failed to start streaming.

------

`ERROR_UNKNOWN`

Unknown cause.

------

`ERROR_INVALID_PARAMS`

Invalid configuration parameters.

------

`ERROR_TIMEOUT_BATTERY`

No received Battery Level value within the permitted time.

------

`ERROR_DECODE_BATTERY`

Failed to decode battery level value.

------

`ERROR_PREFIX_NAME`

Invalid headset name : it must start with melo_ prefix.

------

`ERROR_VPRO_INCOMPATIBLE`

Feature not available for VPro headset.

------

`ERROR_GPS_DISABLED`

This operation could not be started : GPS disabled.

------

`ERROR_LOCATION_PERMISSION`

This operation could not be started : Location permission not granted.

------

`ERROR_BLUETOOTH_DISABLED`

This operation could not be started : Bluetooth is disabled.

------

`ERROR_RECONNECT_FAILED`

Incompatible firmware version : update is necessary.

------

`ERROR_TIMEOUT_UPDATE`

Firmware update could not be completed within the permitted time.

------

`ERROR_INIT_FAILED`

Preparing OAD Transfer request failed.

------

`ERROR_VALIDATION_FAILED`

Firmware rejected the OAD update request.

------

`ERROR_WRONG_FIRMWARE_VERSION`

Current firmware version does not match the update version

------

`ERROR_FIRMWARE_UPDATE_FAILED`

Firmware update failed or could not be completed within the permitted time.

------

`ERROR_FIRMWARE_REJECTED_UPDATE`

Firmware rejected the binary file that holds the firmware requested for an OAD update .

------

`ERROR_INVALID_FIRMWARE_VERSION`

Firmware version requested is invalid.

------

`ERROR_TRANSFER_FAILED`

OAD Transfer failed : corruption might occurred while transferring the binary file.

------

`ERROR_LOST_CONNECTION`

Lost Headset connection during an OAD update.

------

*Features only available on the Premium SDK version. Please send a message at sdk@mybraintech.com to get more information about the Premium SDK version.
