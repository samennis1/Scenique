package com.sam.scenique_app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

public class AboutUsActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_about_us);
//
//        Toolbar toolbar = findViewById(R.id.about_us_toolbar);
//        setSupportActionBar(toolbar);
//        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        //getSupportActionBar().setDisplayShowHomeEnabled(true);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
