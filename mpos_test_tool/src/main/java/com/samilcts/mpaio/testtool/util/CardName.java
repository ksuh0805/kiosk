package com.samilcts.mpaio.testtool.util;

import java.util.HashMap;

/**
 * Created by mskim on 2016-06-28.
 * mskim@31cts.com
 */
public class CardName {

    private final static HashMap<String, String> cardName = new HashMap<>();

    static  {
        cardName.put("01", "BC카드");
        cardName.put("13", "수협카드");
        cardName.put("23", "전북카드");
        cardName.put("12", "시티카드");
        cardName.put("16", "시티카드");
        cardName.put("39", "시티카드");
        cardName.put("15", "우리카드");
        cardName.put("29", "하나카드");
        cardName.put("07", "LG카드");
        cardName.put("03", "외환카드");
        cardName.put("06", "삼성카드");
        cardName.put("08", "현대카드");
        cardName.put("38", "롯데비자카드");
        cardName.put("33", "AMX카드");
        cardName.put("31", "JCB카드");
        cardName.put("32", "다이너스");
        cardName.put("34", "비자");
        cardName.put("35", "마스터");
        cardName.put("02", "국민카드");
        cardName.put("11", "NH카드");
        cardName.put("05", "신한카드");
        cardName.put("21", "제주비자");
        cardName.put("22", "광주비자");

    }

    public static String getCardName(String code) {

        String name = cardName.get(code);

        return name != null ? name : "";
    }

}
