package es.joapingut.eshoe.dto;

import java.io.Serializable;

public interface EShoe extends Serializable {

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

    enum EShoeFootPosition{
        UNKNOWN, PRONATION, NEUTRAL, SUPINATION
    }

    enum EShoeStepPhase {
        UNKNOWN, LANDING, REST, LIFT_UP, LIFT
    }

    EShoeData getData();
    EShoeStatus onConnectionStateChange(int newState);
    EShoeStatus getStatus();
    String getNameString();
}
