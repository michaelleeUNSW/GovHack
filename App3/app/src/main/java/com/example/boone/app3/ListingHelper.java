package com.example.boone.app3;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class ListingHelper {

    private String accessToken;
    private String ItemName;
    private String ItemSku;
    private String ItemDescription;
    private String ItemPrice;
    private String merchantLocationKey;
    private String ItemImageUrl;
    private Activity activity;



    public ListingHelper(Activity a, String Token, String name, String sku, String description, String price, String imageUrl, String mKey) {
        activity = a;
        accessToken = Token;
        ItemName = name;
        ItemSku = sku;
        ItemDescription = description;
        ItemPrice = price.replace("$","");
        merchantLocationKey = mKey;
        ItemImageUrl = imageUrl;
    }


    public void createInventoryItem() {
        new client_call(accessToken) {
            @Override
            public Request.Builder JsonRequest(Request.Builder RB) throws JSONException {
                JSONObject jsonObject = new JSONObject();
                JSONObject availability = new JSONObject();
                JSONObject shipToLocationAvailability = new JSONObject();

                shipToLocationAvailability.put("quantity", 1);
                availability.put("shipToLocationAvailability", shipToLocationAvailability);
                jsonObject.put("availability", availability);
                jsonObject.put("condition", "NEW");

                JSONObject product = new JSONObject();
                product.put("title",ItemName);
                product.put("description",ItemDescription);
                if(ItemImageUrl!= null) {
                    JSONArray imageUrls = new JSONArray();
                    imageUrls.put(ItemImageUrl);
                    product.put("imageUrls",imageUrls); 
                }
                jsonObject.put("product", product);

                JSONObject weight = new JSONObject();
                weight.put("unit","KILOGRAM");
                weight.put("value","1");
                jsonObject.put("weight", weight);
                Log.d("testing_01", jsonObject.toString());


                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());
                Toast("Creating Inventory");
                String createInventoryItemURL = activity.getString(R.string.createInventoryItemURL);
                return RB.url(createInventoryItemURL+ItemSku)
                        .put(body);
            }

            @Override
            void OnResponse(JSONObject data, Response response) {
                Log.d("testing_01", response.toString());
                if (response.isSuccessful()){
                    //TODO if item inventory already exist, but new offer is needed
                    createOffer();
                }else{
                    Toast("Error With Inventory Creation");
                    Log.d("testing_01a", data.toString());
                }
            }
        };
    }



    public void createOffer() {
        new client_call(accessToken) {

            @Override
            public Request.Builder JsonRequest(Request.Builder RB) throws JSONException {
                String fulfillmentPolicyId = activity.getString(R.string.fulfillmentPolicyId);
                String paymentPolicyId = activity.getString(R.string.paymentPolicyId);
                String returnPolicyId = activity.getString(R.string.returnPolicyId);
                String createOfferURL = activity.getString(R.string.createOfferURL);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("sku", ItemSku);
                jsonObject.put("marketplaceId", "EBAY_US");
                jsonObject.put("format", "FIXED_PRICE");
                jsonObject.put("categoryId", "30120");
                JSONObject policy = new JSONObject();
                policy.put("fulfillmentPolicyId", fulfillmentPolicyId);
                policy.put("paymentPolicyId", paymentPolicyId);
                policy.put("returnPolicyId", returnPolicyId);
                jsonObject.put("listingPolicies",policy);
                JSONObject pricingSummary = new JSONObject();
                JSONObject price = new JSONObject();
                price.put("currency", "USD");
                price.put("value", ItemPrice);
                pricingSummary.put("price",price);
                jsonObject.put("merchantLocationKey",merchantLocationKey);
                jsonObject.put("pricingSummary",pricingSummary);

                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());


                return RB.url(createOfferURL)
                        .post(body);
            }

            @Override
            void OnResponse(JSONObject data, Response response) {
                Log.d("testing_03", response.toString());
                if (response.isSuccessful()){
                    Toast("Offer Is Created");
                    Log.d("testing_03a", data.toString());
                    publishOffer(data.optString("offerId"));
                }else{
                    Toast("Error With Offer Creation");
                }
            }
        };

    }


    public void publishOffer(final String id) {
        new client_call(accessToken) {

            @Override
            public Request.Builder JsonRequest(Request.Builder RB) throws JSONException {
                String publishOfferURL = activity.getString(R.string.publishOfferURL);
                return RB.url(publishOfferURL+id+"/publish")
                        .method("POST",RequestBody.create(null, ""));
            }

            @Override
            void OnResponse(JSONObject data, Response response) {
                if (response.isSuccessful()){
                    Toast("Item Is listed");
                    Log.d("testing_04a", data.toString());
                    activity.finish();
                }else{
                    Toast("Error With Listing");
                    Log.d("testing_04Ea", data.toString());
                }
            }
        };

    }
    private void Toast(final String message){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
