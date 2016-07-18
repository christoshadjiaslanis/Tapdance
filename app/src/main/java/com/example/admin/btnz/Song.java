package com.example.admin.btnz;

import java.io.Serializable;

/**
 * Created by Admin on 27/3/2016.
 */
public class Song implements Serializable {
    private long id;
    private String title;
    private String artist;
    private String filename;

    public int getDurationMillis() {
        return durationMillis;
    }

    private int durationMillis;

    public Song(long songID, String songTitle, String songArtist, String filename, int durationMillis) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        this.filename = filename;
        this.durationMillis = durationMillis;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getFilename(){return filename;}

}
