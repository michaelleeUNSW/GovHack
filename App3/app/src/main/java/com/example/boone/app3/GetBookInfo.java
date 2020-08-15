package com.example.boone.app3;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class GetBookInfo extends AsyncTask<String, Object, JSONObject> {
    private String barcodeValue;
    private ListingActivity currActivity;

    public GetBookInfo(ListingActivity curr, String bar){
        currActivity = curr;
        barcodeValue = bar;
    }

    protected JSONObject doInBackground(String... bookISBN) {
        // Stop if cancelled
        if(isCancelled()){
            return null;
        }
        Log.d(getClass().getName(), "ISBN String: " + barcodeValue);

        try {
            String html ="https://stackoverflow.com/questions/2971155/what-is-the-fastest-way-to-scrape-html-webpage-in-android";
            Document doc = null;
            doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new InputSource(new StringReader(html)));
            XPathExpression xpath = null;
            xpath = XPathFactory.newInstance()
                    .newXPath().compile("//*[@id=\"answers-header\"]/div/div[1]/h2");
            String result = (String) xpath.evaluate(doc, XPathConstants.STRING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject responseJson) {

        if(isCancelled() || responseJson == null){
            // Request was cancelled due to no network connection.
            return;
        } else{
            responseJson.toString();
        }

        try {
            if(responseJson.getInt("totalItems") < 1){
                Toast("Book NOT Found!",Toast.LENGTH_SHORT);
            } else {
                String Name = "";
                String Description = "";
                String ImageUrl = "";

                EditText name = (EditText) currActivity.findViewById(R.id.name);
                EditText description = (EditText) currActivity.findViewById(R.id.description);
                ImageView imageView = (ImageView) currActivity.findViewById(R.id.coverImage);
                if (Name != null) {name.setText(Name);};
                if (Description != null) {description.setText(Description);};
                if (ImageUrl != null) {
                    ImageUrl = ImageUrl.replace("http://", "https://");
                    Picasso.get().load(ImageUrl).fit().into(imageView);
                    currActivity.imageUrl = ImageUrl;
                }
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;

    }
    private void Toast(final String message, final int duration){
        currActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(currActivity, message,duration).show();
            }
        });
    }

}