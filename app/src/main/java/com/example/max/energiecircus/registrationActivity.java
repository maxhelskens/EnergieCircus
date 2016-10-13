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

public class RegistrationActivity extends AppCompatActivity {

    SharedPreferences SharedPreferences;
    EditText name;
    EditText klas;
    Button registratieKnop;
    Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        name = (EditText) findViewById(R.id.name);
        klas = (EditText) findViewById(R.id.klas);
        registratieKnop = (Button) findViewById(R.id.registratie);

        //File name and mode. Mode "0" is private mode
        SharedPreferences = getApplicationContext().getSharedPreferences("RegistrationActivity",0);
        editor = SharedPreferences.edit();

    }

    public void registreren(View v){

        String nameTxt = name.getText().toString();
        String klasTxt = klas.getText().toString();

        if(name.getText().length()<=0){
            Toast.makeText(RegistrationActivity.this, "Kies een naam", Toast.LENGTH_SHORT).show();
        }
        else if(klas.getText().length()<=0){
            Toast.makeText(RegistrationActivity.this, "Kies een klas", Toast.LENGTH_SHORT).show();
        }

        editor.putString("Naam", nameTxt);
        editor.putString("Klas", klasTxt);
        editor.commit();

        SharedPreferences prefs = getSharedPreferences("RegistrationActivity",0);
        String naamRegistratie = prefs.getString("Naam",null);
        String klasRegistratie = prefs.getString("Klas",null);
        if(naamRegistratie !=null && klasRegistratie !=null){
            Log.e("naam",naamRegistratie);
            Log.e("klas",klasRegistratie);
        }

        Intent showActivity = new Intent(this, GraphActivity.class);
        startActivity(showActivity);

    }



}
