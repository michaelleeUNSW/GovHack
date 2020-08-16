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

public class GetBookInfo extends AsyncTask<String, Object, String[]> {
    private String barcodeValue;
    private ListingActivity currActivity;

    public GetBookInfo(ListingActivity curr, String bar){
        currActivity = curr;
        barcodeValue = bar;
    }

    protected String[] doInBackground(String... bookISBN) {
        // Stop if cancelled
        if(isCancelled()){
            return null;
        }

        try {
            Document doc = Jsoup.connect("https://encore.slwa.wa.gov.au/iii/encore/search/C__S"+barcodeValue+"__Orightresult__U?lang=eng&suite=def").get();
            Elements results = doc.select("#recordDisplayLink2Component");
            if (results.size()==0){
                Toast("Not Book in Library database with this ISBN, checking the Google Book API",Toast.LENGTH_LONG);
                return GoogleBookAPISearch(barcodeValue);
            }else{
                String link = results.first().absUrl("href");
                return WaStateLibrarySearch(link);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("testing1",e.toString());
        }
        return null;
    }


    private String[] WaStateLibrarySearch(String link) throws IOException {
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

        String ImageUrl = doc2.select(".noLinkImage").select("img").attr("src");

        String[] result = new String[3];
        result[0] = Name;
        result[1] = Description;
        result[2] = ImageUrl;
        return result;
    }

    private String[] GoogleBookAPISearch(String barcodeValue){

        String apiUrlString = "https://www.googleapis.com/books/v1/volumes?q=isbn:" +barcodeValue;

        try{
            HttpURLConnection connection = null;
            // Build Connection.
            try{
                URL url = new URL(apiUrlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(5000); // 5 seconds
                connection.setConnectTimeout(5000); // 5 seconds
            } catch (MalformedURLException e) {
                // Impossible: The only two URLs used in the app are taken from string resources.
                e.printStackTrace();
            } catch (ProtocolException e) {
                // Impossible: "GET" is a perfectly valid request method.
                e.printStackTrace();
            }
            int responseCode = connection.getResponseCode();
            Log.d(getClass().getName(), "Response CODE: " + responseCode);
            if(responseCode != 200){
                Log.w(getClass().getName(), "GoogleBooksAPI request failed. Response Code: " + responseCode);
                connection.disconnect();
                return null;
            }

            // Read data from response.
            StringBuilder builder = new StringBuilder();
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = responseReader.readLine();
            while (line != null){
                builder.append(line);
                line = responseReader.readLine();
            }
            String responseString = builder.toString();
//                Log.d(getClass().getName(), "Response String: " + responseString);
            JSONObject responseJson = new JSONObject(responseString);
            connection.disconnect();

            if(responseJson.getInt("totalItems") < 1){
                Toast("Book NOT Found!",Toast.LENGTH_SHORT);
                return null;
            }

            String[] result = new String[3];
            JSONObject bookDetails = responseJson.getJSONArray("items").getJSONObject(0);
            result[0] = bookDetails.getJSONObject("volumeInfo").getString("title");
            result[1] = bookDetails.getJSONObject("volumeInfo").getString("description");
            result[2] = bookDetails.getJSONObject("volumeInfo").getJSONObject("imageLinks").getString("thumbnail");
            return result;
        } catch (Exception e){
            Log.d(getClass().getName(), "Exception Occurred: " + e.toString());
            e.printStackTrace();
            return null;
        }

    }
    @Override
    protected void onPostExecute(String[] result) {

        EditText name = (EditText) currActivity.findViewById(R.id.name);
        EditText description = (EditText) currActivity.findViewById(R.id.description);
        ImageView imageView = (ImageView) currActivity.findViewById(R.id.coverImage);
        EditText price = currActivity.findViewById(R.id.Price);
        if (result[0] != null) {name.setText(result[0]); price.setText("$10");};
        if (result[1] != null) {description.setText(result[1]);};
        if (result[2] != null) {
            result[2] = result[2].replace("http://", "https://");
            Picasso.get().load(result[2]).fit().into(imageView);
            currActivity.imageUrl = result[2];
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