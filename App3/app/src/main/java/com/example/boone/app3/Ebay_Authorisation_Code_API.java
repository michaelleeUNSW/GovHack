package com.example.boone.app3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public abstract class Ebay_Authorisation_Code_API {

    private Activity Activity;
    private String clientSecret;
    private String clientId;
    private String auth;
    private String authentication;
    private String RuName;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String MY_PREFS_NAME = "MyPrefsFile";


    public Ebay_Authorisation_Code_API(Activity A,String ruName) {
        Activity = A;
        RuName = ruName;
        clientSecret =  Activity.getString(R.string.clientSecret);
        clientId = Activity.getString(R.string.clientId);
        auth = clientId + ":" + clientSecret;
        authentication = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
    }
    abstract void onResponse(String accessToken);

    //Temp
    private String sku = "SA10";
    private String merchantLocationKey = "LA1";

    public void runWithAuth() {
        prefs = Activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();
        if(prefs.getLong("ClientExpire", 0) - 300 * 1000 > System.currentTimeMillis() && prefs.contains("ClientToken")) {
            onResponse(prefs.getString("ClientToken",null));  //Pass - If the user token hasn't expired
        } else {
            String OAuthURL = Activity.getString(R.string.OAuthURL);
            //If the user token is expired
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority(OAuthURL)
                    .appendPath("oauth2")
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", clientId)
                    .appendQueryParameter("redirect_uri", RuName)
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("state", RuName)
                    .appendQueryParameter("scope", "https://api.ebay.com/oauth/api_scope https://api.ebay.com/oauth/api_scope/sell.inventory https://api.ebay.com/oauth/api_scope/sell.account")
                    .appendQueryParameter("prompt", "login");
            String myUrl = builder.build().toString();

            Log.d("testing", Uri.parse(myUrl).toString());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myUrl));
            Activity.startActivity(intent);
        }
    }

    public void resumeWithAuth(Intent intent) {
        if (intent != null){
            if(intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                    Uri uri = intent.getData();
                    Log.d("Testing_return_url", uri.toString());

                    if (uri.getQueryParameter("error") != null) {
                        String error = uri.getQueryParameter("error");
                        Log.e("Testing_", "An error has occurred : " + error);
                    } else {
                        String state = uri.getQueryParameter("state");
                        if (state.equals(RuName)) {
                            String code = uri.getQueryParameter("code");
                            Log.d("Testing_code", code);
                            ExchangeAuthorisationForToken(code);
                        }
                    }
                }
            }
        }
    }

    private void ExchangeAuthorisationForToken(final String code) {
        new client_call(code) {
            @Override
            void OnResponse(JSONObject data, Response response) {
                Log.d("testing_EAC",data.toString());
                prefs = Activity.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                editor = prefs.edit();
                String accessToken = data.optString("access_token");
                int  access_expires = data.optInt("expires_in");
                editor.putLong("ClientExpire",System.currentTimeMillis()+access_expires*1000);
                editor.putString("ClientToken",accessToken);

                String refreshToken = data.optString("refresh_token");
                int  refresh_expires = data.optInt("refresh_token_expires_in");
                editor.putLong("RefreshExpire",System.currentTimeMillis()+refresh_expires*1000);
                editor.putString("RefreshToken",refreshToken);
                editor.apply();

                onResponse(accessToken);
            }

            @Override
            public Request.Builder authorisation(Request.Builder RB) throws JSONException {
                String authorisationURL = Activity.getString(R.string.authorisationURL);
                return RB
                    .addHeader("Authorization", "Basic " + authentication)
                    .url(authorisationURL)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                            "grant_type=authorization_code&code=" + code +
                                    "&redirect_uri=" + RuName));

            }
        };
    }

}
