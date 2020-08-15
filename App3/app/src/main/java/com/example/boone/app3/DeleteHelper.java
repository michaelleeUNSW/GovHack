package com.example.boone.app3;

import android.app.Activity;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeleteHelper {

    private String merchantLocationKey;
    private DeleteActivity activity;

    //Temp

    private String ItemSku;

    public DeleteHelper(DeleteActivity a, String sku, String LocationKey) {
        activity = a;
        ItemSku = sku;
        merchantLocationKey = LocationKey;

    }

    public void getTitle(String accessToken) {
        new client_call(accessToken) {
            @Override
            public Request.Builder JsonRequest(Request.Builder RB) throws JSONException {
                String getTitleURL = activity.getString(R.string.getTitleURL);
                return RB.url(getTitleURL+ItemSku);
            }

            @Override
            void OnResponse(JSONObject data, Response response) {
                try {
                    JSONObject product = data.getJSONObject("product");
                    String title = product.getString("title");
                    TextView bookTitle = (TextView) activity.findViewById(R.id.title);
                    bookTitle.setText(title);
                    activity.title = title;
                } catch (Exception e){
                    terminateDueToError("Book Not Found");
                }
            }
        };

    }
    public void getOffers(String accessToken) {
        new client_call(accessToken) {

            @Override
            public Request.Builder JsonRequest(Request.Builder RB) throws JSONException {
                String getOffersURL = activity.getString(R.string.getOffersURL);
                return RB.url(getOffersURL+ItemSku);
            }

            @Override
            void OnResponse(JSONObject data, Response response) {
                if (!response.isSuccessful()) {
                    terminateDueToError("Error0 at selectOffers()");
                }

                try {
                    JSONArray arr = data.getJSONArray("offers");
                    if(arr.length() == 0){
                        terminateDueToError("No offer found");
                    }else if (arr.length() > 1){
                        terminateDueToError("Error1 at selectOffers()");
                    }
                    JSONObject offer =  arr.getJSONObject(0);
                    //Only let remove items listed from that store
                    String LKey = offer.getString("merchantLocationKey");
                    if( LKey.equals(merchantLocationKey)){
                        activity.offerID = offer.getString("offerId");
                        Toast("Found offer");
                    }else{
                        terminateDueToError("Error2 at selectOffers()");
                    }

                } catch (Exception e){
                    terminateDueToError("Error3 at selectOffers()");
                }
            }
        };

    }


    public void withdrawOffers(String accessToken,final String ID){
        new client_call(accessToken) {

            @Override
            public Request.Builder JsonRequest(Request.Builder RB) throws JSONException {
                Toast("Sent delete request");
                String withdrawOffersURL = activity.getString(R.string.withdrawOffersURL);
                return RB.url( withdrawOffersURL+ ID )
                        .method("DELETE", RequestBody.create(null, ""));
            }

            @Override
            void OnResponse(JSONObject data, Response response) {
                if (response.isSuccessful()){
                    Toast("Offer is deleted");
                }else{
                    Toast("Error With deleting offer");
                }

            }
        };
    }

    public void deleteInventory(String accessToken,final String ID){
        new client_call(accessToken) {

            @Override
            public Request.Builder JsonRequest(Request.Builder RB) throws JSONException {
                String deleteInventoryURL = activity.getString(R.string.deleteInventoryURL);
                return RB.url(deleteInventoryURL + ID )
                        .method("DELETE", RequestBody.create(null, ""));
            }

            @Override
            void OnResponse(JSONObject data, Response response) {
                if (response.isSuccessful()){
                    Toast("Inventory is deleted");
                }else{
                    Toast("Error With deleting inventory");
                }

            }
        };
    }
    private void terminateDueToError(final String message){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                activity.finish();

            }
        });
    }

    private void Toast(final String message){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
