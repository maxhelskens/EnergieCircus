package com.example.max.energiecircus;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    SharedPreferences SharedPreferences;
    EditText name;
    EditText klas;
    EditText klasOpp;
    Button registratieKnop;
    Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        name = (EditText) findViewById(R.id.name);
        klas = (EditText) findViewById(R.id.klas);
        klasOpp = (EditText) findViewById(R.id.oppervlakteKlas);
        registratieKnop = (Button) findViewById(R.id.registratie);

        //File name and mode. Mode "0" is private mode. File name is .java class file name.
        SharedPreferences = getApplicationContext().getSharedPreferences("RegistrationActivity",0);
        editor = SharedPreferences.edit();

    }

    public void registreren(View v){

        String nameTxt = name.getText().toString();
        String klasTxt = klas.getText().toString();
        int klasOppInt = Integer.parseInt(klasOpp.getText().toString());

        /*Fool proof: als ze iets vergeten in te vullen. Gebruik maken van Toast.*/
        if(name.getText().length()<=0){
            Toast.makeText(MainActivity.this, "Kies een naam", Toast.LENGTH_SHORT).show();
        }
        else if(klas.getText().length()<=0){
            Toast.makeText(MainActivity.this, "Kies een klas", Toast.LENGTH_SHORT).show();
        }else if(klasOpp.getText().length()<=0){
            Toast.makeText(MainActivity.this, "Je moet de oppervlakte van je klas ingeven!", Toast.LENGTH_SHORT).show();
        }

        /*Nodig voor SharedPreferences*/
        editor.putString("Naam", nameTxt);
        editor.putString("Klas", klasTxt);
        editor.putInt("KlasOpp", klasOppInt);
        editor.commit();

        SharedPreferences prefs = getSharedPreferences("RegistrationActivity",0);
        String naamRegistratie = prefs.getString("Naam",null);
        String klasRegistratie = prefs.getString("Klas",null);
        int klasOppervlakteRegistratie = prefs.getInt("KlasOpp", 0);
        if(naamRegistratie !=null && klasRegistratie !=null && klasOppervlakteRegistratie != 0){
            Log.e("naam",naamRegistratie);
            Log.e("klas",klasRegistratie);
            Log.e("klasOppervklate", String.valueOf(klasOppervlakteRegistratie));
        }

        Intent showActivity = new Intent(this, GraphActivity.class);
        startActivity(showActivity);

    }



}
