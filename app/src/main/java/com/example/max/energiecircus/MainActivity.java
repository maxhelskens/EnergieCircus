package com.example.max.energiecircus;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

    SharedPreferences SharedPreferences;
    EditText name;
    EditText klas;
    EditText aantalLeerlingen;
    Button registratieKnop;
    Editor editor;
    EditText extraPuntenKwis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //File name and mode. Mode "0" is private mode. File name is .java class file name.
        SharedPreferences = getApplicationContext().getSharedPreferences("MainActivity", 0);
        editor = SharedPreferences.edit();

        //Check if school is still logged in
        SharedPreferences prefs = getSharedPreferences("MainActivity", 0);
        String naamRegistratie = prefs.getString("Naam", null);
        String klasRegistratie = prefs.getString("Klas", null);
        int aantalLeerlingenRegistratie = prefs.getInt("AantalLeerlingen", 0);
        Log.e("Shared Prefs", naamRegistratie + " " + klasRegistratie + " " + aantalLeerlingenRegistratie);
        if (naamRegistratie != null && klasRegistratie != null && aantalLeerlingenRegistratie != 0) {
            Intent showActivity = new Intent(this, GraphActivity.class);
            startActivity(showActivity);
        }
        //set view
        setContentView(R.layout.activity_registration);

        //declare variables
        name = (EditText) findViewById(R.id.editText_name);
        klas = (EditText) findViewById(R.id.editText_klas);
        aantalLeerlingen = (EditText) findViewById(R.id.editText_aantalLeerlingen);
        registratieKnop = (Button) findViewById(R.id.registratie);
        extraPuntenKwis = (EditText) findViewById(R.id.editText_aantalPuntenKwis);
    }

    public void registreren(View v) {

        String nameTxt = name.getText().toString();
        String klasTxt = klas.getText().toString();
        int aantalLeerlingenTxt;
        int aantalExtraPuntenKwisTxt;

        if (aantalLeerlingen.getText().toString().matches("")) {
            aantalLeerlingenTxt = 0;
        } else {
            aantalLeerlingenTxt = Integer.parseInt(aantalLeerlingen.getText().toString());

        }

        if (extraPuntenKwis.getText().toString().matches("")) {
            aantalExtraPuntenKwisTxt = 0;
        } else {
            aantalExtraPuntenKwisTxt = Integer.parseInt(extraPuntenKwis.getText().toString());

        }
        boolean inputOk = true;

        /*Fool proof: als ze iets vergeten in te vullen. Gebruik maken van Toast.*/
        if (name.getText().length() <= 0) {
            Toast.makeText(MainActivity.this, "Kies een naam", Toast.LENGTH_SHORT).show();
            inputOk = false;
        } else if (klas.getText().length() <= 0) {
            Toast.makeText(MainActivity.this, "Kies een klas", Toast.LENGTH_SHORT).show();
            inputOk = false;
        } else if (aantalLeerlingen.getText().length() <= 0) {
            Toast.makeText(MainActivity.this, "Hoeveel lampen telt je klas?", Toast.LENGTH_SHORT).show();
            inputOk = false;
        }


        if (inputOk) {
        /*Nodig voor SharedPreferences*/
            editor.putString("Naam", nameTxt);
            editor.putString("Klas", klasTxt);
            editor.putInt("AantalLeerlingen", aantalLeerlingenTxt);
            editor.putInt("AantalExtraPunten", aantalExtraPuntenKwisTxt);
            editor.putInt("laatsteScore", 0);
            editor.commit();

        /*Lezen van sharedPreferences*/
            SharedPreferences prefs = getSharedPreferences("MainActivity", 0);
            String naamRegistratie = prefs.getString("Naam", null);
            String klasRegistratie = prefs.getString("Klas", null);
            int aantalLeerlingenRegistratie = prefs.getInt("AantalLeerlingen", 0);

            if (naamRegistratie != null && klasRegistratie != null && aantalLeerlingenRegistratie != 0) {
                Log.e("naam: ", naamRegistratie);
                Log.e("klas: ", klasRegistratie);
                Log.e("aantal leerlingen: ", String.valueOf(aantalLeerlingenRegistratie));
            }

            DatabaseHelper dbh = new DatabaseHelper(this);
            Classroom classroom = new Classroom();
            classroom.setGroepsnaam(naamRegistratie);
            classroom.setClassname(klasRegistratie);
            classroom.setHighscore(String.valueOf(0)); //Stringify double value
            dbh.addClassroom(classroom);
            Log.e("ClassRoom added: ", classroom.getGroepsnaam());
            Intent showActivity = new Intent(this, GraphActivity.class);
            showActivity.putExtra("classroomObject", classroom);
            startActivity(showActivity);
        }

    }


}
