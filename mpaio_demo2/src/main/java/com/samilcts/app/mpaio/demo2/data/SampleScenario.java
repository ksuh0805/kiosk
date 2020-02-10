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
    private ArrayList<String> ProductArray = new ArrayList<>();

    public void request(String urlStr){ // 웹서버에서 시나리오 정보 json 가져오기
        ProductArray.clear(); // 이전 시나리오 정보 초기화
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

    public ArrayList<String> getDemoScenario(Context context){ // 시나리오 정보 가져오기

        String serial = android.os.Build.SERIAL; // 기기 serial number 가져오기
        Log.d("serialnumber", serial);

        final String urlStr = String.format(context.getString(R.string.scenario_server), serial,
                context.getString(R.string.ID), context.getString(R.string.PWD)); //  기기별 scenario 웹서버 url

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
    public ArrayList Matching() { // scenario 내 유효한 상품번호만 가져오기
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
