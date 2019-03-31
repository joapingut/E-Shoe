package es.joapingut.eshoe;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import es.joapingut.eshoe.dto.EShoeData;
import es.joapingut.eshoe.dto.EShoeUtils;

import static android.os.VibrationEffect.*;

/**
 * https://stackoverflow.com/questions/42648150/simple-android-ble-scanner
 */
public class JavaMainActivity extends AppCompatActivity {

    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_SCAN_BT = 2;

    private static final int REQUEST_UPDATE_INTERVAL = 40;
    private static final int REQUEST_DATA_INTERVAL = 40;

    private TextView lbldebug;
    private TextView lbldevice;
    private TextView lblfps;

    private SurfaceView surfaceData;
    private EShoeSurface eShoeSurface;

    private Manager manager;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lbldebug = findViewById(R.id.lblDebug);
        lbldevice = findViewById(R.id.lblDevice);
        lblfps = findViewById(R.id.lblfps);
        surfaceData = findViewById(R.id.surfaceData);
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

        SurfaceHolder surfaceDataHolder = surfaceData.getHolder();
        eShoeSurface = new EShoeSurface(manager);
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
    }

    public void onBtnTestSystem(View v){
        this.manager.connectToNewDevice(EShoeUtils.getVirtualTestDevice());
    }


    public void onBtnScan(View v){
        NotificationManager nm = (NotificationManager) getSystemService( NOTIFICATION_SERVICE);
        Notification notif = new Notification();
        notif.ledARGB = 0xFFff0000;
        notif.flags = Notification.FLAG_SHOW_LIGHTS;
        notif.ledOnMS = 100;
        notif.ledOffMS = 100;

        Notification.Builder bui = new Notification.Builder(this).setLights(0xFFff0000, 500, 100).setSmallIcon(android.R.drawable.ic_media_play);
        nm.notify(5, bui.build());

        //nm.notify(5, notif);

        Vibrator vibrat = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrat.vibrate(createOneShot(500, DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            vibrat.vibrate(500);
        }
    }

    public void onBtnSend(View v){
        if (manager.isActualConnected() && manager.isNotAsking()){
            lastFps = new Date().getTime();
            mDataChecker.run();
            mStatusChecker.run();
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
        mHandler.removeCallbacks(mStatusChecker);
        mHandler.removeCallbacks(mDataChecker);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    long lastFps;
    int fpscounter;

    Runnable mDataChecker = new Runnable() {
        @Override
        public void run() {
            try {
                manager.queryActiveForData();
            } finally {
                mHandler.postDelayed(mDataChecker, REQUEST_DATA_INTERVAL);
            }
        }
    };

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                long sc = new Date().getTime();
                EShoeData data = manager.getData();
                long pc = new Date().getTime();
                if (data != null){
                    eShoeSurface.updateInfo(data);
                }
                long now = new Date().getTime();
                lbldebug.setText("QUERY: " + (pc - sc) + " PAINT: " + (now - pc) + "\nPosition: " + data.getFootPosition() + " Phase: " + data.getStepPhase() + "\nSteps: " + manager.getNumSteps());
                if (now - lastFps > 1000){
                    lblfps.setText(fpscounter + " fps");
                    fpscounter = 0;
                    lastFps = new Date().getTime();
                } else {
                    fpscounter += 1;
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, REQUEST_UPDATE_INTERVAL);
            }
        }
    };
}