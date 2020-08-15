package com.example.boone.app3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Request;
import okhttp3.Response;

public class DeleteActivity extends AppCompatActivity {

    private String ISBN = null;
    private String merchantKey = null;
    public String title = null;
    public String offerID = null;
    private DeleteHelper deleteHelper;
    private String RuName;
    private Ebay_Authorisation_Code_API AuthSearchingWrapper;
    private Ebay_Authorisation_Code_API AuthDeletingWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        RuName = getString(R.string.RuName);
        AuthSearchingWrapper = createAuthSearchingWrapper();
        AuthDeletingWrapper = createAuthDeletingWrapper();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            ISBN = extras.getString("ISBN");
            merchantKey = extras.getString("merchantKey");
            if(ISBN != null && merchantKey != null){
                deleteHelper = new DeleteHelper(this,ISBN,merchantKey);
            }
        }

        this.setTitle("Checking Out");
        AuthSearchingWrapper.runWithAuth();
    }

    private long mLastClickTime = 0;
    public void withdraw(View view) {
        // double-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000){return;}
        mLastClickTime = SystemClock.elapsedRealtime();
        AuthDeletingWrapper.runWithAuth();


    }

    public Ebay_Authorisation_Code_API createAuthSearchingWrapper (){
        return new Ebay_Authorisation_Code_API(this,RuName) {
            @Override
            void onResponse(String accessToken) {
                deleteHelper.getTitle(accessToken);
                deleteHelper.getOffers(accessToken);
            }
        };
    }


    public Ebay_Authorisation_Code_API createAuthDeletingWrapper(){
        return new Ebay_Authorisation_Code_API(this,RuName) {
            @Override
            void onResponse(String accessToken) {
                if(offerID != null){
                    deleteHelper.withdrawOffers(accessToken,offerID);
                    deleteHelper.deleteInventory(accessToken,offerID);
                    try{Thread.sleep(2000);}catch (InterruptedException e){Log.d("testing","failed");};
                    finish();
                }
            }
        };
    }

    public void finish(View view) {
        finish();
    }

}
