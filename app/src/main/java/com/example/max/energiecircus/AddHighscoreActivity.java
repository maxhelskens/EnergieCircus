package com.example.max.energiecircus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddHighscoreActivity extends AppCompatActivity {

    private EditText    editText_school;
    private EditText    editText_class;
    private EditText    editText_highscore;
    private Button      button_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* setContentView(R.layout.activity_add_highscore);

        editText_school = (EditText) findViewById(R.id.editText_school);
        editText_class = (EditText) findViewById(R.id.editText_class);
        editText_highscore = (EditText) findViewById(R.id.editText_highscore);
        button_save = (Button) findViewById(R.id.button_save);*/

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHighscore();
            }
        });
    }

    //saveHighscore in SQLite
    private void saveHighscore() {
        DatabaseHelper dbh = new DatabaseHelper(this);
        Classroom classroom = new Classroom();
        classroom.setSchoolname(editText_school.getText().toString());
        classroom.setClassname(editText_class.getText().toString());
        classroom.setHighscore(editText_highscore.getText().toString());
        dbh.addClassroom(classroom);
        finish();
    }

    public void toMainMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
