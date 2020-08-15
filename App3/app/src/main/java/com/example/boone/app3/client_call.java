package com.example.boone.app3;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class client_call {
    private String token;

    client_call(String T) {
        token = T;
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();


        Request.Builder RB = new Request.Builder();

        try {
            Request request = authorisation(RB).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("testing_", "Error: " + e.getMessage());
                    Log.d("testing_", "Error: " + e.toString());
                    OnFailure();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    JSONObject data = null;
                    try {
                        String json = response.body().string();
                        data = new JSONObject(json);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    OnResponse(data,  response);

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public Request.Builder authorisation(Request.Builder RB) throws JSONException{
        return JsonRequest(RB.addHeader("Content-Language", "en-US")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", TextUtils.htmlEncode("Bearer " + token)));
    }
    abstract void OnResponse(JSONObject data, Response response);
    public void OnFailure(){};
    public Request.Builder JsonRequest(Request.Builder RB) throws JSONException{return RB;};
    /*Sample Body
        JSONObject jsonObject = new JSONObject();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        return RB.url("https://api.sandbox.ebay.com/sell/inventory/v1/inventory_item/water-bottle")
                .put(body);
     */

}
