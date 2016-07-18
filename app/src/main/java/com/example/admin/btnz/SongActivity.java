package com.example.admin.btnz;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.admin.btnz.GameComponents.Difficulty;

import java.io.IOException;

public class SongActivity extends AppCompatActivity {

    MediaPlayer mPlayer;

    ToggleButton toggleButtonEasy;
    ToggleButton toggleButtonMedium;
    ToggleButton toggleButtonHard;
    ToggleButton toggleButtonInsane;

    TextView songNameTextView;

    Difficulty difficulty = Difficulty.MEDIUM;


    @Override
    protected void onPause(){
        super.onPause();
        try {
            if (mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }

            }
        }catch(Exception e){


        }

    }
    @Override
    protected void onResume(){
        super.onResume();
        try {
            if ((mPlayer != null)) {
                if (!mPlayer.isPlaying()) {
                    mPlayer.start();
                }
            }
        }catch(Exception e){


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        setupAppearance();
        initialiseMediaPlayer(getApplicationContext(), getIntent().getLongExtra("songID", -1), getIntent().getExtras().getInt("songDuration", -1));
        setupDifficultyToggles();
        setupScore();

        startMediaPlayer();
    }



    private void setupScore() {
        TextView scoreTextView = (TextView)findViewById(R.id.highScoreTextView);
        SharedPreferences mPrefs = getSharedPreferences("scores", 0);
        scoreTextView.setText(String.valueOf(mPrefs.getLong(String.valueOf(getIntent().getLongExtra("songID", 0)), 0)));
    }

    private void setupAppearance() {
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        songNameTextView = (TextView)findViewById(R.id.songName);
        songNameTextView.setText(getIntent().getExtras().getString("songName"));
        TextView playSongTextView = (TextView)findViewById(R.id.play_song);
        playSongTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PowerManager powerManager = (PowerManager)
                        getSystemService(Context.POWER_SERVICE);

                if (powerManager.isPowerSaveMode()) {

                    Toast toast = Toast.makeText(getApplicationContext(), "Please turn battery saver mode off", Toast.LENGTH_SHORT);
                    toast.show();

                } else {


                    Intent intent = new Intent(SongActivity.this, GameScreen.class);
                    intent.putExtra("songDuration", getIntent().getExtras().getInt("songDuration", -1));
                    intent.putExtra("songID", getIntent().getLongExtra("songID", -1));
                    intent.putExtra("difficulty", difficulty);
                    startActivity(intent);
                    mPlayer.stop();
                    mPlayer.release();
                    finish();
                }
            }
        });
    }

    private void setupDifficultyToggles() {

        toggleButtonEasy = (ToggleButton) findViewById(R.id.toggleButtonEasy);
        toggleButtonEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButtonMedium.isChecked())
                    toggleButtonMedium.toggle();
                if (toggleButtonHard.isChecked())
                    toggleButtonHard.toggle();
                if (toggleButtonInsane.isChecked())
                    toggleButtonInsane.toggle();
                if (!((ToggleButton) v).isChecked()) {
                    ((ToggleButton) v).toggle();
                }

                difficulty = Difficulty.EASY;
            }
        });
        toggleButtonMedium = (ToggleButton) findViewById(R.id.toggleButtonMedium);
        toggleButtonMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButtonEasy.isChecked())
                    toggleButtonEasy.toggle();
                if (toggleButtonHard.isChecked())
                    toggleButtonHard.toggle();
                if (toggleButtonInsane.isChecked())
                    toggleButtonInsane.toggle();
                if (!((ToggleButton) v).isChecked()) {
                    ((ToggleButton) v).toggle();
                }

                difficulty = Difficulty.MEDIUM;

            }
        });
        toggleButtonHard = (ToggleButton) findViewById(R.id.toggleButtonHard);
        toggleButtonHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButtonEasy.isChecked())
                    toggleButtonEasy.toggle();
                if (toggleButtonMedium.isChecked())
                    toggleButtonMedium.toggle();
                if (toggleButtonInsane.isChecked())
                    toggleButtonInsane.toggle();
                if (!((ToggleButton) v).isChecked()) {
                    ((ToggleButton) v).toggle();
                }
                difficulty = Difficulty.HARD;

            }
        });
        toggleButtonInsane = (ToggleButton) findViewById(R.id.toggleButtonInsane);
        toggleButtonInsane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleButtonEasy.isChecked())
                    toggleButtonEasy.toggle();
                if (toggleButtonHard.isChecked())
                    toggleButtonHard.toggle();
                if (toggleButtonMedium.isChecked())
                    toggleButtonMedium.toggle();
                if (!((ToggleButton) v).isChecked()) {
                    ((ToggleButton) v).toggle();
                }
                difficulty = Difficulty.INSANE;

            }
        });

    }

    private void initialiseMediaPlayer(Context context, long songID, int duration) {

        if (songID == -1) {

            Toast.makeText(SongActivity.this, "No Song Sorry", Toast.LENGTH_SHORT).show();

        }
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(context, ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    songID));
            mPlayer.prepare();

        } catch (IOException e) {
            Toast toast = Toast.makeText(context, "Could not find beatmap", Toast.LENGTH_SHORT);
            toast.show();
            Log.d("test", e.getMessage());

        }

        mPlayer.seekTo(duration / 2);

    }

    private void startMediaPlayer() {

        mPlayer.start();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    mPlayer.stop();
                    mPlayer.release();
                    Intent intent = new Intent(SongActivity.this, Menu.class);
                    startActivity(intent);
                    finish();
            }

        }
        return super.onKeyDown(keyCode, event);
    }



}
