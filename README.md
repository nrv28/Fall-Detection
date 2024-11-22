# Fall-Detection App

This project aims to develop an Android application capable of detecting falls in elderly 
individuals, providing timely assistance in emergency situations. The application leverages 
the device's built-in accelerometer to monitor sudden changes in movement, characteristic 
of a fall. Upon detecting a fall, the app sends an instant alert via socket communication to a 
designated contact person, along with the user's precise GPS location. Additionally, a local 
audible alert is triggered on the device to further aid in seeking help. 

[![View Documentation](Resource/Shots.png)](Resource/Shots.png)


## Key Features 

1. Real-time Fall Detection: Utilizes the accelerometer sensor to continuously monitor the 
user's movement patterns. 
2. Instant Alert Notification: Sends immediate alerts to designated contacts via socket 
communication. 
3. GPS Location Sharing: Provides accurate GPS coordinates of the fall incident to facilitate 
prompt assistance. 
4. Local Audible Alert: Triggers a loud audible alert on the device to attract attention. 

## Installation

To install the BLE Connector app on your Android device, follow these steps:

1. Clone this repository:
    ```sh
    git clone https://github.com/nrv28/Fall-Detection
    ```
2. Open the project in Android Studio.
3. Build and run the project on your Android device.

## Permissions

The application requires the following permissions:

`<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>`
`<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>`
`<uses-permission android:name="android.permission.BODY_SENSORS"/>
`<uses-permission android:name="android.permission.VIBRATE"/>`
`<uses-permission android:name="android.permission.INTERNET"/>`
`<uses-permission android:name="android.permission.WAKE_LOCK"/>`
`<uses-feature android:name="android.hardware.sensor.accelerometer"/>`
`<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION"/>`
`<uses-permission android:name="android.permission.CALL_PHONE"/>`
`<uses-feature android:name="android.hardware.telephony" android:required="false"/>`
`<uses-feature android:name="android.hardware.camera.flash" android:required="false"/>`

- `android.permission.BLUETOOTH`
- `android.permission.BLUETOOTH_SCAN`
- `android.permission.BLUETOOTH_CONNECT`
- `android.permission.BLUETOOTH_ADMIN`
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.POST_NOTIFICATIONS`

Ensure these permissions are granted for the app to function correctly.

## Technical Implementation 

## Android App Development:

1. Sensor Data Acquisition: The accelerometer sensor is used to collect data on the device's 
acceleration in three axes (X, Y, and Z). 
2. Fall Detection Algorithm: A robust algorithm is implemented to analyze the acceleration 
data and identify patterns indicative of a fall. 
3. Socket Communication: A socket connection is established to transmit alert messages and 
GPS location data to a server or another device. 
4. GPS Location Retrieval: The device's GPS sensor is used to obtain the user's current 
location. 
5. User Interface: A simple and intuitive user interface is designed to allow users to easily 
configure the app and view essential information. 


## Server-Side : 
1. Socket Server: A server-side application is developed to receive alert messages and GPS 
location data from the mobile app. 
2. Notification System: The server can be integrated with various notification systems (e.g., 
SMS, email, push notifications) to alert caregivers or emergency services.


## Tools and Technologies  

1. Android Studio: Integrated Development Environment (IDE) for Android app 
development. 
2. Java/Kotlin: Programming languages for Android app development. 
3. Accelerometer Sensor: Device sensor to detect changes in acceleration. 
4. GPS Sensor: Device sensor to determine the user's location. 
5. Socket Programming: Network communication protocol for sending and receiving data.

   
## Future Enhancements 

1. Machine Learning Integration: Incorporate machine learning algorithms to improve fall 
detection accuracy and reduce false alarms. 
2. Remote Monitoring: Develop a web-based dashboard to allow caregivers to monitor the 
user's activity and receive real-time alerts. 
3. Emergency Call Functionality: Integrate direct calling functionality to emergency services. 
4. Wearable Integration: Extend the solution to wearable devices (e.g., smartwatches) for 
enhanced convenience and accuracy.
## Code Overview

### Main Components

1. **MainActivity**: Entry point of the application, handles Fall Detection App.
2. **DetectionActivity**: Manages the Fall-Detection using Accelerometer Data.
3. **GPS Activity**: Keep Track of the current location of Device.

### Accelerometer

- **initialize()**: Initializes the Bluetooth adapter.
- **connect(address: String)**: Connects to a BLE device with the specified address.
- **enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)**: Enables notifications for a specific characteristic.
- **createNotification()**: Creates a notification to indicate the service is running.
- **startForegroundService(notification: Notification)**: Starts the service in the foreground.
- **stopForegroundService()**: Stops the foreground service.

## Troubleshooting

- Ensure Permissions are given and device is connected over Wi-Fi.
- Grant all required permissions in the device settings.


## Contributing

Contributions are welcome! If you have any suggestions or improvements, feel free to create a pull request or open an issue.
