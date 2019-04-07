package es.joapingut.eshoe;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanActivity extends AppCompatActivity {

    public static final String SCAN_RESULT_DEVICE = "es.joapingut.eshoe.devicefound";
    public static final int SCAN_RESULT_CODE_FOUND = 1;
    public static final int SCAN_RESULT_CODE_NOT_FOUND = -1;

    private Map<String,BluetoothDevice> foundList;

    private BluetoothLeScanner mBluetoothLeScanner;
    private RecyclerView scanView;
    private boolean scanning;
    private Handler mHandler;

    private LinearLayout scanLinearLayout;

    private ScanCallback mLeScanCallback =
            new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.i("callbackType", String.valueOf(callbackType));
                    Log.i("result", result.toString());
                    BluetoothDevice found = result.getDevice();
                    if (found != null && found.getName() != null && found.getName().startsWith("TFM")){
                        foundList.put(found.getAddress(), found);
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothLeScanner = bluetoothManager.getAdapter().getBluetoothLeScanner();

        scanning = false;
        foundList = new HashMap<>();
        mHandler = new Handler();

        scanLinearLayout = findViewById(R.id.progress_layout);

        scanView = findViewById(R.id.scanRecyclerView);
        scanView.setHasFixedSize(true);
        scanView.setLayoutManager(new LinearLayoutManager(this));

        scanForDevices(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scanForDevices(false);
            }
        }, 5000);
    }

    private void scanComplete(){
        if (foundList.isEmpty()){
            new AlertDialog.Builder(this)
                    .setTitle(R.string.scan_empty_header)
                    .setMessage(R.string.scan_empty_body)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(SCAN_RESULT_CODE_NOT_FOUND);
                            finish();
                        }
                    })
                    .show();
        } else {
            ScanResultAdapter adapter = new ScanResultAdapter(new ArrayList<>(foundList.values()));
            adapter.setOnItemClickListener(new ScanResultAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, BluetoothDevice device) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(SCAN_RESULT_DEVICE, device);
                    setResult(SCAN_RESULT_CODE_FOUND, resultIntent);
                    finish();
                }
            });
            scanView.setAdapter(adapter);
        }
    }

    public void scanForDevices(final boolean enable) {
        if (mBluetoothLeScanner == null){
            return;
        }

        if (scanning && !enable){
            Log.i("Scanning", "stop");
            scanning = false;
            mBluetoothLeScanner.stopScan(mLeScanCallback);
            scanLinearLayout.setVisibility(View.GONE);
            scanComplete();
        } else if (!scanning && enable){
            Log.i("Scanning", "start");
            scanning = true;
            mBluetoothLeScanner.startScan(mLeScanCallback);
            scanLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanForDevices(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeScanner != null){
            mBluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }
}
