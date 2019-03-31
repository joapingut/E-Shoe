package es.joapingut.eshoe.dto;

public class EShoeData {

    private EShoe.EShoeDataType type;

    private float POSITION_MARGIN = 0.01F;
    private float PHASE_MARGIN = 0.01F;

    private float fsr1;
    private float fsr2;
    private float fsr3;
    private float fsr4;
    private float fsr5;
    private float fsr6;
    private float fsr7;

    public void setData(int i, float data){
        switch (i){
            case 1:
                fsr1 = data;
                break;
            case 2:
                fsr2 = data;
                break;
            case 3:
                fsr3 = data;
                break;
            case 4:
                fsr4 = data;
                break;
            case 5:
                fsr5 = data;
                break;
            case 6:
                fsr6 = data;
                break;
            case 7:
                fsr7 = data;
                break;
            default:
                break;
        }
    }

    public float getData(int i){
        switch (i){
            case 1:
                return fsr1;
            case 2:
                return fsr2;
            case 3:
                return fsr3;
            case 4:
                return fsr4;
            case 5:
                return fsr5;
            case 6:
                return fsr6;
            case 7:
                return fsr7;
            default:
                return 0;
        }
    }

    public EShoe.EShoeStepPhase getStepPhase(){
        if (type != EShoe.EShoeDataType.DT_DIME){
            return EShoe.EShoeStepPhase.UNKNOWN;
        }
        boolean heelUp = false;
        boolean headUp = false;

        if (fsr1 < PHASE_MARGIN){
            heelUp = true;
        }

        if (fsr2 < PHASE_MARGIN && fsr3 < PHASE_MARGIN && fsr4 < PHASE_MARGIN && fsr5 < PHASE_MARGIN && fsr6 < PHASE_MARGIN && fsr7 < PHASE_MARGIN){
            headUp = true;
        }

        if (headUp && heelUp){
            return EShoe.EShoeStepPhase.LIFT;
        } else if (headUp){
            return EShoe.EShoeStepPhase.LANDING;
        } else if (heelUp){
            return EShoe.EShoeStepPhase.LIFT_UP;
        } else {
            return EShoe.EShoeStepPhase.REST;
        }
    }

    public EShoe.EShoeFootPosition getFootPosition(){
        if (type != EShoe.EShoeDataType.DT_DIME || getStepPhase() != EShoe.EShoeStepPhase.REST){
            return EShoe.EShoeFootPosition.UNKNOWN;
        }
        float rigth = (fsr5 + fsr6 + fsr7) / 3;
        float left = (fsr2 + fsr3 + fsr4) / 3;

        float diference = rigth - left;

        if (diference > POSITION_MARGIN){
            return EShoe.EShoeFootPosition.SUPINATION;
        } else if (diference < POSITION_MARGIN){
            return EShoe.EShoeFootPosition.PRONATION;
        } else {
            return EShoe.EShoeFootPosition.NEUTRAL;
        }
    }

    public float getFsr1() {
        return fsr1;
    }

    public void setFsr1(float fsr1) {
        this.fsr1 = fsr1;
    }

    public float getFsr2() {
        return fsr2;
    }

    public void setFsr2(float fsr2) {
        this.fsr2 = fsr2;
    }

    public float getFsr3() {
        return fsr3;
    }

    public void setFsr3(float fsr3) {
        this.fsr3 = fsr3;
    }

    public float getFsr4() {
        return fsr4;
    }

    public void setFsr4(float fsr4) {
        this.fsr4 = fsr4;
    }

    public float getFsr5() {
        return fsr5;
    }

    public void setFsr5(float fsr5) {
        this.fsr5 = fsr5;
    }

    public float getFsr6() {
        return fsr6;
    }

    public void setFsr6(float fsr6) {
        this.fsr6 = fsr6;
    }

    public float getFsr7() {
        return fsr7;
    }

    public void setFsr7(float fsr7) {
        this.fsr7 = fsr7;
    }

    public EShoe.EShoeDataType getType() {
        return type;
    }

    public void setType(EShoe.EShoeDataType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "EShoeData{" +
                "type=" + type +
                ", fsr1=" + fsr1 +
                ", fsr2=" + fsr2 +
                ", fsr3=" + fsr3 +
                ", fsr4=" + fsr4 +
                ", fsr5=" + fsr5 +
                ", fsr6=" + fsr6 +
                ", fsr7=" + fsr7 +
                '}';
    }
}
