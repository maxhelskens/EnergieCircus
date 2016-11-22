package com.example.max.energiecircus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Evert on 6/10/2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE =  "sqlitedb";
    private static final String TABLE =     "sqlitetable";
    private static final String SCHOOL =    "school";
    private static final String CLASS =     "class";
    private static final String HIGHSCORE = "highscore";

    public DatabaseHelper(Context context) {
        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE + " (id integer primary key, " + SCHOOL + " text, " + CLASS + " text, " + HIGHSCORE + " integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Insert a new highscore into the database
     * @param classroom
     * @return
     */
    public boolean addClassroom(Classroom classroom){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try{
            contentValues.put(SCHOOL, classroom.getGroepsnaam());
            contentValues.put(CLASS, classroom.getClassname());
            contentValues.put(HIGHSCORE, classroom.getHighscore());
            db.insert(TABLE, null, contentValues);
            db.close();
            return true;
        }
        catch (Exception e){
            db.close();
            return false;
        }
    }

    /**
     * Get all classrooms with a highscore from the database
     * @return
     */
    public ArrayList<Classroom> getAllClassrooms(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Classroom> classrooms = new ArrayList<Classroom>();
        Cursor cursor = db.rawQuery("select * from " + TABLE + " order by " + HIGHSCORE, null);
        cursor.moveToFirst();
        try{
            while(!cursor.isAfterLast()){
                Classroom classroom = new Classroom();
                classroom.setId(cursor.getInt(cursor.getColumnIndex("id")));
                classroom.setClassname(cursor.getString(cursor.getColumnIndex(CLASS)));
                classroom.setHighscore(cursor.getString(cursor.getColumnIndex(HIGHSCORE)));
                classroom.setGroepsnaam(cursor.getString(cursor.getColumnIndex(SCHOOL)));
                classrooms.add(classroom);
                cursor.moveToNext();
            }
            db.close();
            return classrooms;
        }
        catch (Exception e) {
            db.close();
            return null;
        }
    }
}
