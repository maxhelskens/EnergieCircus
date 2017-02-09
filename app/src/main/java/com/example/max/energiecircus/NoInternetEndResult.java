package com.example.max.energiecircus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.TextView;

import com.plattysoft.leonids.ParticleSystem;
import com.plattysoft.leonids.modifiers.ScaleModifier;

public class NoInternetEndResult extends AppCompatActivity {

    private TextView endResultTxt;
    private int lengthEndResult;
    @Override
    public void onBackPressed()
    {
        //Can't press back button
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intention = getIntent();
        double endResult = intention.getDoubleExtra("ENDRESULT", 0);
        Log.e("Energy Left Class B: " , String.valueOf(endResult));

        //set view
        setContentView(R.layout.activity_no_internet_end_result);

        String s= String.valueOf((int) endResult) + "W";
        lengthEndResult = String.valueOf((int) endResult).length();
        SpannableString ss1=  new SpannableString(s);
        ss1.setSpan(new RelativeSizeSpan(10f), 0,lengthEndResult, 0); // set size
        ss1.setSpan(new RelativeSizeSpan(6f), lengthEndResult, lengthEndResult+1,0);
        endResultTxt = (TextView) findViewById(R.id.myImageViewText);
        endResultTxt.setText(ss1);


//        endResultTxt = (TextView) findViewById(R.id.myImageViewText);
//        endResultTxt.setText((String.valueOf((int) endResult)));
//        endResultTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 150.f);

        //Sparkling stars
        ParticleSystem ps = new ParticleSystem(this, 20, R.drawable.star, 3000);//Deze star is de afbeelding die ik u direct zal sturen maar ge moogt daar ook een andere van maken ze
        ps.setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                .setAcceleration(0.000003f, 90)
                .setInitialRotationRange(0, 360)
                .setRotationSpeed(120)
                .setFadeOut(2000)
                .addModifier(new ScaleModifier(0f, 1.5f, 0, 1500))
                .oneShot(findViewById(R.id.myImageView), 20);//Hier vervangt ge de R.id.medal door de afbeelding waar ge uw sterren uit wilt laten komen

    }

}
