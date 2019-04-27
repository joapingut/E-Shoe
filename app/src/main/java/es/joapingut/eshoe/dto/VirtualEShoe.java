package es.joapingut.eshoe.dto;

import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VirtualEShoe implements EShoe {

    private static final float[][] demoValues = {
            {0.8F,0.8F},{0.8F,0.65F},{0.9F,0.45F},{0.75F,0.30F},{0.65F,0.0F},{0.50F,0.0F},{0.35F,0.0F},{0.25F,0.0F},
            {0.0F,0.0F},{0.0F,0.0F},{0.0F,0.0F},{0.0F,0.20F},{0.0F,0.35F},{0.15F,0.50F},{0.25F,0.65F},{0.35F,0.70F},
            {0.50F,0.75F},{0.65F,0.8F},{0.70F,0.8F},{0.76F,0.8F},{0.8F,0.8F},{0.75F,0.8F},{0.77F,0.8F},{0.8F,0.8F}
    };

    private UUID name;

    private EShoeStatus mStatus;

    private boolean pronator;

    private float randomSeed;

    private int multiplier;

    private int step;

    private int looper;

    public VirtualEShoe(){
        name = UUID.randomUUID();
        mStatus = EShoeStatus.CONNECTING;
        step = -1;
        randomSeed = (float) (0.9F + Math.random() * (1F - 0.9F));
        pronator = Math.random() < 0.5;
        multiplier = 8;
        looper = -1;
    }

    @Override
    public EShoeData getData() {
        looper += 1;
        if (looper % multiplier == 0){
            step += 1;
            looper = 1;
        }
        return nextStep();
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

    private EShoeData nextStep(){
        if (step >= demoValues.length){
            step = 0;
        }
        float upper = demoValues[this.step][0];
        float down = demoValues[this.step][1];

        EShoeData result = new EShoeData();
        result.setType(EShoeDataType.DT_DIME);
        result.setFsr1(down);

        float per2 = 0.96F;
        float per3 = 1F;
        float per4 = 0.97F;

        float per5 = 0.96F;
        float per6 = 0.94F;
        float per7 = 0.95F;

        if (pronator){
            result.setFsr2(per2 * upper * randomSeed);
            result.setFsr3(per3 * upper * randomSeed);
            result.setFsr4(per4 * upper * randomSeed);

            result.setFsr5(per5 * upper * randomSeed);
            result.setFsr6(per6 * upper * randomSeed);
            result.setFsr7(per7 * upper * randomSeed);
        } else {
            result.setFsr6(per2 * upper * randomSeed);
            result.setFsr5(per3 * upper * randomSeed);
            result.setFsr7(per4 * upper * randomSeed);

            result.setFsr4(per5 * upper * randomSeed);
            result.setFsr2(per6 * upper * randomSeed);
            result.setFsr4(per7 * upper * randomSeed);
        }
        return result;
    }
}
