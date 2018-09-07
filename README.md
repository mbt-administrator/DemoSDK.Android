# USER GUIDE
# How to install and use the myBrain Technologies’ SDK in your projects 


## I. Overview 
----
This document aims at helping you to install and use the My Brain Technologies’ SDK in your own mobile project. It is arranged in a way to guide you through the various steps in your development process. We recommend following each of the steps outlined below and reading the documentation in the order displayed.
 
**What is the myBrain Technologies’ SDK ?** 
The myBrain Technologies’ SDK is a closed source library developed by My Brain Technologies. Its main purpose is to allow the development of external Android applications connected to myBrain Technologies’ electroencephalography headsets.  It also provides code examples, and technical and use case documentation for developers.

**What is electroencephalography  ?** 
Electroencephalography (also known as EEG) is a technique used to measure the electrical activity in the brain. Brain cells communicate with each other through electrical impulses that fluctuate rhythmically in distinct patterns.

**What are the myBrain Technologies’ headsets ?**
My Brain Technologies’ headsets includes Melomind and VPro headsets:
* Melomind is an audio headset composed of 2 electrodes that record electrical activity of the brain. These 2 channels of EEG acquisition are located on P3 and P4 parietal position.
* VPro is a headset composed of 8 electrodes that record electrical activity of the brain. These channels of EEG acquisition can be located at customized position of the brain

This library is currently distributed as a .aar file. Its content is obfuscated. Only the public content is accessible to external applications. 

 To this day, this document and the associated SDK are available for Android plateform. An iOS version will be soon available.

## II.  Versions
----
The current version of the SDK is 2.0.2. Further updates will be released in the following months with more features, but don’t try to use the 1.0.0 version, it has been removed. 

Using the My Brain Technologies’ SDK requires to install an IDE for developing Android applications. 
Note : this document explains how to install the SDK on Android Studio IDE only.

The myBrain Technologies’ SDK is compiled with Android API version 27 and is compatible with Android API version 22 and higher. Older versions will not work.  Please adjust your application’s minimum API version accordingly. 

The minimum Bluetooth version of the Android device (smartphone or tablet) required to connect to a Melomind headset is the 4.0 version. Any higher version is compatible and any lower version is incompatible.

 
## III. Features
----

The main features offered by the SDK are listed below. 

### 1.	Bluetooth Features

* Bluetooth connection with a Melomind headset
* Bluetooth disconnection of a Melomind headset.  
* Transmission and communication between the headset and the application
* Headset battery level measurement 

### 2.	EEG Data Features

* Starting EEG data acquisition from a connected headset.
* Retrieval of EEG data acquired from the headset to the application.
* Notification from the SDK to your application when new user-readable EEG data are received
* Stopping streaming to stop receiving EEG data
* Processing of the EEG signal acquired by the headset, that includes a conversion of the EEG raw data acquired into user-readable EEG data values.
* Real time computation of quality values for 1s of EEG.


## IV.  Tutorial
----

This tutorial contains instructions on how to create your own project, install the SDK and use its features. It assumes that you are using the Android Studio development environment and a supported Android device.

### 1.	Requirements

##### Create a new Android Studio Project

* Open the Android Studio IDE.
* On the top bar, click on File, select New, then New Project... 
* On the Create New Project dialog, enter your Application name, your Company domain, your project location in the file system, your package name and click on Next button
* Select “Phone and Tablet” in the form factors and select the lowest version of the Android SDK you wish to support and click on Next. Here you should choose API 22 : Android 5.1. (Lollipop).
* add an Empty Activity and click on Next.
* Enter your activity name (for example “MainActivity”) and your layout name. (For example “activity_main”) then click on Next and Finish.

##### Request the mandatory permissions

The SDK relies on Bluetooth Low Energy scanner. From Android 6, the Bluetooth is considered as a location technology so it is necessary to request the location permission for using Bluetooth features. To do so, add the following code :

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>

To do so, you must request permission when you want the user to connect to a remote Bluetooth device. 

##### Request Credentials 

For using the SDK, you must register for a personal set of credentials and specify these credentials in your Android gradle file. This credentials are unique to one application so you need to get several credentials for developping multiple applications.

_Note: The access to the SDK will be blocked if you don’t get your personal set of credentials._

To do so, send an email at **support@mybraintech.com** that includes the user name and email you want to use for your account creation. We will send you a confirmation email if the user name is not already taken with your password.
Keep your login and password safe. You’ll need them to access to the library.

### 2.	How to install the SDK 

Inside the Gradle section, open your gradle.properties file and add the following code

    nexusUrl=https://package.mybraintech.com/repository/maven-public/
    nexusUsername=username
    nexusPassword=password

Replace “**username**” and “**password**” with your own credentials.

Inside your app *build.gradle* file, add the following block of code


    repositories{
        maven{
            url nexusUrl
            credentials {
                username nexusUsername
                password nexusPassword
            }
        }
    }

Add the following dependency to your dependencies list. If the ‘2.0.2’ version is not the lastest available version, replace ‘2.0.2’ with the lastest version of the SDK:


    implementation 'mybraintech.com:sdk-lite:2.0.2


Build with ./gradlew build command or “Sync now” option on Android Studio.
That’s it, the SDK should be available.


### 3. How to use the SDK 

##### Communication between SDK & application

Once the SDK is installed on the application project, you can use its features for developing your own application. 

The SDK communicates with the application through a client: the MbtClient.

The first step is to create an instance of the MbtClient Object and initialize this client inside the `OnCreate()` method of the main activity of the application.

    private MbtClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = MbtClient.init(getApplicationContext());
    }


_Note: If you forget to initialize a MbtClient instance, you won’t have access to the Bluetooth and EEG acquisition and processing features. This initialization can also be done in the `onCreate()` method of your Application subclass._


To use your client inside several Activities, you can call the following method:


    client = MbtClient.getClientInstance();


You just need to be sure that you have called 
`MbtClient.init()` at least once. This method will throw a **NullPointerException** if the instance is null or has never been initialized.

The application is now set up to interact with the SDK features.


##### Bluetooth Features

The Bluetooth communication between the headset and the application is managed through the use of the following features. It allows Bluetooth devices scanning, connection, disconnection, reading informations and EEG streaming.

##### CONNECTION
To connect to a headset, you need to call the following method:

    client.connectBluetooth(connectionConfig);

**Parameters** 

>connectionConfig is the connection configuration Object. It provides some methods to customize the connection process. Use the ConnectionConfig.Builder to create the instance.

 Before calling this method, you need to create a non null instance of the ConnectionStateListener Object.  This listener contains a `onStateChanged(BtState newState)` method, that will be fired when the connection state has changed. 

For example, its value can be : 

    BtState.CONNECTED
or

    BtState.DISCONNECTED 

More states are listed in the appendix. 

 The `onError(ConnectionException exception)` method will be fired if an error that affects the transfer of the current connection state. All error cases are listed in the appendix.
 
Then you need to initialize the connection configuration by creating an instance of the ConnectionConfig Object. This instance must contains the instance of ConnectionStateListener Object previously created. Moreover, the user can add some specifications:

* .deviceName(String deviceName) : the parameter is a String Object that contains the name of the headset to connect. A Melomind device always starts with "melo_" followed by 10 digits.
	**If you don't want to specify any, pass null. This will tell the SDK to connect to the first found melomind** 

* .maxScanDuration(long durationInMillis) : the maximum scanning duration (time after which the application will stop looking for a headset to connect, because the scanning has taken too long). A minimum value of **20000** milliseconds is mandatory. Note: This value is in milliseconds.*

* .scanDeviceType(features.ScannableDevices.MELOMIND) : the headset type. 

For example, its value can be :

    ScannableDevices.VPRO
or

    ScannableDevices.MELOMIND

_Note: VPRO is not supported yet._

Here is an example for initializing a ConnectionStateListener and a ConnectionConfig instance to add before calling `client.connectBluetooth(connectionConfig)`

    ConnectionStateListener connectionStateListener = new ConnectionStateListener<ConnectionException>() {
        @Override
        public void onStateChanged(@NonNull BtState newState) {
        }

        @Override
        public void onError(ConnectionException exception) {
        }
    }

    ConnectionConfig connectionConfig = new ConnectionConfig.Builder(connectionStateListener)
	.deviceName(“melo_1010101010”)
	.maxScanDuration(20000)
	.scanDeviceType(features.ScannableDevices.MELOMIND)
	.create();

###### DISCONNECTION
To end the current connection with a connected headset, you need to call the following method:

    client.disconnectBluetooth()


This method ends the current connection with a connected headet so that no more information can be passed from the headset to the application. If a data streaming is in progress, it stops transmitting the notifications that return the acquired EEG data of the headset to the application.

*Tip: To check that a Melomind headset has been correctly disconnected, you can take a look at the LED located next to the ON/OFF button. If the LED is emitting a flashing blue light, it means that the headset has been disconnected. If the LED is emitting a blue light but is not flashing, it means that the headset is still connected.*

*Note : You should not call this method if the application is not connected to a headset.*


###### READING BATTERY LEVEL 
For getting the current battery level of the connected headset, you need to call the following method:

    client.readBattery(deviceInfoListener)


**Parameters**

>DeviceInfoListener deviceInfoListener is an instance of the DeviceInfoListener Object.

This method transmits a notification that contains the current headset battery level to the application. Before calling this method, you need to create a non null instance of the DeviceInfoListener Object.  This listener contains a `onBatteryChanged()` method, that will be called every time a new battery level value is sent to the client. 
The received value is contained in the “newLevel” String variable. It is a percentage included between 0 and 100 :

* A value of 0 means that the battery of the headset is empty.
* A value of 100 means that the battery of the headset is totally charged.
* The possible values are 0, 15, 35, 50, 65, 85, 100

You can handle the battery value received in this method and use it according to your specific needs. For example, you can display the battery level as a text, or as a battery icon representing the current percentage.
 
 The `onError()` method will be called if an error that affects the transfer of the battery level. 

This listener receives other informations about the headset, such as the firmware version, the hardware version or the serial number, according to the request.  

Here is an example for initializing a DeviceInfoListener instance to add before calling `client.readBattery(deviceInfoListener)` 

    DeviceInfoListener deviceInfoListener = new DeviceInfoListener() {
        @Override
        public void onBatteryChanged(String newLevel) {
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
        }
    };


*Note: You should not call this method if the application is not connected to a headset.*


##### EEG Features

The EEG data acquisition and signal processing are managed through the use of the following features. It allows starting and stopping EEG data streaming.

###### STARTING AN EEG STREAM


For starting the EEG acquisition, you needs to call the following method:

    client.startStream(streamConfig)


**Parameters**

>streamConfig is the streaming configuration Object. It provides some methods to customize the streaming parameters

This method starts to transmit notifications that contains the EEG data acquired by the headset to the application. By default, the period (time interval) of user notification is **1** second. It means that a notification will be sent every second. You can choose the value of this period when you initialize the streaming configuration.

Before calling this method, you needs to create a non null instance of the EegListener Object. 

 This listener contains a method `onNewPackets(MbtEEGPacket newPacket)`, that will be called every time a new packet of EEG data is sent to the client. You can handle the EEG data received in this method and use them according to their specific needs. For example, you can display the EEG data values as a text, or plot them in a chart, or save them in a database.

 The `onError(EEGException exception)` method will be called if an error that affects the transfer of EEG data occurs. The different cases are listed in the appendix.

Then you need to initialize the streaming configuration by creating an instance of the StreamConfig Object. This instance must contains the instance of EegListener Object previously created. Moreover, you can add some specifications:

* .setNotificationPeriod(periodInMillis) : the parameter is an integer Object that contains the notification period for receiving EEG data packets. you can set an other value. Minimum value is 200ms

*Note : This value is in milliseconds.*

* .useQualities(boolean) : Tells whether or not the quality checker will be invoked every time a new MbtEEGPacket is created. 
This algorithm is computed asynchronously in order not to congest the eeg flow. The result is stored in the MbtEEGPacket and is accessible by calling  `MBTEEGPacket.getQualities()`
**Reminder: a notification period of 1000ms is required to use the quality algorithms**

Here is an example for initializing a StreamConfig and a EegListener instance to add before calling `client.startStream(streamConfig)`

    EegListener<EEGException> eegListener = new EegListener<EEGException>() {

        @Override
        public void onError(EEGException exception) {
        
        }

        @Override
        public void onNewPackets(final MbtEEGPacket mbtEEGPackets) {

        }
    };

    StreamConfig streamConfig = new StreamConfig.Builder(eegListener).setNotificationPeriod(2000).create();


The EEG data are sent in a MbtEEGPacket Object that contains a matrix of EEG data acquired during a time interval equals to the notification period. Each column of the matrix contains all the EEG data values acquired by one channel during the whole period.

 Considering that all the channels are sending the same number of data, all the columns must have the same number of item. The number of item is equal to the sampling rate x the notification period.  Each line of the matrix contains the acquired EEG data by all the channels at a specific moment. 

For example, one line contains 2 EEG data values with a Melomind headset as it has 2 channels of acquisition /electrodes. As for a Vpro headset, one line contains 8 EEG data values.

To get the matrix of EEG data, you need to add the following code :

    mbtEEGPackets.getChannelsData()


To get the number of line of the matrix / the number of EEG data acquired by one channel during the whole period, you need to add the following code :

    mbtEEGPackets.getChannelsData().size()


To get the number of column of the matrix / the number of EEG data acquired by all the channels at a specific moment, you need to add the following code :

    mbtEEGPackets.getChannelsData().get(0).size()


As each line has the same number of EEG data, you can also call :

    mbtEEGPackets.getChannelsData().get(1).size()


It is possible to get the same matrix where the lines and columns are inverted (where each  column contains the EEG data acquired by all the channels at a specific moment and the lines contains the EEG data values acquired by one channel during the whole period). To get this inverted matrix, you need to call the following method :

    ArrayList<ArrayList<Float>> invertedMatrix = MatrixUtils.invertFloatMatrix(mbtEEGPackets.getChannelsData()));


To get the status associated to the EEG data, you need to add the following code :

    mbtEEGPackets.getStatusData()
*Note: the status data is currently not used.*

 
To display all the informations included in the returned MbtEEGPacket object, you need to add the following code : 

    mbtEEGPackets.toString()


To determine if the returned MbtEEGPacket object contains only empty values, you need to add the following code : 

    mbtEEGPackets.isEmpty()


*Note: You should not call this method if the application is not connected to a headset.*

###### UNDERSTANDING THE QUALITY CHECKER
The quality checker is a closed source algorithm that is able to assess the quality of the EEG signal. It is based on the recorded EEG signal and the sampling frequency. 
The output is a one-dimension ArrayList<Float> that contains one value per channel. (ie 2 for melomind because melomind has only two EEG channels)
The following values represents the possible output of the quality checker algorithm.
* 1.0 : The EEG signal is very good and perfectly usable. 
* 0.5 : the EEG is almost good but there is a lot of physiological artifacts (blinking, eyes motion, ...)
* 0.25 : the EEG is almost good but there is a lot of muscular artifacts (chewing, head motion,...)
* 0.0 : The EEG is very bad and unusable. This may indicate that the headset is not correcty set on one's head.
* -1.0 : There is no EEG: The headset is probably not on someones's head. 


###### STOPPING A CURRENT EEG STREAM 
For stopping the EEG acquisition, you need to call the following method:

    client.stopStream()


This method stops to transmit notification to the application so that no EEG data acquired by the headset are received.
 
*Note: You should not call this method if a streaming has not been started and if the application is not connected to a headset.*

##### Device Features

The device module is responsible for managing all the information regarding the headset. It is also responsible for managing acquisitions that are not EEG acquisitions (such as batterly level).
To have access to the currently connected device, call the following method: 

        client.requestCurrentConnectedDevice(new SimpleRequestCallback<MbtDevice>() {
            @Override
            public void onRequestComplete(MbtDevice object) {
                //TODO: your own code.
            }
        });
The `object` is null until a device is connected (ie the  `BtState.CONNECTED_AND_READY` state has been reached). 

The `MbtDevice` class is abstract and cannot be instanciated. Instead you'll receive and instance of either `MelomindDevice`or `VproDevice`, depending on which device you're connected to.  


## V.Appendix
----

### Bluetooth states
Here is the list of all the possible Bluetooth states that a ConnectionStateListener instance can send:

----

    INTERNAL_FAILURE
When something went wrong but is not necessarily related to Android itself.

----

    LOCATION_IS_REQUIRED,
Location is required in order to start the LE scan. GPS is disabled.

----
    LOCATION_PERMISSION_NOT_GRANTED,
Location is required in order to start the LE scan. Location may or may not be enabled, the user forgot to grant permissions to access FINE or COARSE location.
*Note: this is needed only in Android M and next.*

----
    SCAN_FAILED_ALREADY_STARTED
Failed to start scan as BLE scan with the same settings is already started by the app.

----
    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED
Failed to start scan as app cannot be registered.

----
    SCAN_STARTED
Scanning has just started.

----
    SCAN_TIMEOUT
Failed to find device after scanning for a defined amount of time.

   STREAM_ERROR
Failed to retrieve data.

----
    SCAN_FAILED_FEATURE_UNSUPPORTED
Failed to start power optimized scan as this feature is not supported.

----
    DISABLED
Bluetooth is available on device but not enabled (turned on).

----
    NO_BLUETOOTH
Should not occur (see Android Manifest uses-feature android:name="android.hardware.bluetooth_le" android:required="true").
The device does not have a Bluetooth interface or does not support Bluetooth Low Energy.

----
    CONNECT_FAILURE
Failed to connect : remote server not found (not in range or turned off).

----
    IDLE
Idle, not connected, awaiting order.

----
    CONNECTING
Currently attempting to connect to a Bluetooth remote endpoint.

----
    CONNECTED
Successfully connected.

----
    CONNECTED_AND_READY
Successfully connected and ready to use. This state is used when communication is finally possible, for example, when services are discovered for LE.

----
    INTERRUPTED
User request to stop connecting.

----
    DISCONNECTING
When connection is being disconnected.

----
    DEVICE_FOUND
Used to notify user when a device has been found during scanning. The device can be a specific one if the user specified one, or the first device scanned if no device has been specified.

----
    DISCONNECTED
When connection is lost.

### ConnectionExceptions values

    BT_NOT_ACTIVATED  
"Bluetooth adapter is disabled, please enable adapter first.";

    GPS_DISABLED 
"LE Scanner needs access to GPS but GPS is disabled, please enable GPS and try again";

    GPS_PERMISSIONS_NOT_GRANTED
"LE Scanner needs access to GPS but permissions are not granted, please give permissions to GPS and try again";

    LE_SCAN_FAILURE
"LE Scan has failed to start";

    CONNECTION_FAILURE
"Connection operation has failed. Please try again";

    ANOTHER_DEVICE_CONNECTED 
"Another device is already connected, please call disconnect(), wait for the DISCONNECTED event in onStateChanged callback then try again";

    INVALID_NAME 
"Invalid parameters: Input name does not match the required format. Name must start with melo_ or VPro";

    INVALID_SCAN_DURATION
"Invalid parameters: Scan duration is too small. It must be at least 10sec. Please change duration and try again";

### EEGExceptions values

    DEVICE_NOT_CONNECTED
"device not connected, impossible to start streaming";

    DEVICE_JUST_DISCONNECTED
"device has disconnected, streaming has aborted";

    STREAM_START_FAILED 
"Couldn't start EEG acquisition, please try again";

    INVALID_PARAMETERS
"Invalid input parametersn, please check your configuration and try again";



