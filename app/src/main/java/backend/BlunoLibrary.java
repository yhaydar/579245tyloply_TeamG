package backend;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.bbqbuddy.R;

import java.util.ArrayList;
import java.util.List;

import ui.CookingActivity;

import static ui.SettingsActivity.SHARED_PREFS;
import static ui.SettingsActivity.TempUnitSwitch;

public class BlunoLibrary extends Activity {
    private BluetoothAdapter bluetoothAdapter;
    private Context mainContext;

    private int mBaudrate=115200;	//set the default baud rate to 115200
    private String mPassword="AT+PASSWOR=DFRobot\r\n";
    private String mBaudrateBuffer = "AT+CURRUART="+mBaudrate+"\r\n";

    private static BluetoothGattCharacteristic mSCharacteristic, mModelNumberCharacteristic, mSerialPortCharacteristic, mCommandCharacteristic;
    public BluetoothLeService mBluetoothLeService;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mScanning =false;
    private String mDeviceName;
    private String mDeviceAddress;
    public enum connectionStateEnum{isNull, isScanning, isToScan, isConnecting , isConnected, isDisconnecting};

    public connectionStateEnum mConnectionState = connectionStateEnum.isNull;
    private static final int REQUEST_ENABLE_BT = 1;

    private Handler mHandler= new Handler();

    public boolean mConnected = false;

    private final static String TAG = "BluetoothLE";


    public static final String SerialPortUUID="0000dfb1-0000-1000-8000-00805f9b34fb";
    public static final String CommandUUID="0000dfb2-0000-1000-8000-00805f9b34fb";
    public static final String ModelNumberStringUUID="00002a24-0000-1000-8000-00805f9b34fb";

    public String currentTemp = "0";

    private TextView textReceived;
    private TextView textStatus;
    private TextView textBTDisconnect;

    public boolean hasBeenAlerted = false;
    boolean timerRunning = false;
    int nightModeFlags;
    private AlertDialog alertDialogBT;

    private boolean DegreesC;

    public BlunoLibrary(Context mainContext) {
        this.mainContext = mainContext;
        textReceived = ((Activity)mainContext).findViewById(R.id.text_Received);
        textStatus = ((Activity)mainContext).findViewById(R.id.textStatus);
        textBTDisconnect = ((Activity)mainContext).findViewById(R.id.textBTDisconnect);

        final BluetoothManager bluetoothManager = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Log.d(TAG, "REQUEST BLE ENABLE");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) mainContext).startActivityForResult(enableBtIntent, 1);
        }
        else
        {
            Intent gattServiceIntent = new Intent(mainContext, BluetoothLeService.class);
            mainContext.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

            IntentFilter BTAdapterFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            mainContext.registerReceiver(BTAdapterReceiver, BTAdapterFilter);
        }

        nightModeFlags = mainContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

//
//        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
//            Intent gattServiceIntent = new Intent(mainContext, BluetoothLeService.class);
//            mainContext.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//
//            IntentFilter BTAdapterFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//            mainContext.registerReceiver(BTAdapterReceiver, BTAdapterFilter);
//        }
    }



    public void onResumeProcess(){
        Log.d(TAG, "onResumeProcess");
//        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            (mainContext).startActivity(enableBtIntent);
//        }


        mainContext.registerReceiver(GattUpdateReceiver, makeGattUpdateIntentFilter());

        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            Intent gattServiceIntent = new Intent(mainContext, BluetoothLeService.class);
            mainContext.bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        IntentFilter BTAdapterFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mainContext.registerReceiver(BTAdapterReceiver, BTAdapterFilter);

        if(mBluetoothLeService != null) {
            if (mBluetoothLeService.mConnectionState == 0) {
                mConnectionState = connectionStateEnum.isScanning;
                onConectionStateChange(mConnectionState);
                scanLeDevice(true);
            }
        }

        if(mConnectionState == mConnectionState.isConnected) {
            onConectionStateChange(connectionStateEnum.isConnected);
        }
        else{
            scanLeDevice(true);
        }

    }

    public void onPauseProcess(){
        mainContext.unregisterReceiver(GattUpdateReceiver);
        mainContext.unregisterReceiver(BTAdapterReceiver);
    }

    public void onDestroyProcess(){
        Log.d(TAG, "onDestroyProcess");

        if(mBluetoothLeService!=null)
        {
			mBluetoothLeService.disconnect();
            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
            mHandler.removeCallbacks(mDisonnectingOverTimeRunnable);
            mBluetoothLeService.close();


            mainContext.unbindService(serviceConnection);
        }
        mSCharacteristic=null;

        mBluetoothLeService = null;
    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            Log.d(TAG, "Begin scanning");

            System.out.println("mBluetoothAdapter.startLeScan");

            if(!mScanning && !(bluetoothAdapter == null))
            {
                mConnectionState=connectionStateEnum.isScanning;
                onConectionStateChange(mConnectionState);
                mScanning = true;
                bluetoothAdapter.startLeScan(mLeScanCallback);
            }
            else{
                //Toast.makeText(mainContext, "Bluetooth needs to be turned on", Toast.LENGTH_LONG).show();
            }
        } else {
            if(mScanning)
            {
                mScanning = false;
                bluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    }

    private final BroadcastReceiver GattUpdateReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            System.out.println("mGattUpdateReceiver->onReceive->action="+action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                mHandler.removeCallbacks(mConnectingOverTimeRunnable);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, "ACTION_GATT_DISCONNECTED");
                mConnected = false;
                mConnectionState = connectionStateEnum.isToScan;
                onConectionStateChange(mConnectionState);
                mHandler.removeCallbacks(mDisonnectingOverTimeRunnable);
                mBluetoothLeService.close();
                BluetoothAlert();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
                    System.out.println("ACTION_GATT_SERVICES_DISCOVERED  "+
                            gattService.getUuid().toString());
                }
                getGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if(mSCharacteristic==mModelNumberCharacteristic)
                {
                    if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO")) {
                        mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, false);
                        mSCharacteristic=mCommandCharacteristic;
                        mSCharacteristic.setValue(mPassword);
                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic.setValue(mBaudrateBuffer);
                        mBluetoothLeService.writeCharacteristic(mSCharacteristic);
                        mSCharacteristic=mSerialPortCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
                        mConnectionState = connectionStateEnum.isConnected;
                        onConectionStateChange(mConnectionState);

                    }
                    else {
                        //Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
                        mConnectionState = connectionStateEnum.isToScan;
                        onConectionStateChange(mConnectionState);
                    }
                }
                else if (mSCharacteristic==mSerialPortCharacteristic) {
                    onSerialReceived(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }


                System.out.println("displayData "+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

//            	mPlainProtocol.mReceivedframe.append(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)) ;
//            	System.out.print("mPlainProtocol.mReceivedframe:");
//            	System.out.println(mPlainProtocol.mReceivedframe.toString());


            }
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            ((Activity) mainContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("mLeScanCallback onLeScan run ");
                    //mLeDeviceListAdapter.addDevice(device);
                    //mLeDeviceListAdapter.notifyDataSetChanged();
                    if (device == null)
                        return;
                    //scanLeDevice(false);

                    if(device.getName()==null || device.getAddress()==null)
                    {
                        mConnectionState= connectionStateEnum.isScanning;
                        onConectionStateChange(mConnectionState);
                    }
                    else if (device.getName().equals("Bluno")){

                        System.out.println("onListItemClick " + device.getName().toString());

                        System.out.println("Device Name:"+device.getName() + "   " + "Device Name:" + device.getAddress());

                        mDeviceName=device.getName();
                        mDeviceAddress=device.getAddress();

                        if(mBluetoothLeService != null) {

                            if (mBluetoothLeService.connect(mDeviceAddress)) {
                                Log.d(TAG, "Connect request success");
                                mConnectionState = connectionStateEnum.isConnecting;
                                onConectionStateChange(mConnectionState);
                                mHandler.postDelayed(mConnectingOverTimeRunnable, 10000);
                                scanLeDevice(false);
                            } else {
                                Log.d(TAG, "Connect request fail");
                                mConnectionState = connectionStateEnum.isToScan;
                                onConectionStateChange(mConnectionState);
                            }
                        }
                    }
                    else {
                        Log.d(TAG, device.getName());
                    }
                }
            });
        }
    };

    ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("mServiceConnection onServiceConnected");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.d(TAG, "Unable to initialize Bluetooth");
                //((Activity) mainContext).finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("mServiceConnection onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    private void getGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        mModelNumberCharacteristic=null;
        mSerialPortCharacteristic=null;
        mCommandCharacteristic=null;
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            System.out.println("displayGattServices + uuid="+uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                if(uuid.equals(ModelNumberStringUUID)){
                    mModelNumberCharacteristic=gattCharacteristic;
                    System.out.println("mModelNumberCharacteristic  "+mModelNumberCharacteristic.getUuid().toString());
                }
                else if(uuid.equals(SerialPortUUID)){
                    mSerialPortCharacteristic = gattCharacteristic;
                    System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
                else if(uuid.equals(CommandUUID)){
                    mCommandCharacteristic = gattCharacteristic;
                    System.out.println("mSerialPortCharacteristic  "+mSerialPortCharacteristic.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
                }
            }
            mGattCharacteristics.add(charas);
        }

        if (mModelNumberCharacteristic==null || mSerialPortCharacteristic==null || mCommandCharacteristic==null) {
            //Toast.makeText(mainContext, "Please select DFRobot devices",Toast.LENGTH_SHORT).show();
            mConnectionState = connectionStateEnum.isToScan;
            onConectionStateChange(mConnectionState);
        }
        else {
            mSCharacteristic=mModelNumberCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(mSCharacteristic, true);
            mBluetoothLeService.readCharacteristic(mSCharacteristic);
        }

    }

    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                textStatus.setText("Connected");
                serialSend("1");
                hasBeenAlerted = false;
                break;
            case isConnecting:
                textStatus.setText("Connecting");
                break;
            case isToScan:
                textStatus.setText("Scan Required");
                scanLeDevice(true);
                break;
            case isScanning:
                textStatus.setText("Scanning");
                break;
            case isDisconnecting:
                textStatus.setText("Disconnected");
                break;
            default:
                break;
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void onSerialReceived( String theString) {							//Once connection data received, this function will be called
        Log.d("DEBUG","onSerialRecieved");
//        char temp = theString.charAt(0);
//        int i = (int) temp;

        Boolean cTmpChecked;
        SharedPreferences preferences = mainContext.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        cTmpChecked = preferences.getBoolean(TempUnitSwitch, false);

        if(cTmpChecked){
            DegreesC = false;

        }else{
            DegreesC = true;
        }

        double currentTempInC = Double.parseDouble(theString);
        double currentTempInF = currentTempInC *1.8 +32;

        if(DegreesC){
            currentTemp = theString;
            textReceived.setText(currentTemp +"°C");
        }else {
            currentTemp = Double.toString(currentTempInF);
            textReceived.setText(currentTemp + "°F");        //append the text into the EditText
            currentTemp = theString;
        }
    }

    public void serialSend(String theString){
        if (mConnectionState == connectionStateEnum.isConnected) {
            mSCharacteristic.setValue(theString);
            mBluetoothLeService.writeCharacteristic(mSCharacteristic);
        }
    }

    public double getCurrentTemp() {
        while(this.currentTemp == null){
            try{
                Thread.sleep(200);
            }
            catch(Exception e){
                Log.d("DEBUG",e.getMessage());
            }
        }
        return Double.parseDouble(this.currentTemp);
    }

    private Runnable mConnectingOverTimeRunnable=new Runnable(){

        @Override
        public void run() {
            if(mConnectionState== connectionStateEnum.isConnecting)
                mConnectionState= connectionStateEnum.isToScan;
            onConectionStateChange(mConnectionState);
            mBluetoothLeService.close();
        }};

    private Runnable mDisonnectingOverTimeRunnable=new Runnable(){

        @Override
        public void run() {
            if(mConnectionState == connectionStateEnum.isDisconnecting)
                mConnectionState = connectionStateEnum.isToScan;
            onConectionStateChange(mConnectionState);
            mBluetoothLeService.close();
        }};

    private final BroadcastReceiver BTAdapterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            (mainContext).startActivity(enableBtIntent);
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break;
                }
            }
        }
    };

    private void BluetoothAlert(){
        Log.d("BluetoothLE", "BluetoothAlert() started");
        if(!hasBeenAlerted && timerRunning) {
            hasBeenAlerted = true;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                alertDialogBT = new AlertDialog.Builder(mainContext,AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setIcon(R.drawable.ic_bluetooth_disabled_black_24dp)
                        .setTitle("Bluetooth Connection Lost")
                        .setMessage("BBQ Buddy is no longer able to communicate " +
                                "with the Bluetooth device and is trying to " +
                                "reconnect. Please make sure that the device " +
                                "is turned on.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                textBTDisconnect.setVisibility(View.VISIBLE);
                scanLeDevice(true);
                Log.d("BluetoothLE", "scanLeDevice from onTick");
            } else {
                alertDialogBT = new AlertDialog.Builder(mainContext)
                        .setIcon(R.drawable.ic_bluetooth_disabled_black_24dp)
                        .setTitle("Bluetooth Connection Lost")
                        .setMessage("BBQ Buddy is no longer able to communicate" +
                                "with the Bluetooth device and is trying to " +
                                "reconnect. Please make sure that the device " +
                                "is turned on.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                textBTDisconnect.setVisibility(View.VISIBLE);
                scanLeDevice(true);
                Log.d("BluetoothLE", "scanLeDevice from onTick");
            }
        }
    }

    public void setTimerRunning(boolean timerStatus){
        timerRunning = timerStatus;
    }
}
