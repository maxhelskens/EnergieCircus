package com.example.max.energiecircus;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EndResultActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter recAdapter;
    private RecyclerView.LayoutManager recLayoutManager;

    private ArrayList<Classroom> dataSet = new ArrayList<Classroom>();

    private static String url_all_schools = "http://www.thinkcore.be/sync/get_schools.php";


    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    // products JSONArray
    JSONArray schools = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_result);

        new LoadAllSchools().execute();

        // Connect the RecyclerView to the code
        recyclerView = (RecyclerView) findViewById(R.id.main_recyclerView);
        recyclerView.setHasFixedSize(true);

        // Create the layout manager and add it to the RecyclerView
        recLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recLayoutManager);

    }

    @Override
    public void onBackPressed()
    {
        // Nu kan da nimeer
    }


    /**
     * Background Async Task to Load all Schools by making HTTP Request
     * */
    class LoadAllSchools extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * getting All School from url
         */
        protected String doInBackground(String... args) {
            // getting JSON string from URL
            JSONArray json = jParser.makeHttpRequest(url_all_schools, "GET", null);

            Log.e("JSON", json.toString());

            for(int i=0; i<json.length(); i++){
                try {
                    JSONObject klas = json.getJSONObject(i);
                    Classroom insertClass = new Classroom();

                    insertClass.setClassname(klas.getString("Schoolname"));
                    insertClass.setGroepsnaam(klas.getString("Classname"));
                    insertClass.setHighscore(klas.getString("Highscore"));

                    dataSet.add(insertClass);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            return null;
        }

        protected void onPostExecute(String file_url) {
            recAdapter = new RecyclerAdapter(dataSet);
            recyclerView.setAdapter(recAdapter);
        }
    }

}
