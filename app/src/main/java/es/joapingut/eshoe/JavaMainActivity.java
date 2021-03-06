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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import es.joapingut.eshoe.dto.EShoe;
import es.joapingut.eshoe.dto.EShoeData;
import es.joapingut.eshoe.dto.EShoeUtils;


/**
 * https://stackoverflow.com/questions/42648150/simple-android-ble-scanner
 */
public class JavaMainActivity extends AppCompatActivity {

    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SCAN_BT = 2;
    private static final int REQUEST_RESULT_SCREEN = 3;

    private static final int REQUEST_UPDATE_INTERVAL = 25;
    private static final int REQUEST_DATA_INTERVAL = 10;
    private static final int REQUEST_DATA_SLEEP_INTERVAL = 250;

    private TextView lbldebug;
    private TextView lbldevice;
    private TextView lblfps;

    private SurfaceView surfaceData;
    private EShoeSurface eShoeSurface;

    private Manager manager;

    private BluetoothAdapter mBluetoothAdapter;

    private boolean runDataChecker;
    private boolean pauseDataChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lbldebug = findViewById(R.id.lblDebug);
        lbldevice = findViewById(R.id.lblDevice);
        lblfps = findViewById(R.id.lblfps);
        surfaceData = findViewById(R.id.surfaceResultData);
        mHandler = new Handler();
        manager = Manager.getManagerInstance(this, mHandler);

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

        SurfaceHolder surfaceDataHolder = surfaceData.getHolder();
        eShoeSurface = new EShoeSurface(this);
        surfaceDataHolder.addCallback(eShoeSurface);
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
        pauseDataChecker = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else if (requestCode == REQUEST_SCAN_BT && resultCode == ScanActivity.SCAN_RESULT_CODE_FOUND){
            BluetoothDevice device = data.getParcelableExtra(ScanActivity.SCAN_RESULT_DEVICE);
            lbldevice.setText(device.getName());
            manager.connectToNewDevice(device);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseDataChecker = true;
    }

    public void onBtnTestSystem(View v){
        lbldevice.setText(getString(R.string.device_name));
        this.manager.connectToNewDevice(EShoeUtils.getVirtualTestDevice());
    }

    public void onBtnSend(View v){
        if (manager.isActualConnected()){
            if (runDataChecker){
                Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
                mHandler.removeCallbacks(mStatusChecker);
                runDataChecker = false;
                Intent intent = new Intent(this, ResultActivity.class);
                startActivity(intent);
            } else {
                lastFps = new Date().getTime();
                mDataChecker.start();
                mStatusChecker.run();
            }
        } else {
            Toast.makeText(this, "Wait Please", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBtnToScan(View v){
        mHandler.removeCallbacks(mStatusChecker);
        runDataChecker = false;
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, REQUEST_SCAN_BT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mStatusChecker);
        runDataChecker = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_enable_debug:
                Log.i("ActionBar", "Enabling Debug");
                onBtnTestSystem(null);
                return true;
            case R.id.action_enable_complex:
                this.eShoeSurface.alterMode();
                return true;
            case R.id.action_change_foot:
                this.manager.setRightFoot(!this.manager.isRightFoot());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    long lastFps;
    int fpscounter;

    Thread mDataChecker = new Thread() {
        @Override
        public void run() {
            runDataChecker = true;
            pauseDataChecker = false;
            while (runDataChecker){
                try{
                    while (pauseDataChecker){
                        Thread.sleep(REQUEST_DATA_SLEEP_INTERVAL);
                    }
                    manager.queryActiveForData();
                    Thread.sleep(REQUEST_DATA_INTERVAL);
                } catch (InterruptedException ex){
                    Log.e("", "Cannot sleep on DataCheckerThread");
                }
            }
        }
    };

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (!pauseDataChecker){
                    long sc = System.currentTimeMillis();
                    EShoeData data = manager.getData();
                    long pc = System.currentTimeMillis();
                    if (data != null && data.getType() == EShoe.EShoeDataType.DT_DIME){
                        eShoeSurface.updateInfo(data);
                    }
                    long now = System.currentTimeMillis();
                    if (data != null){
                        lbldebug.setText("QUERY: " + (pc - sc) + " PAINT: " + (now - pc) + "\nPosition: " + EShoeUtils.getStringByLocal(getApplicationContext(), data.getFootPosition().getId()) + " Phase: " + EShoeUtils.getStringByLocal(getApplicationContext(),data.getStepPhase().getId()) + "\nSteps: " + manager.getNumSteps());
                    } else {
                        lbldebug.setText("QUERY: " + (pc - sc) + " PAINT: " + (now - pc) + "\nPosition: " + EShoeUtils.getStringByLocal(getApplicationContext(),EShoe.EShoeFootPosition.UNKNOWN.getId()) + " Phase: " + EShoeUtils.getStringByLocal(getApplicationContext(),EShoe.EShoeStepPhase.UNKNOWN.getId()) + "\nSteps: " + manager.getNumSteps());
                    }

                    if (now - lastFps > 1000){
                        lblfps.setText(fpscounter + " fps");
                        fpscounter = 0;
                        lastFps = System.currentTimeMillis();
                    } else {
                        fpscounter += 1;
                    }
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, REQUEST_UPDATE_INTERVAL);
            }
        }
    };
}