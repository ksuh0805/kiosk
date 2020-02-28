package com.samilcts.app.mpaio.demo2.data;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.samilcts.app.mpaio.demo2.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * 웹서버로부터 시나리오 정보 가져오기
 */

public class SampleScenario {
    /**
     * 상품리스트
     */
    private ArrayList<String> ProductArray = new ArrayList<>();

    /**
     * 웹서버에서 시나리오 정보 json 읽어와 상품리스트 업데이트
     * @param urlStr 웹서버 접속 url
     */
    public void request(String urlStr){

        /**
         * 이전 시나리오 정보 초기화
         */
        ProductArray.clear();
        String output = "";
        try{
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if(conn != null){
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                int resCode = conn.getResponseCode();
                Log.d("scenario rescode", String.valueOf(resCode));

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = null;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    output += line;
                }
                reader.close();
                conn.disconnect();
            }
            Log.d("jsonview", output);

            JSONArray jsonObject = new JSONArray(output);

            for(int i=0; i<jsonObject.length(); i++){
                Log.d("length", String.valueOf(jsonObject.length()));
                ProductArray.add(String.valueOf(jsonObject.get(i)));
                Log.d("add", String.valueOf(jsonObject.get(i)));
            }
        } catch (Exception ex) {
            Log.d("SampleScenarioError", "예외 발생 : " + ex.toString());
        }Log.d("plist", String.valueOf(ProductArray));
    }

    /**
     * 시나리오 정보 가져오기
     * @return 시나리오상 상품리스트
     */
    public ArrayList<String> getDemoScenario(Context context){

        /**
         * 기기 serial number
         */
        String serial = android.os.Build.SERIAL;
        Log.d("serialnumber", serial);

        /**
         * 기기별 scenario 웹서버 url
         */
        final String urlStr = String.format(context.getString(R.string.scenario_server), serial,
                context.getString(R.string.ID), context.getString(R.string.PWD));

        Log.d("url", urlStr);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                request(urlStr);
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return ProductArray;
    }

    /**
     * scenario 내 유효한 상품번호만 가져오기
     * @return 유효한(매칭된) 상품리스트
     */
    public ArrayList Matching() {
        ArrayList<Integer> productNum = new ArrayList<>();

        for (int i = 0; i < ProductArray.size(); i++) {
            if (Integer.parseInt(ProductArray.get(i)) != 0){
                Log.d("pl", String.valueOf(ProductArray.get(i)));
                productNum.add(i+1);
            }
        }
        return productNum;
    }
}
