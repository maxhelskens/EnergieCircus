package com.example.max.energiecircus;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void classRegistration(View v)
    {
        Intent showActivity = new Intent(this, RegistrationActivity.class);
        startActivity(showActivity);

    }

    public void loadWebsite(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://thinkcore.be/project/caves-energieke-scholen"));
        startActivity(browserIntent);
    }

}
