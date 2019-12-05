package com.samilcts.app.mpaio.demo2.data;

import java.io.Serializable;

/**
 * Created by mskim on 2015-07-20.
 *
 * 카드 정보 데이터 타입 클래스
 */
public class CardInfo implements Serializable{

    public static final int LEN_DATE = 4;

    public String number = "";
    public String year = "";
    public String month = "";
    public String name = "";


    public String t1 = "";
    public String t2 = "";
    public String t3 = "";

    @Override
    public String toString() {

        return "Card number : " + number.replaceAll("\\d{4}", "$0-").replaceAll("\\-$","") + "\n"
                + "Valid Date : " + month+"/"+year +"\n"
                + "Name : " + name + "\n"
                + "T1 : " + t1 + "\n"
                + "T2 : " + t2 + "\n"
                + "T3 : " + t3;

    }
}
