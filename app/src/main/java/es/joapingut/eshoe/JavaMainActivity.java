package es.joapingut.eshoe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import es.joapingut.eshoe.dto.EShoeData;

/**
 * https://stackoverflow.com/questions/42648150/simple-android-ble-scanner
 */
public class JavaMainActivity extends AppCompatActivity {

    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SCAN_BT = 2;

    private TextView lbldebug;

    private Manager manager;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lbldebug = findViewById(R.id.lblDebug);
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
    }



    public void onBtnScan(View v){

    }

    public void onBtnSend(View v){
        if (manager.isActualConnected() && manager.isNotAsking()){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EShoeData data = manager.queryActiveForData();
                    lbldebug.setText(new Date() + " - " + data.toString());
                }
            }, 100);
        } else {
            Toast.makeText(this, "Wait Please", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBtnToScan(View v){
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, REQUEST_SCAN_BT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}