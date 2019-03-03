package es.joapingut.eshoe.dto;

public interface EShoe {


    enum EShoeStatus {
        CONNECTED, DISCONNECTED, CONNECTING, WAITING
    }

    EShoeStatus onConnectionStateChange(int newState);
    EShoeStatus getStatus();
    String getNameString();
}
