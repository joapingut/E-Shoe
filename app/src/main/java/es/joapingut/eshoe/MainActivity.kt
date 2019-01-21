package es.joapingut.eshoe

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView

private const val REQUEST_BT_PERMISSIONS : Int = 0
private const val REQUEST_ENABLE_BT: Int = 1

private const val SCAN_PERIOD: Long = 15000

class KotMainActivity : AppCompatActivity() {

    private val mBluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            Log.i("BLE", device.name + " addr: " + device.address)
        }
    }

    private val mHandler : Handler = Handler()

    private var buttonScan : Button? = null

    private var enable : Boolean = false

    private var mScanning : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonScan = findViewById(R.id.button_scan)

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        mBluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        if (mBluetoothAdapter == null){
            finishAndRemoveTask()
        }

    }

    fun startScan(view: View){
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                Log.i("INFO", "Scan started")
                mHandler.postDelayed({
                    mScanning = false
                    mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                mBluetoothAdapter!!.startLeScan(mLeScanCallback)
            }
            else -> {
                Log.i("INFO", "Scan stopped")
                mScanning = false
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
            }
        }
        enable = !enable
    }
}
