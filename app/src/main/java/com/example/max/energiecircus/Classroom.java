package com.example.max.energiecircus;

import java.io.Serializable;

/**
 * Created by Evert on 11/10/2016.
 */

public class Classroom implements Serializable{

    private int id;
    private String groepsnaam;
    private String classname;
    private String highscore;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroepsnaam() {
        return groepsnaam;
    }

    public void setGroepsnaam(String name) {
        this.groepsnaam = name;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getHighscore() {
        return highscore;
    }

    public void setHighscore(String highscore) {
        this.highscore = highscore;
    }
}
