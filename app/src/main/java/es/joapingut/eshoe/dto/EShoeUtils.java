package es.joapingut.eshoe.dto;

import android.util.Log;

import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class EShoeUtils {

    private EShoeUtils(){
        // Private constructor
    }

    public static EShoe getVirtualTestDevice(){
        return new VirtualEShoe();
    }

    /*
    *
    * For learning proposes the data packets are programmatic generated but a more elegant way to
    * do it will be to have a lookup table with the raw bytes for each type.
    *
    */

    public static byte[] getDefaultForType(EShoe.EShoeDataType type) {
        switch (type){
            case DT_FSR:
                return getDefaultSimpleData(EShoe.EShoeDataType.DT_FSR);
            case DT_DIME:
                return getDefaultSimpleData(EShoe.EShoeDataType.DT_DIME);
            case DT_PING:
                return getDefaultPingData();
            default:
                return getDefaultNokData();
        }
    }

    /**
     *
     * @return
     */
    @Contract(pure = true)
    public static byte[] getDefaultPingData(){
        byte[] data = new byte[8];
        data[0] = 0x23;
        data[1] = 0x01;
        data[2] = 0x08;
        data[3] = 0x02;
        data[6] = (byte) 0xF5;
        data[7] = (byte) 0xF5;
        int checksum = calculateChecksum(data, 8);
        data[4] = (byte)((checksum & 0x0000FF00) >> 8); // CHECKSUM B
        data[5] = (byte)(checksum & 0x000000FF); // CHECKSUM A
        return data;
    }

    /**
     *
     * @return
     */
    @Contract(pure = true)
    public static byte[] getDefaultNokData(){
        return getDefaultSimpleData(EShoe.EShoeDataType.DT_NOK);
    }

    private static byte[] getDefaultSimpleData(EShoe.EShoeDataType type){
        byte[] data = new byte[8];
        data[0] = 0x23;
        data[1] = 0x00;
        data[2] = 0x08;
        data[3] = (byte) type.ordinal();
        data[6] = (byte) 0xF5;
        data[7] = (byte) 0xF5;
        int checksum = calculateChecksum(data, 8);
        data[4] = (byte)((checksum & 0x0000FF00) >> 8); // CHECKSUM B
        data[5] = (byte)(checksum & 0x000000FF); // CHECKSUM A
        return data;
    }

    public static EShoeData interpretData(byte[] buffer){
        EShoeData result = new EShoeData();
        result.setType(EShoe.EShoeDataType.DT_NO_RESPONSE);
        for (int i = 0; i < buffer.length; i++){
            byte c = buffer[i];
            if (c == (byte) 0x23){
                if (validateTam(buffer, i) && validateHeader(buffer, i)){
                    result.setType(EShoe.EShoeDataType.getFromOrdinal(buffer[3]));
                    result = extractData(buffer, i);
                    break;
                }
            }
        }
        return result;
    }

    private static EShoeData extractData(byte[] buffer, int i) {
        EShoeData result = new EShoeData();
        EShoe.EShoeDataType type = EShoe.EShoeDataType.getFromOrdinal(buffer[i + 3]);
        switch (type){
            case DT_OK:
                result.setType(EShoe.EShoeDataType.DT_OK);
                break;
            case DT_DIME:
                result.setType(EShoe.EShoeDataType.DT_DIME);
                putDataFromSingleByte(result, buffer, 4, 7);
                break;
            case DT_FSR:
                result.setType(EShoe.EShoeDataType.DT_FSR);
                putData(result, buffer, 4, 1);
                break;
            case DT_PING:
                result.setType(EShoe.EShoeDataType.DT_PING);
                break;
            default:
                result.setType(EShoe.EShoeDataType.DT_NOK);
                break;
        }
        return result;
    }

    private static EShoeData putData(EShoeData data, byte[] buffer, int offset, int numData){
        for (int i = 0; i < numData; i++){
            try{
                data.setData(i + 1, ByteBuffer.wrap(buffer, offset + 1 + (5 * i),4).order(ByteOrder.BIG_ENDIAN).getFloat());
            } catch (IndexOutOfBoundsException ex){
                Log.e("shet", "What", ex);
            }
        }
        return data;
    }

    private static EShoeData putDataFromSingleByte(EShoeData data, byte[] buffer, int offset, int numData){
        for (int i = 0; i < numData; i++){
            try{
                int value = byteToUnsigned(buffer[offset + i]);
                data.setData(i + 1, (value / 255F));
            } catch (IndexOutOfBoundsException ex){
                Log.e("shet", "What", ex);
            }
        }
        return data;
    }

    @Contract(pure = true)
    private static boolean validateHeader(byte[] buffer, int i) {
        if (buffer[i] == 35) {
            byte rtam = buffer[ i + 2];
            int tam = rtam < 0 ? (rtam & 0xFF) : rtam;
            if ((i + tam) <= buffer.length && buffer[(i + tam) - 1] == (byte)0xF5 && buffer[(i + tam) - 1] == buffer[(i + tam) - 2]) {
                int ch_upper = byteToUnsigned(buffer[(i + tam) - 4]);
                int ch_down = byteToUnsigned(buffer[(i + tam) - 3]);
                int checksum = ((ch_upper << 8) + ch_down);
                if (checksum == calculateChecksum(buffer, tam)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Contract(pure = true)
    private static boolean validateTam(byte[] buffer, int i) {
        if (i + 2 < buffer.length ){
            byte tam = buffer[i + 2];
            if (i + tam <= buffer.length){
                return true;
            }
        }
        return false;
    }

    @Contract(pure = true)
    private static int calculateChecksum(byte[] buffer, int tam) {
        int sum = 0;
        for (int i = 0; i < tam; i++) {
            if ((i == tam - 3) || (i == tam - 4)) {
                continue;
            } else {
                sum += byteToUnsigned(buffer[i]);
            }
        }
        return (sum % 0xFF);
    }

    @Contract(pure = true)
    public static int byteToUnsigned(byte sb){
        int ub = (sb & 0x7F);
        if (sb < 0){
            ub += 0x80;
        }
        return ub;
    }

    @Contract(pure = true)
    public static byte[] mergeByteArray(byte[] one, byte[] second){
        byte [] newOne =  new byte[one.length + second.length];
        for (int i = 0; i < one.length; i++){
            newOne[i] = one[i];
        }
        for (int i = 0; i < second.length; i ++){
            newOne[one.length + i] = second[i];
        }
        return newOne;
    }

    public static <T extends Comparable> T clipInRange(T number, T max, T min){
        T result;
        if (number.compareTo(max) > 0){
            result = max;
        } else if (number.compareTo(min) < 0){
            result = min;
        } else {
            result = number;
        }
        return result;
    }

    public static float extrapolate(float x, float inMin, float inMax, float outMin, float outMax){
        if (x > inMax){
            return outMax;
        } else if (x < inMin){
            return outMin;
        }
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
}
