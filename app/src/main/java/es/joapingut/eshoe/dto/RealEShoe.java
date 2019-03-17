package es.joapingut.eshoe.dto;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class RealEShoe extends BluetoothGattCallback implements EShoe {

    public static final UUID SH_H8_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID SH_H8_RX_TX = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    public static final int MAX_RECON_NUMBER = 4;
    public static final int RECON_DELAY = 5000;
    public static final int WRITE_DELAY = 10;
    public static final int RESPONSE_DELAY = 200;
    public static final int BUFFER_TAM = 256;

    private BluetoothGatt mGatt;
    private Handler mHandler;

    private int reconNumber = 0;
    private EShoeStatus mStatus;

    private byte[] inputBuffer;
    private int lastByte;
    private Semaphore bufferSemaphore;

    public RealEShoe(Context context, Handler mHandler, @NonNull BluetoothDevice device){
        this.mHandler = mHandler;
        this.mStatus = EShoeStatus.CONNECTING;
        this.inputBuffer = new byte[BUFFER_TAM];
        this.lastByte = -1;
        this.bufferSemaphore = new Semaphore(1);
        this.mGatt = device.connectGatt(context, false, this, BluetoothDevice.TRANSPORT_LE);
        mGatt.connect();
    }

    public void queryForData(EShoeDataType type) throws InterruptedException {
        writeSerialString(EShoeUtils.getDefaultForType(type));
    }

    public EShoeData queryForResponse() throws InterruptedException {
        EShoeData result = new EShoeData();
        result.setType(EShoeDataType.DT_NO_RESPONSE);
        Thread.sleep(RESPONSE_DELAY);
        byte[] buffer = readFromBuffer();
        for (int i = 0; i < MAX_RECON_NUMBER; i++) {
            result = EShoeUtils.interpretData(buffer);
            if (result.getType() == EShoeDataType.DT_NO_RESPONSE) {
                Thread.sleep(RESPONSE_DELAY);
                buffer = EShoeUtils.mergeByteArray(buffer, readFromBuffer());
            } else {
                break;
            }
        }
        return result;
    }

    @Override
    public EShoeData getData() {
        EShoeData result = new EShoeData();
        result.setType(EShoeDataType.DT_NO_RESPONSE);
        try{
            queryForData(EShoeDataType.DT_DIME);
            result = queryForResponse();
        } catch (InterruptedException ex){
            Log.e("EShoe", "Error on getData", ex);
        }
        return result;
    }

    public EShoeStatus onConnectionStateChange(int newState){
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                Log.i("RealEShoe", "Device " + mGatt.getDevice().getName() + " connected");
                reconNumber = 0;
                mStatus = EShoeStatus.CONNECTED;
                mGatt.discoverServices();
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                if (mStatus == EShoeStatus.CONNECTING && reconNumber >= MAX_RECON_NUMBER){
                    Log.i("RealEShoe", "Device " + mGatt.getDevice().getName() + " cannot connect");
                    mStatus = EShoeStatus.DISCONNECTED;
                    mGatt.close();
                } else if (mStatus == EShoeStatus.CONNECTING) {
                    Log.i("RealEShoe", "Device " + mGatt.getDevice().getName() + " cannot connect retry " + reconNumber);
                    reconNumber += 1;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mGatt.connect();
                        }
                    }, RECON_DELAY);
                } else {
                    Log.i("RealEShoe", "Device " + mGatt.getDevice().getName() + " waiting status");
                    mStatus = EShoeStatus.WAITING;
                    mGatt.disconnect();
                }
                break;
            default:
                Log.e("RealEShoe", "Device " + mGatt.getDevice().getName() + " Unknown new state " + newState);
        }
        return mStatus;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        this.onConnectionStateChange(newState);
    }

    private void writeSerialString(byte[] command) throws InterruptedException {
        if (mGatt != null && this.getStatus() == EShoeStatus.CONNECTED){
            BluetoothGattCharacteristic chara = mGatt.getService(SH_H8_UUID).getCharacteristic(SH_H8_RX_TX);
            int size = 20;
            if (command.length > 20){
                for (int i = 0; i < command.length; i += size) {
                    chara.setValue(Arrays.copyOfRange(command, i, Math.min(command.length, i + size)));
                    mGatt.writeCharacteristic(chara);
                    Thread.sleep(WRITE_DELAY);
                }
            } else {
                for (int i = 0; i < command.length; i++ ){
                    chara.setValue(Arrays.copyOfRange(command, i, i + 1));
                    mGatt.writeCharacteristic(chara);
                    Thread.sleep(WRITE_DELAY);
                }
                /*
                chara.setValue(command);
                mGatt.writeCharacteristic(chara);
                Thread.sleep(WRITE_DELAY);
                chara.setValue("\n");
                mGatt.writeCharacteristic(chara);*/
            }
        }
    }

    private void writeToBuffer(byte[] newData) throws InterruptedException {
        bufferSemaphore.acquire();
        if (lastByte >= BUFFER_TAM) {
            Log.e("RealEShoe", "Device " + mGatt.getDevice().getName() + " Buffer complete");
            bufferSemaphore.release();
            return;
        }
        for (int i = 0; i < newData.length && lastByte < BUFFER_TAM; i++){
            lastByte += 1;
            inputBuffer[lastByte] = newData[i];
        }
        bufferSemaphore.release();
    }

    private byte[] readFromBuffer() throws InterruptedException {
        bufferSemaphore.acquire();
        if (lastByte < 0){
            bufferSemaphore.release();
            return new byte[0];
        }
        int size = lastByte + 1;
        lastByte = -1;
        byte[] read = Arrays.copyOf(inputBuffer, size);
        bufferSemaphore.release();
        return read;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d("RealEShoe", "Device " + mGatt.getDevice().getName() + " read value: " + new String(characteristic.getValue()));
        try {
            writeToBuffer(characteristic.getValue());
        } catch (InterruptedException ex){
            Log.e("RealEShoe", "Device " + mGatt.getDevice().getName() + " error writing to buffer", ex);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        gatt.setCharacteristicNotification(gatt.getService(SH_H8_UUID).getCharacteristic(SH_H8_RX_TX), true);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        Log.i("onCharacteristicWrite", "onCharacteristicWrite " + characteristic.getUuid().toString());

        if (SH_H8_RX_TX.equals(characteristic.getUuid())) {
            Log.d("onCharacteristicWrite", "Received data RX: " + characteristic.getStringValue(0));
        }
    }

    public BluetoothGatt getmGatt() {
        return mGatt;
    }

    public void setmGatt(BluetoothGatt mGatt) {
        this.mGatt = mGatt;
    }

    public EShoeStatus getStatus() {
        return mStatus;
    }

    @Override
    public String getNameString() {
        return mGatt.getDevice().getName();
    }

    public void setStatus(EShoeStatus mStatus) {
        this.mStatus = mStatus;
    }
}
