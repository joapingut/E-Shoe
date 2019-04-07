package es.joapingut.eshoe;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;

import java.io.Serializable;

import es.joapingut.eshoe.dto.EShoe;
import es.joapingut.eshoe.dto.EShoeData;
import es.joapingut.eshoe.dto.FixedFiFo;
import es.joapingut.eshoe.dto.RealEShoe;

public class Manager implements Serializable {

    private static Manager globalManager;

    public static Manager getManagerInstance(Context context, Handler mHandler){
        if (globalManager == null){
            globalManager = new Manager(context, mHandler);
        }
        return globalManager;
    }

    private Handler mHandler;
    private Context context;

    private EShoe active;
    private EShoeData averageResult;

    private boolean asking;

    private FixedFiFo<EShoeData> buffer;
    private EShoe.EShoeStepPhase lastPhase;
    private long numSteps;
    private long numberOfSamples;

    private Manager (Context context, Handler mHandler){
        this.context = context;
        this.mHandler = mHandler;
        this.buffer = new FixedFiFo<>(30);
        this.numSteps = 0;
        this.numberOfSamples = 1;
        this.lastPhase = null;
        this.averageResult = new EShoeData();
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
        if (!isActualConnected()){
            return null;
        }
        asking = true;
        EShoeData result = active.getData();
        buffer.push(result);
        countSteps(result);
        putOnAverage(result);
        asking = false;
        return result;
    }

    public EShoeData getData(){
        return buffer.pop();
    }

    private void countSteps(EShoeData data){
        if (lastPhase == EShoe.EShoeStepPhase.REST && data.getStepPhase() == EShoe.EShoeStepPhase.LIFT){
            numSteps += 1;
            lastPhase = EShoe.EShoeStepPhase.LIFT;
        } else if (lastPhase == EShoe.EShoeStepPhase.LIFT && data.getStepPhase() == EShoe.EShoeStepPhase.REST){
            lastPhase = EShoe.EShoeStepPhase.REST;
        } else if (data.getStepPhase() != EShoe.EShoeStepPhase.UNKNOWN){
            lastPhase = data.getStepPhase();
        }
    }

    private void putOnAverage(EShoeData data){
        if (data.getStepPhase() == EShoe.EShoeStepPhase.REST){
            for (int i = 1; i <= 7; i++){
                averageResult.setData(i, averageResult.getData(i) + data.getData(i));
            }
            numberOfSamples += 1;
        }
    }

    public EShoeData getAverageResult(){
        EShoeData data = new EShoeData();
        for (int i = 1; i <= 7; i++){
            data.setData(i, averageResult.getData(i) / numberOfSamples);
        }
        return data;
    }

    public boolean isActualConnected(){
        return active != null && (active.getStatus() != EShoe.EShoeStatus.DISCONNECTED && active.getStatus() != EShoe.EShoeStatus.WAITING);
    }

    private void disconnectActual(){
        do {
            active.onConnectionStateChange(BluetoothProfile.STATE_DISCONNECTED);
        } while (isActualConnected());
        buffer.clear();
        lastPhase = null;
        numSteps = 0;
        numberOfSamples = 1;
        this.averageResult = new EShoeData();
    }

    public boolean isNotAsking() {
        return !asking;
    }

    public boolean isAsking() {
        return asking;
    }

    public long getNumSteps() {
        return numSteps;
    }
}
