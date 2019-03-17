package es.joapingut.eshoe;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;

import es.joapingut.eshoe.dto.EShoe;
import es.joapingut.eshoe.dto.EShoeData;
import es.joapingut.eshoe.dto.RealEShoe;

public class Manager {

    private Handler mHandler;
    private Context context;

    private EShoe active;

    private boolean asking;

    public Manager (Context context, Handler mHandler){
        this.context = context;
        this.mHandler = mHandler;
    }

    public void connectToNewDevice(BluetoothDevice device){
        EShoe eshoe = new RealEShoe(context, mHandler, device);
        changeActive(eshoe);
    }

    public void connectToNewDevice(EShoe device){
        changeActive(device);
    }

    public void changeActive(EShoe newActive){
        if (isActualConnected()){
            disconnectActual();
        }
        active = newActive;
    }

    public EShoeData queryActiveForData(){
        asking = true;
        EShoeData result = active.getData();
        asking = false;
        return result;
    }

    public boolean isActualConnected(){
        return active != null && (active.getStatus() != EShoe.EShoeStatus.DISCONNECTED && active.getStatus() != EShoe.EShoeStatus.WAITING);
    }

    private void disconnectActual(){
        do {
            active.onConnectionStateChange(BluetoothProfile.STATE_DISCONNECTED);
        } while (isActualConnected());
    }

    public boolean isNotAsking() {
        return !asking;
    }

    public boolean isAsking() {
        return asking;
    }
}
