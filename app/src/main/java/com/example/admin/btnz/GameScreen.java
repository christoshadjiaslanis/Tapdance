package com.example.admin.btnz;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.btnz.GameComponents.Btns.HitCircle;
import com.example.admin.btnz.GameComponents.Difficulty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javazoom.jl.decoder.BitstreamException;

public class GameScreen extends AppCompatActivity {

    HashMap<String, Object> hashMap;
    String mapname;
    ArrayList<HitCircle> beatMap;
    MediaPlayer mPlayer;
    int score = 0;
    int multiplier = 1;
    double rate = 0;
    int nHit = 0;
    int nMissed = 0;
    int hp = 100;
    int i =0;


    TextView scoreTextView;
    TextView multiplierTextView;

    ImageView healthBarView;
    Thread beatMapGeneratorThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);


        setupAppearance();


        final Handler h = new Handler();
        final GameScreen gameScreen = this;

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {

                    final FileInputStream fileInputStream = (FileInputStream) getContentResolver().openInputStream(ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            getIntent().getLongExtra("songID", -1)));

                    beatMap = new ArrayList<HitCircle>();
                    Thread.currentThread().setPriority(3);

                    beatMapGeneratorThread = new Thread(new Runnable() {
                        public void run() {

                            try {
                                AsyncBeatMapGenerator asyncBeatMapGenerator = new AsyncBeatMapGenerator();
                                asyncBeatMapGenerator.generateBeatMap(fileInputStream, getApplicationContext().getResources().getDisplayMetrics(),
                                        getIntent().getIntExtra("songDuration", -1), (Difficulty) getIntent().getExtras().getSerializable("difficulty"),
                                        beatMap, gameScreen);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    beatMapGeneratorThread.setPriority(7);
                    beatMapGeneratorThread.start();


                    /* BeatMapGenerator.generateBeatMap(fileInputStream, getApplicationContext().getResources().getDisplayMetrics(),
                            getIntent().getIntExtra("songDuration", -1), (Difficulty) getIntent().getExtras().getSerializable("difficulty"));


                    loadBeatMap();
                    */
                    initialiseMediaPlayer(getApplicationContext(), getIntent().getLongExtra("songID", -1));
                    startMediaPlayer();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }// catch (BitstreamException e) {
                //   e.printStackTrace();
                //}
            }
        };
        h.postDelayed(r, 100);


    }

    public void changeHp(boolean hit) {
        if ((hit) && (hp > 95)) {
            hp = 100;
        } else if (hit) {
            hp += 2;
        } else {
            hp -= 20;
        }

        updateHealthbar();
        checkGameOver(hp);
    }

    private void checkGameOver(int hp) {
        if (hp <= 0) {
            AsyncBeatMapGenerator.stopped = true;

            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
            }


            releaseHitCircles();
            Intent intent = new Intent(GameScreen.this, GameOverActivity.class);
            startActivity(intent);
            finish();

        }
    }

    private void updateHealthbar() {

        healthBarView.getLayoutParams().width = Math.round((((hp / 100.0F) * this.getResources().getDisplayMetrics().widthPixels)));

    }

    private void loadBeatMap() {
        System.out.println("Adding beatmaps-------------------------------------------");
        for (HitCircle hitCircle : beatMap) {
            hitCircle.addToView(this.getResources().getDisplayMetrics(), findViewById(R.id.gameScreenRelativeLayout), this.getApplicationContext());
        }
        if (!(beatMap.isEmpty())) {
            beatMap.get(0).setGameScreen(this);
        }
    }

    public void addHitCircle(HitCircle hitCircle) {
        hitCircle.setGameScreen(this);
        hitCircle.addToView(this.getResources().getDisplayMetrics(), findViewById(R.id.gameScreenRelativeLayout), this.getApplicationContext());
        beatMap.add(hitCircle);

    }


    public void changeScore(boolean hit) {
        if (hit) {
            score += (multiplier * 100);
            multiplier++;
        } else {
            multiplier = 1;
        }
        updateStats(score, multiplier, hit);

    }

    private void updateStats(int score, int multiplier, boolean hit) {
        updateScoreTextView(score);
        updateMultiplierTextView(multiplier);
        updateRate(hit);
    }

    private void updateRate(boolean hit) {

        if (hit) {
            nHit++;
        } else {
            nMissed++;
        }

        if ((nHit + nMissed) > 0) {
            rate = (1.0 * nHit) / (1.0 * (nHit + nMissed));
        }

        rate *= 100;

        rate -= (rate % 0.01);


    }

    private void updateScoreTextView(int score) {
        scoreTextView.setText(String.valueOf(score));
    }

    private void updateMultiplierTextView(int multiplier) {
        multiplierTextView.setText("x" + String.valueOf(multiplier));
    }


    private void setupAppearance() {

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.gameScreenRelativeLayout);
        relativeLayout.setBackgroundColor(Color.BLACK);
        relativeLayout.setZ(0.5F);

        scoreTextView = (TextView) findViewById(R.id.scoreTextView);
        multiplierTextView = (TextView) findViewById(R.id.multiplierTextView);
        healthBarView = (ImageView) findViewById(R.id.healthBarImageView);


    }

    private void initialiseMediaPlayer(Context context, long songID) {

        if (songID == -1) {

            Toast.makeText(GameScreen.this, "No Song Sorry", Toast.LENGTH_SHORT).show();

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


    }

    private void startMediaPlayer() {
        setMediaPlayerCompletionListener();
        mPlayer.start();

    }

    private void setMediaPlayerCompletionListener() {
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayer.release();

                Intent intent = new Intent(GameScreen.this, ScoreActivity.class);
                intent.putExtra("score", score);
                intent.putExtra("rate", rate);
                intent.putExtra("songID", getIntent().getLongExtra("songID", -1));
                startActivity(intent);
                finish();

            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Intent intent;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    goToMenu();
                    break;
                case KeyEvent.KEYCODE_HOME:
                    goToMenu();
                    break;
                case KeyEvent.KEYCODE_WINDOW:
                    goToMenu();
                    break;


            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private void goToMenu() {
        AsyncBeatMapGenerator.stopped = true;

        try {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
            }
        } catch (Exception e) {

        }

        releaseHitCircles();

        Intent intent = new Intent(GameScreen.this, Menu.class);
        startActivity(intent);

        finish();
    }

    private void releaseHitCircles() {
        System.out.println("released----------------------");
        for (HitCircle hitCircle : beatMap) {

            hitCircle.release();

        }
    }


}
