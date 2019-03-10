package es.joapingut.eshoe.dto;

import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VirtualEShoe implements EShoe {

    private UUID name;

    private EShoeStatus mStatus;

    public VirtualEShoe(){
        name = UUID.randomUUID();
        mStatus = EShoeStatus.CONNECTING;
    }

    @Override
    public EShoeData getData() {
        EShoeData result = new EShoeData();
        result.setType(EShoeDataType.DT_DIME);
        result.setFsr1(0.1F);
        result.setFsr2(0.3F);
        result.setFsr3(0.4F);
        result.setFsr4(0.5F);
        result.setFsr5(0.6F);
        result.setFsr6(0.75F);
        result.setFsr7(1.0F);
        return result;
    }

    @Override
    public EShoeStatus onConnectionStateChange(int newState) {
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                Log.i("VirtualEShoe", "Device " + name.toString() + " connected");
                mStatus = EShoeStatus.CONNECTED;
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                Log.i("VirtualEShoe", "Device " + name.toString() + " disconnect");
                mStatus = EShoeStatus.DISCONNECTED;
                break;
            default:
                Log.e("VirtualEShoe", "Device " + name.toString() + " Unknown new state " + newState);
        }
        return mStatus;
    }

    @Override
    public EShoeStatus getStatus() {
        return mStatus;
    }

    @Override
    public String getNameString() {
        return name.toString();
    }

    public static List<EShoe> generateSampleList(int size){
        List<EShoe> result = new ArrayList<>();
        while (result.size() < size){
            EShoe eshoe = new VirtualEShoe();
            result.add(eshoe);
        }
        return result;
    }
}
