package com.samilcts.app.mpaio.demo2.data;

import java.io.Serializable;

/**
 * Created by mskim on 2015-07-20.
 * 장치 정보 데이터 타입 클래스
 */
public class DeviceInfo implements Serializable {



    public String datetime = "";
    public String serialNumber = "";
    public String bluetoothAddress ="";
    public String name = "";
    public String model = "";
    public String firmwareVersion = "";
    public String hardwareRevision = "";



    @Override
    public String toString() {

        return datetime +"/" + serialNumber + "/" +bluetoothAddress + "/" +name + "/" +model + "/"+firmwareVersion + "/" + hardwareRevision;
    }
}
