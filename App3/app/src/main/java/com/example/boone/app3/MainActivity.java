package com.example.boone.app3;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    String merchantKey = "LA2";
    //Variables
    private LinearLayout.LayoutParams lp;
    private LinearLayout parent, bottom;

    //Function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Testing_", "start");
        String RuName = getString(R.string.RuName);

        Log.d("Testing_", RuName);
        this.setTitle("Product Details");

        int PERMISSION_REQUEST_CODE = 200;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }

        try {
            CovidWarning();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private int ISBN_FOR_LISTING = 201;
    private String RuName = "Michael_Lee-MichaelL-Testin-lcfyfuf";

    public void listingWithBarcode(View view) {
        Intent intent = new Intent(MainActivity.this, ISBNScanner.class);
        startActivityForResult(intent, ISBN_FOR_LISTING);
    }

    public void listingWithoutBarcode(View view) {
        Intent intent = new Intent(MainActivity.this, ListingActivity.class);
        startActivity(intent);
    }

    private int ISBN_FOR_DELETING = 202;

    public void deleteWithBarcode(View view) {
        Intent intent = new Intent(MainActivity.this, ISBNScanner.class);
        startActivityForResult(intent, ISBN_FOR_DELETING);

    }

    public void deleteWithoutBarcode(View view) {
        Intent intent = new Intent(MainActivity.this, DeleteActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ISBN_FOR_LISTING && resultCode == RESULT_OK) {
            String ItemISBN = data.getStringExtra("ISBN");
            Intent intent = new Intent(MainActivity.this, ListingActivity.class);
            intent.putExtra("ISBN", ItemISBN);
            startActivity(intent);

        } else if (requestCode == ISBN_FOR_DELETING && resultCode == RESULT_OK) {
            String ItemISBN = data.getStringExtra("ISBN");
            Intent intent = new Intent(MainActivity.this, DeleteActivity.class);
            intent.putExtra("ISBN", ItemISBN);
            intent.putExtra("merchantKey", merchantKey);
            startActivity(intent);

        }


    }

    public void CovidWarning() throws IOException {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        String postcode = addresses.get(0).getPostalCode();

        TextView warning = findViewById(R.id.warning);
        if(postcode.substring(0,1).equals("2")){
            try {
                getNumActiveCases(postcode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            warning.setText("⚠ The COVID warning system is unavailable at postcode "+postcode+" \n(outside of NSW)");
        }
        Log.d("testing1",postcode);
    }

    private void getNumActiveCases(final String postcode) throws IOException, JSONException {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... arg) {
                String apiUrlString = "https://data.nsw.gov.au/data/api/3/action/datastore_search?resource_id=5200e552-0afb-4bde-b20f-f8dd4feff3d7";
                int numCase = 0;
                try {
                    HttpURLConnection connection = null;
                        URL url = new URL(apiUrlString);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setReadTimeout(5000); // 5 seconds
                        connection.setConnectTimeout(5000); // 5 seconds
                    int responseCode = connection.getResponseCode();
                    Log.d(getClass().getName(), "Response CODE: " + responseCode);
                    if (responseCode != 200) {
                        connection.disconnect();
                        throw new IOException("API failed. Response Code: " + responseCode);
                    }

                    // Read data from response.
                    StringBuilder builder = new StringBuilder();
                    BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = responseReader.readLine();
                    while (line != null) {
                        builder.append(line);
                        line = responseReader.readLine();
                    }
                    String responseString = builder.toString();
                    JSONObject responseJson = new JSONObject(responseString);
                    connection.disconnect();

                    JSONArray records = responseJson.getJSONObject("result").getJSONArray("records");
                    for (int i = 0; i < records.length(); i++) {
                        JSONObject record = records.getJSONObject(i);
                        String address = record.getString("Address");
                        Log.d("testing1",address);
                        String postcode2 = address.substring(address.length() - 4);
                        if (postcode.equals(postcode2)) {
                            numCase++;
                        }
                    }
                    } catch (Exception e) {
                        // Impossible: "GET" is a perfectly valid request method.
                        e.printStackTrace();
                    }
                return numCase;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                TextView warning = findViewById(R.id.warning);
                TextView preparedness = findViewById(R.id.preparedness);
                String numCase = integer.toString();
                String p = String.valueOf(postcode);
                warning.setText("⚠ There are "+numCase+" active COVID case(s) in your postcode of "+p);
                if(integer>0){
                    preparedness.setText("BE PREPARED TO CLOSE!");
                }else{
                    preparedness.setText("It's safe to keep the store open.");
                }
            }
        }.execute();
    }
}


