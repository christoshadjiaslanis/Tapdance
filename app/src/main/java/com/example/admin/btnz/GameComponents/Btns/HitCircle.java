package com.example.admin.btnz.GameComponents.Btns;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.btnz.GameScreen;
import com.example.admin.btnz.R;

import java.io.Serializable;

public class HitCircle implements Serializable {
    private Ring ring;
    private long hitTime;
    private long lifeTime;
    private int x;
    private int y;
    private static int radiusInPixels = 0;
    private float z;
    private ImageButton button;
    private static int score;
    private Vibrator vibrator;
    private boolean hit = false;
    private static GameScreen gameScreen;
    private Handler handler;
    private Handler handler2;

    public HitCircle(long hitTime, long lifeTime, int x, int y, float z) {

        this.hitTime = hitTime;
        this.lifeTime = lifeTime;
        this.x = (int)Math.round(x - (0.75 * radiusInPixels));
        this.y = (int)Math.round(y - (0.75 * radiusInPixels));
        this.z = z;

    }

    public static void setGameScreen(GameScreen gameScreenInstance){
        gameScreen = gameScreenInstance;
    }

    public static void setRadiusInPixels(DisplayMetrics displayMetrics){

            radiusInPixels = (int)Math.round(displayMetrics.widthPixels * 0.16666);

    }

    private void setupButton(Context context) {
        setupVibrator(context);

        this.ring = new Ring(R.drawable.ring, radiusInPixels * 2, context, this.x, this.y, z);
        button = new ImageButton(context);
        button.setX(this.x);
        button.setY(this.y);
        button.setLayoutParams(new LinearLayout.LayoutParams(radiusInPixels, radiusInPixels));
        button.setZ(z);
        button.setBackgroundResource(R.drawable.button);
        button.setSoundEffectsEnabled(false);
    }

    private void setupVibrator(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void addToView(DisplayMetrics dm, View view, Context context) {
        setupButton(context);
        startTimer(view);
    }

    private void startTimer(final View view) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button.getParent() != null) {
                    addScore(v);
                    ((ViewManager) button.getParent()).removeView(button);
                    ((ViewManager) ring.getRingImage().getParent()).removeView(ring.getRingImage());
                    score += 100;
                    hit = true;
                    gameScreen.changeScore(true);
                    gameScreen.changeHp(hit);
                }
            }
        });

        handler = new Handler();
        Runnable r = new Runnable() {


            public void run() {

                RelativeLayout rl = (RelativeLayout) view;
                rl.addView(button);
                rl.addView(ring.getRingImage());
                ((View) button).setAlpha(0);
                ((View) ring.getRingImage()).setAlpha(0);
                ObjectAnimator anim = ObjectAnimator.ofFloat(button, "alpha", 0f, 1f);
                anim.setDuration(500);
                anim.start();
                ring.reduceSize();
            }
        };
        handler.postDelayed(r, hitTime - lifeTime);

        handler2 = new Handler();
        Runnable r2 = new Runnable() {


            public void run() {
                if (button.getParent() != null) {
                    ((ViewManager) button.getParent()).removeView(button);
                    ((ViewManager) ring.getRingImage().getParent()).removeView(ring.getRingImage());
                }

                if (!hit) {
                    gameScreen.changeScore(hit);
                    gameScreen.changeHp(hit);
                    vibrator.vibrate(75);

                }


            }
        };
        handler2.postDelayed(r2, hitTime);

    }

    private void addScore(View view) {
        TextView score = new TextView(view.getContext());
        score.setText("Great!");
        score.setX(this.x);
        score.setY(this.y);
        score.setTextColor(Color.WHITE);
        score.setTextSize(20);
        score.setTypeface(null, Typeface.BOLD);
        score.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        RelativeLayout rl = (RelativeLayout) view.getParent();

        rl.addView(score);


        score.animate().translationY(-20).setDuration(400);
        score.animate().alpha(0).setDuration(800);
    }

    public void normalise(DisplayMetrics displayMetrics) {
            this.x = (int)Math.round ((this.x * (1.0 - ( 2.0 * radiusInPixels / displayMetrics.widthPixels))) + radiusInPixels);
            this.y = (int)Math.round ((this.y * (1.0 - ( 2.0 * radiusInPixels / displayMetrics.widthPixels))) + radiusInPixels);
    }


    public static int getRadiusInPixels() {
        return radiusInPixels;
    }

    public void release(){
        if(handler != null ){
            handler.removeCallbacksAndMessages(null);
        }

        if(handler2 != null) {
            handler2.removeCallbacksAndMessages(null);
        }
    }
}
