package com.example.admin.btnz;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.btnz.GameComponents.Btns.HitCircle;
import com.example.admin.btnz.GameComponents.Difficulty;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javazoom.jl.decoder.BitstreamException;

public class Menu extends AppCompatActivity {

    private ArrayList<Song> songList;
    private ListView songView;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setupAppearance();





        getSongList();
        songView = (ListView) findViewById(R.id.song_list);
        sort();
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }



    private void setupAppearance() {
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    public void sort() {
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    public void getSongList() {

        songList = new ArrayList<Song>();

        ContentResolver musicResolver = getContentResolver();

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int fileName = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);
            int durationMillis = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION); // duration in ms


            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisFileName = musicCursor.getString(fileName);
                int thisDurationMillis = musicCursor.getInt(durationMillis);


                songList.add(new Song(thisId, thisTitle, thisArtist, thisFileName, thisDurationMillis));
            }
            while (musicCursor.moveToNext());
        }
    }



    public class SongAdapter extends BaseAdapter {

        private ArrayList<Song> songs;
        private LayoutInflater songInf;

        public SongAdapter(Context c, ArrayList<Song> theSongs) {
            songs = theSongs;
            songInf = LayoutInflater.from(c);
        }


        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Object getItem(int arg0) {
            Song currSong = songs.get(arg0);
            currSong.getFilename();

            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public void songPicked(View view) {
            Song currSong = songList.get(Integer.parseInt(view.getTag().toString()));

        }


        public View getView(int position, View convertView, ViewGroup parent) {
            //map to song layout
            LinearLayout songLay = (LinearLayout) songInf.inflate
                    (R.layout.song, parent, false);
            songLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                        Song currSong = songList.get(Integer.parseInt(v.getTag().toString()));


                        Intent intent = new Intent(Menu.this, SongActivity.class);
                        intent.putExtra("songDuration", currSong.getDurationMillis());
                        intent.putExtra("songID", currSong.getID());
                        intent.putExtra("songName", currSong.getTitle());
                        System.out.println(currSong.getTitle());
                        startActivity(intent);
                        finish();




                }
            });
            //get title and artist views
            TextView songView = (TextView) songLay.findViewById(R.id.song_title);
            TextView artistView = (TextView) songLay.findViewById(R.id.song_artist);
            //get song using position
            Song currSong = songs.get(position);
            //get title and artist strings
            songView.setText(currSong.getTitle());
            artistView.setText(currSong.getArtist());
            //set position as tag
            songLay.setTag(position);
            return songLay;
        }




    }



}

//-----ARRAY ADAPTER---------------------------------------------------------------------------------------


