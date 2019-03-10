package es.joapingut.eshoe.dto;

public interface EShoe {

    enum EShoeDataType {
        DT_OK, DT_NOK, DT_PING, DT_DIME, DT_FSR, DT_NO_RESPONSE;

        private static EShoeDataType[] values;

        public static EShoeDataType getFromOrdinal(int i){
            if (values == null){
                values = values();
            }
            return values[i];
        }
    }

    enum EShoeStatus {
        CONNECTED, DISCONNECTED, CONNECTING, WAITING
    }

    EShoeData getData();
    EShoeStatus onConnectionStateChange(int newState);
    EShoeStatus getStatus();
    String getNameString();
}
