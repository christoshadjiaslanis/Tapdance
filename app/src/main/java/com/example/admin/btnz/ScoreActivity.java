package com.example.admin.btnz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class ScoreActivity extends AppCompatActivity {

    TextView ratingTextView;
    TextView scoreTextView;
    TextView ratioTextView;

    long score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        setupAppearance();

        setRating(getIntent().getDoubleExtra("rate", -1));
        score = getIntent().getIntExtra("score", -1);
        setScoreTextView(getIntent().getIntExtra("score", -1));
        setRatioTextView(getIntent().getDoubleExtra("rate", -1));
        saveScore();
    }

    private void setupAppearance() {
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ratingTextView = (TextView) findViewById(R.id.Rating);
        scoreTextView = (TextView) findViewById(R.id.score);
        ratioTextView = (TextView) findViewById(R.id.rate);
    }

    private void setRating(double rate) {

        rate /= 100;


        if (rate == 1) {
            ratingTextView.setText("S");
        } else if ((1 > rate) && (rate >= 0.90)) {
            ratingTextView.setText("A");
        } else if ((0.90 > rate) && (rate >= 0.80)) {
            ratingTextView.setText("B");
        } else if ((0.80 > rate) && (rate >= 0.60)) {
            ratingTextView.setText("C");
        } else {
            ratingTextView.setText("F");
        }
    }

    private void setScoreTextView(int score){
        scoreTextView.setText(String.valueOf(score));
    }

    private void setRatioTextView(double ratio){
        ratioTextView.setText(String.valueOf(ratio - (ratio % 0.01)) + "%");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    Intent intent = new Intent(ScoreActivity.this, Menu.class);
                    startActivity(intent);
                    finish();
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private void saveScore() {
        if (isHighScore()) {
            SharedPreferences.Editor mEditor = getSharedPreferences("scores", 0).edit();
            mEditor.putLong(String.valueOf(getIntent().getLongExtra("songID", -1)), score).commit();

            Toast toast = Toast.makeText(getApplicationContext(), "New Highscore!!", Toast.LENGTH_SHORT);
            toast.show();

        }

    }

    private boolean isHighScore(){
        boolean isHighScore = false;

        SharedPreferences mPrefs = getSharedPreferences("scores", 0);
        long prevScore = mPrefs.getLong(String.valueOf(getIntent().getLongExtra("songID", -1)), -1);

        if(prevScore < score){
            isHighScore = true;
            System.out.println("---------------------------------------HIGHSCORE--------------------------------------");
        }


        return isHighScore;
    }
}
