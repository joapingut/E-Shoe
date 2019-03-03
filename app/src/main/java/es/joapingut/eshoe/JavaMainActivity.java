package es.joapingut.eshoe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 * https://stackoverflow.com/questions/42648150/simple-android-ble-scanner
 */
public class JavaMainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mGatt;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SCAN_BT = 2;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 1000;

    public static UUID SH_H8_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static UUID SH_H8_RX_TX = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private Button btnScan = null;
    private EditText edittxt = null;

    private Manager manager;

    private ScanCallback mLeScanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.i("callbackType", String.valueOf(callbackType));
                    Log.i("result", result.toString());
                    BluetoothDevice found = result.getDevice();
                    if(mGatt ==  null && "88:3F:4A:E5:F6:85".equalsIgnoreCase(found.getAddress())){
                        mGatt = found.connectGatt(getApplicationContext(), false, gattCallback, BluetoothDevice.TRANSPORT_LE);
                        mScanning = true;
                        mBluetoothLeScanner.stopScan(this);
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult sr : results) {
                        Log.i("ScanResult - Results", sr.toString());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.i("BLE", "error");
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = findViewById(R.id.button_scan);
        edittxt = findViewById(R.id.text_send);
        mHandler = new Handler();
        manager = new Manager(this, mHandler);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE no soportado", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "BLT no soportado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else if (requestCode == REQUEST_SCAN_BT && resultCode == ScanActivity.SCAN_RESULT_CODE_FOUND){
            BluetoothDevice device = data.getParcelableExtra(ScanActivity.SCAN_RESULT_DEVICE);
            manager.connectToNewDevice(device);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }



    public void onBtnScan(View v){
        if (mScanning){
            mScanning = false;
            scanLeDevice(false);
            btnScan.setText("STOP");
        } else {
            mScanning = true;
            scanLeDevice(true);
            btnScan.setText("SCAN");
        }
    }

    public void onBtnSend(View v) throws InterruptedException {
        if (mGatt != null){
            BluetoothGattCharacteristic chara = mGatt.getService(SH_H8_UUID).getCharacteristic(SH_H8_RX_TX);
            String txt = edittxt.getText().toString();
            int size = 20;
            if (txt.length() > 20){
                for (int i = 0; i < txt.length(); i += size) {
                    chara.setValue(txt.substring(i, Math.min(txt.length(), i + size)));
                    mGatt.writeCharacteristic(chara);
                    Thread.sleep(100);
                }
            }
        }
    }

    public void onBtnToScan(View v){
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, REQUEST_SCAN_BT);
    }

    public void scanLeDevice(final boolean enable) {
        if (mBluetoothLeScanner == null){
            return;
        }

        if (enable) {
            mScanning = true;
            Log.i("Scanning", "start");
            mBluetoothLeScanner.startScan(mLeScanCallback);
            /*mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {


                }
            }, SCAN_PERIOD);*/
        } else {
            Log.i("Scanning", "stop");
            mScanning = false;
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }

    @Override
    protected void onDestroy() {
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
        super.onDestroy();
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    mGatt = gatt;
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    final BluetoothGatt gatti = gatt;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gatti.connect();
                        }
                    }, 5000);
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            final BluetoothGattCharacteristic chara;
            for (BluetoothGattService service : services) {
                Log.i("onServicesDiscoveredSVC", service.getUuid().toString());
                for (BluetoothGattCharacteristic gattCharacteristic : service.getCharacteristics()) {
                    Log.i("onServicesDisSVCGATT", gattCharacteristic.getUuid().toString());
                }
            }
            gatt.readCharacteristic(services.get(3).getCharacteristics().get(0));
            gatt.setCharacteristicNotification(services.get(3).getCharacteristics().get(0), true);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            BluetoothGattCharacteristic characteristic =
                    gatt.getService(SH_H8_UUID)
                            .getCharacteristic(SH_H8_UUID);
            characteristic.setValue(new byte[]{1, 1});
            gatt.writeCharacteristic(characteristic);
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            Log.i("onCharacteristicRead", characteristic.getStringValue(0));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("CHARCHA", new String(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i("onCharacteristicWrite", "onCharacteristicWrite " + characteristic.getUuid().toString());

            if (SH_H8_RX_TX.equals(characteristic.getUuid().toString())) {
                Log.d("onCharacteristicWrite", "Received data RX: " + characteristic.getStringValue(0));
            }
        }
    };
}