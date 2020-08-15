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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

public class GetBookInfo extends AsyncTask<String, Object, String> {
    private String barcodeValue;
    private ListingActivity currActivity;

    public GetBookInfo(ListingActivity curr, String bar){
        currActivity = curr;
        barcodeValue = bar;
    }

    protected String doInBackground(String... bookISBN) {
        // Stop if cancelled
        String ImageUrl = "";
        if(isCancelled()){
            return null;
        }
        Log.d(getClass().getName(), "ISBN String: " + barcodeValue);

        Log.d("testing1",barcodeValue);

        try {
            Document doc = Jsoup.connect("https://encore.slwa.wa.gov.au/iii/encore/search/C__S"+barcodeValue+"__Orightresult__U?lang=eng&suite=def").get();
            Elements results = doc.select("#recordDisplayLink2Component");
            if (results.size()==0){
                Toast("Not Book in Library database with this ISBN",Toast.LENGTH_LONG);
                return null;
            }
            String link = results.first().absUrl("href");
            Document doc2 = Jsoup.connect(link).get();

            String Name = doc2.select("#bibTitle").text();
            int NameEnd = Name.indexOf("/");
            Name = Name.substring(0,NameEnd);

            String Description = "";
            Elements newsHeadlines = doc2.select("#bibInfoDetails").select("tr");
            for (Element headline : newsHeadlines) {
                if(headline.select(".bibInfoLabel").text().equals("Summary")){
                    Description = headline.select(".bibInfoData").text();
                }
            }

            ImageUrl = doc2.select(".noLinkImage").select("img").attr("src");

            EditText name = (EditText) currActivity.findViewById(R.id.name);
            EditText description = (EditText) currActivity.findViewById(R.id.description);
            if (Name != null) {name.setText(Name);};
            if (Description != null) {description.setText(Description);};

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("testing1",e.toString());
        }
        return ImageUrl;
    }


    @Override
    protected void onPostExecute(String ImageUrl) {
        ImageView imageView = (ImageView) currActivity.findViewById(R.id.coverImage);
        if (ImageUrl != null) {
            ImageUrl = ImageUrl.replace("http://", "https://");
            Picasso.get().load(ImageUrl).fit().into(imageView);
            currActivity.imageUrl = ImageUrl;
        }


    }

    private void Toast(final String message, final int duration){
        currActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(currActivity, message,duration).show();
            }
        });
    }

}