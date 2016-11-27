package com.example.max.energiecircus;

        import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.plattysoft.leonids.ParticleSystem;
import com.plattysoft.leonids.modifiers.ScaleModifier;

public class NoInternetEndResult extends AppCompatActivity {

    private TextView endResultTxt;

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

        endResultTxt = (TextView) findViewById(R.id.myImageViewText);
        endResultTxt.setText((String.valueOf((int) endResult)));
        endResultTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 150.f);

        //Sparkling stars
        ParticleSystem ps = new ParticleSystem(this, 20, R.drawable.star, 3000);//Deze star is de afbeelding die ik u direct zal sturen maar ge moogt daar ook een andere van maken ze
        ps.setSpeedByComponentsRange(-0.1f, 0.1f, -0.1f, 0.02f)
                .setAcceleration(0.000003f, 90)
                .setInitialRotationRange(0, 360)
                .setRotationSpeed(120)
                .setFadeOut(2000)
                .addModifier(new ScaleModifier(0f, 1.5f, 0, 1500))
                .oneShot(findViewById(R.id.myImageViewText), 20);//Hier vervangt ge de R.id.medal door de afbeelding waar ge uw sterren uit wilt laten komen

    }

}
