package com.example.boone.app3;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mindorks.paracamera.Camera;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
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


}


