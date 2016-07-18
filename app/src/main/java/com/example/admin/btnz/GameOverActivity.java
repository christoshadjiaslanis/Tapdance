package com.example.admin.btnz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class GameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        setupAppearance();
        setupRetryButton();
    }

    private void setupAppearance() {

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setupRetryButton(){

        TextView retryButtonTextView = (TextView) findViewById(R.id.menuTextView);
        retryButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GameOverActivity.this, Menu.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
