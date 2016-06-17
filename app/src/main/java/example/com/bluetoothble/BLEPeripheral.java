package example.com.bluetoothble;

/**
 * @author: flint
 * @date: 6/17/16
 */
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;


public class BLEPeripheral{

    Context mContext;

    BluetoothManager mManager;
    BluetoothAdapter mAdapter;

    BluetoothLeAdvertiser  mLeAdvertiser;

    BluetoothGattServer  mGattServer;

    public static boolean isEnableBluetooth(){
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public int init(Context context){

        if(null == mManager)
            mManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);

        if(null == mManager)
            return -1;

        if(false == context.getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            return -2;


        if(null == mAdapter)
            mAdapter = mManager.getAdapter();

        if(false == mAdapter.isMultipleAdvertisementSupported())
            return -3;

        mContext = context;
        return 0;
    }

    public void close()
    {

    }

    public static String getAddress() {
        return BluetoothAdapter.getDefaultAdapter().getAddress();
    }

    private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {

        @Override
        public void onStartFailure(int errorCode){
            Log.d("advertise","onStartFailure");
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect){
            Log.d("advertise","onStartSuccess");
        };
    };

    private final BluetoothGattServerCallback mGattServerCallback
            = new BluetoothGattServerCallback(){

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState){
            Log.d("GattServer", "Our gatt server connection state changed, new state ");
            Log.d("GattServer", Integer.toString(newState));
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.d("GattServer", "Our gatt server service was added.");
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.d("GattServer", "Our gatt characteristic was read.");
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d("GattServer", "We have received a write request for one of our hosted characteristics");
            Log.d("GattServer", "data = "+ value.toString());
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status)
        {
            Log.d("GattServer", "onNotificationSent");
            super.onNotificationSent(device, status);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.d("GattServer", "Our gatt server descriptor was read.");
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);

        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d("GattServer", "Our gatt server descriptor was written.");
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.d("GattServer", "Our gatt server on execute write.");
            super.onExecuteWrite(device, requestId, execute);
        }

    };

    private void addDeviceInfoService(BluetoothGattServer gattServer)
    {
        if(null == gattServer)
            return;
        //
        // device info
        //
        final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
        final String SOFTWARE_REVISION_STRING = "00002A28-0000-1000-8000-00805f9b34fb";

        BluetoothGattCharacteristic softwareVerCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(SOFTWARE_REVISION_STRING),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        BluetoothGattService deviceInfoService = new BluetoothGattService(
                UUID.fromString(SERVICE_DEVICE_INFORMATION),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);


        softwareVerCharacteristic.setValue(new String("0.0.1").getBytes());

        deviceInfoService.addCharacteristic(softwareVerCharacteristic);
        gattServer.addService(deviceInfoService);
    }

    public void startAdvertise()
    {
        if(null == mAdapter)
            return;

        if (null == mLeAdvertiser)
            mLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();

        if(null == mLeAdvertiser)
            return;

        AdvertiseSettings.Builder settingBuilder;

        settingBuilder = new AdvertiseSettings.Builder();
        settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingBuilder.setConnectable(true);
        settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

        AdvertiseData.Builder advBuilder;

        advBuilder = new AdvertiseData.Builder();

        mAdapter.setName("PeripheralAndroid"); //8 characters works, 9+ fails
        advBuilder.setIncludeDeviceName(true);

        mGattServer = mManager.openGattServer(mContext, mGattServerCallback);

        addDeviceInfoService(mGattServer);


        final String  SERVICE_A = "0000fff0-0000-1000-8000-00805f9b34fb";
        final String  CHAR_READ_1 = "00fff1-0000-1000-8000-00805f9b34fb";
        final String  CHAR_READ_2 = "00fff2-0000-1000-8000-00805f9b34fb";
        final String  CHAR_WRITE = "00fff3-0000-1000-8000-00805f9b34fb";


        BluetoothGattCharacteristic read1Characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_READ_1),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        read1Characteristic.setValue(new String("this is read 1").getBytes());

        BluetoothGattCharacteristic read2Characteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_READ_2),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
        );

        read2Characteristic.setValue(new String("this is read 2").getBytes());


        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(CHAR_WRITE),
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
        );


        BluetoothGattService AService = new BluetoothGattService(
                UUID.fromString(SERVICE_A),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);


        AService.addCharacteristic(read1Characteristic);
        AService.addCharacteristic(read2Characteristic);
        AService.addCharacteristic(writeCharacteristic);

        // Add notify characteristic here !!!
//        final String  CHAR_NOTIFY = "00fffB-0000-1000-8000-00805f9b34fb";
//        final BluetoothGattCharacteristic notifyCharacteristic = new BluetoothGattCharacteristic(
//                UUID.fromString(CHAR_NOTIFY),
//                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
//                BluetoothGattCharacteristic.PERMISSION_READ
//        );
//
//        notifyCharacteristic.setValue(new String("0"));
//        AService.addCharacteristic(notifyCharacteristic);
//
//        final Handler handler = new Handler();
//
//        Thread thread = new Thread() {
//            int i = 0;
//
//            @Override
//            public void run() {
//                try {
//                    while(true) {
//                        sleep(1500);
//                        handler.post(this);
//
//                        List<BluetoothDevice> connectedDevices
//                                = mManager.getConnectedDevices(BluetoothProfile.GATT);
//
//                        if(null != connectedDevices)
//                        {
//                            notifyCharacteristic.setValue(String.valueOf(i).getBytes());
//
//                            mGattServer.notifyCharacteristicChanged(connectedDevices.get(0),
//                                    notifyCharacteristic, false);
//                        }
//                        i++;
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        thread.start();

        mGattServer.addService(AService);


        mLeAdvertiser.startAdvertising(settingBuilder.build(),
                advBuilder.build(), mAdvCallback);

    }

    public void stopAdvertise()
    {
        if(null != mLeAdvertiser)
            mLeAdvertiser.stopAdvertising(mAdvCallback);

        mLeAdvertiser = null;
    }
}
