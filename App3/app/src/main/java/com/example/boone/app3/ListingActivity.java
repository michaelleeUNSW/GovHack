package com.example.boone.app3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ListingActivity extends AppCompatActivity {
    private LinearLayout.LayoutParams lp;
    private LinearLayout parent, bottom;

    private EditText name;
    private EditText price;
    private EditText ISBN;
    private EditText condition;
    private EditText description;
    public String imageUrl = null;
    private String RuName;
    private Ebay_Authorisation_Code_API AuthListingWrapper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        RuName = getString(R.string.RuName);
        AuthListingWrapper = createAuthListingWrapper();
        name = (EditText) findViewById(R.id.name);
        price = (EditText) findViewById(R.id.Price);
        ISBN = (EditText) findViewById(R.id.isbn);
        condition = (EditText) findViewById(R.id.condition);
        description = (EditText) findViewById(R.id.description);

        Bundle extras = getIntent().getExtras();
        populateWithBookAPIData(extras);

        Log.d("Testing_","start");
        this.setTitle("Product Details");

    }

    public Ebay_Authorisation_Code_API createAuthListingWrapper() {

        return new Ebay_Authorisation_Code_API(this, RuName) {
            @Override
            void onResponse(String accessToken) {
                String ItemName = name.getText().toString();
                String ItemPrice = price.getText().toString();
                String ItemISBN = ISBN.getText().toString();
                String ItemCondition = condition.getText().toString();
                String ItemDescription = description.getText().toString();

                if (!(ItemName.isEmpty()) && !(ItemPrice.isEmpty()) &&
                        !(ItemISBN.isEmpty()) && !(ItemCondition.isEmpty()) &&
                        !(ItemDescription.isEmpty())) {
                    ListingHelper list_on_ebay = new ListingHelper(
                            ListingActivity.this, accessToken, ItemName, ItemISBN, ItemDescription, ItemPrice, imageUrl, "LA2");
                    //add imageUrl

                    list_on_ebay.createInventoryItem();
                } else {
                    ListingActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(ListingActivity.this, "Not All Details Are Filled", Toast.LENGTH_LONG).show();
                        }
                    });
                    //Toast.makeText(getApplicationContext(), "Not All Details Are Filled", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    private long mLastClickTime = 0;
    public void Listing(View view) {
        // double-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 4000){return;}
        mLastClickTime = SystemClock.elapsedRealtime();

        updateExtra();
        AuthListingWrapper.runWithAuth();
    }



    public void populateWithBookAPIData(Bundle extras){
        Log.d("testing5","A");
        if(extras != null) {
            if(extras.getString("ISBN") != null){
                Log.d("testing5","B");
                String ItemISBN = extras.getString("ISBN");
                new GetBookInfo(this, ItemISBN).execute();
                if (ItemISBN != null) {ISBN.setText(ItemISBN);};
            }
        }
    }


    //Reload data after coming back from ebay authentication

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String MY_PREFS_NAME = "RestoreListing";
    public void updateExtra(){
        prefs = this.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();
        editor.putString("Name",name.getText().toString());
        editor.putString("Price",price.getText().toString());
        editor.putString("Condition",condition.getText().toString());
        editor.putString("ISBN",ISBN.getText().toString());
        editor.putString("Description",description.getText().toString());
        editor.putString("ImageUrl",imageUrl);
        editor.apply();
    }

    public void repopulateWithBookAPIData(SharedPreferences extras){
        String ItemName = extras.getString("Name",null);
        Log.d("testing1001",ItemName);
        String ItemPrice = extras.getString("Price",null);
        String ItemCondition = extras.getString("Condition",null);
        String ItemISBN = extras.getString("ISBN",null);
        String ItemDescription = extras.getString("Description",null);
        imageUrl = extras.getString("ImageUrl",null);
        if (ItemName != null) {name.setText(ItemName);};
        if (ItemPrice != null) {price.setText(ItemPrice);};
        if (ItemCondition != null) {condition.setText(ItemCondition);};
        if (ItemISBN != null) {
            Log.d("testing5","C");
            ISBN.setText(ItemISBN);};
        if (ItemDescription != null) {description.setText(ItemDescription);};
        if (imageUrl != null){
            imageUrl = imageUrl.replace("http://", "https://");
            ImageView imageView = (ImageView) findViewById(R.id.coverImage);
            Picasso.get().load(imageUrl).fit().into(imageView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        try{
            String state = intent.getData().getQueryParameter("state");
            if (state.equals(RuName)) {
                Log.d("testing","a");
                prefs = this.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                Log.d("testing",prefs.toString());
                String ItemName = prefs.getString("Name",null);
                if(ItemName == null){
                    finish();
                }
                repopulateWithBookAPIData(prefs);
            }
        }catch (Exception e){}
        AuthListingWrapper.resumeWithAuth(intent);
        //TODO load info back
    }


    //Clear all button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id== R.id.clear){
            new AlertDialog.Builder(ListingActivity.this)
                    .setTitle("Clear All Input")
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            recreate();

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
