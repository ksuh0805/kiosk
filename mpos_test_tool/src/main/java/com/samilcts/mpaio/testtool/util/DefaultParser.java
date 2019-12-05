package com.samilcts.mpaio.testtool.util;


import com.samilcts.mpaio.testtool.data.CardInfo;
import com.samilcts.mpaio.testtool.data.DeviceInfo;
import com.samilcts.util.android.Converter;

import java.util.ArrayList;

/**
 * Created by mskim on 2015-07-20.
 *
 * 명령에 대한 데이터의 파싱을 도와준다.
 */
class DefaultParser {

    /**
     * mpaio ble address prefix
     */
    private static final String PREFIX_BLE_ADDRESS = "Samil";


    private final String TAG = "DefaultParser";


    /**
     * 바코드 데이터를 파싱한다.
     * @param data 에러코드를 제외한 데이터
     * @return 바코드 (맨앞에 1바이트 타입 포함)
     */

    public String parseBarcodeData(byte[] data) {


        if ( data == null ) {
            return "";
        }

        String temp = new String(data);

        return temp.split("\r\n")[0];
    }

    /**
     * 핀코드 데이터를 파싱한다.
     * @param data 에러코드를 제외한 데이터
     * @return 핀코드를 반환한다.
     */

    public String parsePinCode(byte[] data) {

        return null == data ? "" : new String(data);

    }


    /**
     * 펌웨어 버전 데이터를 파싱한다.
     * @param data 에러코드를 제외한 데이터
     * @return 펌웨어 버전
     */
    public String parseFirmwareVersion(byte[] data) {

        return null == data ? "" : new String(data);
    }

    /**
     * mpaio ble 어드레스 파싱
     * @param data 에러코드를 제외한 데이터
     * @return ble 주소
     */

    public String parseBleAddress(byte[] data) {

        if (data == null) return "";

        String temp = new String(data);

        return temp.replaceAll(PREFIX_BLE_ADDRESS, "");
    }


    /**
     * 장치 정보 데이터 파싱
     * @param data 에러코드를 제외한 데이터
     * @return 장치정보
     */
    public DeviceInfo parseDeviceInfo(byte[] data) {

        DeviceInfo info = new DeviceInfo();

        if ( data == null ) return info;

        try {

            String temp = new String(data);
            String[] deviceInfo = temp.split("\\n");

            info.datetime = deviceInfo[0];
            info.serialNumber = deviceInfo[1];
            info.bluetoothAddress = deviceInfo[2];
            info.name = deviceInfo[3];
            info.model = deviceInfo[4];
            info.firmwareVersion = deviceInfo[5];
            info.hardwareRevision = deviceInfo[6];

        } catch (Exception e){

            e.printStackTrace();
        }


        return info;
    }


    /**
     * 날짜 데이터 파싱
     * @param data 에러코드를 제외한 데이터
     * @return 날짜
     */
    public String parseDate(byte[] data) {

        return data == null ? "" : new String(data);
    }


    /**
     * 배터리 데이터 파싱
     * @param data 에러코드를 제외한 데이터
     * @return 배터리 수준 (%단위)
     */
    public int parseBatteryData(byte[] data)  {

        String level = new String(data);

        int val = -1;

        try {

            val = Integer.parseInt(level);

        } catch (NumberFormatException e ) {
            e.printStackTrace();
        }

        return val;

    }

    /**
     * EMV카드 데이터 파싱
     * @param data 에러코드를 제외한 데이터
     * @return 카드정보
     */

    public CardInfo parseEMVCardData(byte[] data) {

        CardInfo cardInfo = new CardInfo();

        if ( data == null) return  cardInfo;

        String strData = new String(data);

        int idxStartDate = strData.indexOf('D');

        if ( idxStartDate > 0 && data.length >= idxStartDate ) {
            cardInfo.number = new String(data, 0, idxStartDate).replaceAll("[^0-9]","");
        }

        if ( idxStartDate > 0 && data.length >= 1 + idxStartDate+ CardInfo.LEN_DATE) {
            cardInfo.year = new String(data, idxStartDate+1, 2);
            cardInfo.month = new String(data, idxStartDate + 3, 2);
        }


        return cardInfo;
    }


    /**
     * MSR카드 데이터 파싱.
     * @param data 에러코드를 제외한 데이터
     * @return 카드정보
     */

    public CardInfo parseMSCardData(byte[] data) {


        CardInfo cardInfo = new CardInfo();

        if ( data == null) return  cardInfo;



        ArrayList<byte[]> trackList = Converter.split(data, (byte) 0x0D);


        String T1 = new String(trackList.get(0));//tracks[0];

        cardInfo.t1 = T1;

        int idxStartName = T1.indexOf('^');
        int idxStartDate = T1.lastIndexOf('^');

        if ( idxStartName > -1 && idxStartDate > idxStartName ) {

            cardInfo.name = T1.substring(idxStartName+1, idxStartDate).trim();
        }


        String T2 = new String(trackList.get(1));//tracks[1];
        cardInfo.t2 = T2;

        idxStartDate = T2.indexOf('=');

        if ( idxStartDate > 0 && T2.length() >= idxStartDate-1 ) {

            cardInfo.number = T2.substring(0, idxStartDate).replaceAll("[^0-9]","");

        }



        if ( idxStartDate > -1 && T2.length() >= idxStartDate + CardInfo.LEN_DATE ) {

            cardInfo.year = T2.substring(idxStartDate+1, idxStartDate+3);
            cardInfo.month = T2.substring(idxStartDate+3, idxStartDate+5);
        }


        if ( trackList.size() > 2 ) {
            cardInfo.t3 = new String (trackList.get(2));
        }

        return cardInfo;
    }

    /**
     *
     * RFID카드 데이터 파싱. 임시..
     * @param data 에러코드를 제외한 데이터
     * @return 카드정보
     *
     */

    public CardInfo parseRFIDCardData(byte[] data) {

        CardInfo cardInfo = new CardInfo();

        if ( data == null)
            return cardInfo;

        cardInfo.number = new String(data);

        return cardInfo;
    }




}
