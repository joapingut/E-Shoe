package es.joapingut.eshoe.dto;

import java.io.Serializable;

import es.joapingut.eshoe.R;

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
        UNKNOWN(R.string.lbl_unknown), PRONATION(R.string.lbl_pronation), NEUTRAL(R.string.lbl_neutral), SUPINATION(R.string.lbl_supination);

        private int id;

        EShoeFootPosition(int id){
            this.id = id;
        }

        public int getId(){return id;}
    }

    enum EShoeStepPhase {
        UNKNOWN(R.string.lbl_unknown), LANDING(R.string.lbl_landing), REST(R.string.lbl_rest), LIFT_UP(R.string.lbl_lift_up), LIFT(R.string.lbl_lift);

        private int id;

        EShoeStepPhase(int id){
            this.id = id;
        }

        public int getId(){return id;}
    }

    EShoeData getData();
    EShoeStatus onConnectionStateChange(int newState);
    EShoeStatus getStatus();
    String getNameString();
}
