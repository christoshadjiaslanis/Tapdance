package com.example.admin.btnz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class Start extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        setup();
    }

    private void setup() {
        setupAppearance();
        setupButtons();
    }

    private void setupAppearance() {
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setupButtons() {
        TextView playButton = (TextView) this.findViewById(R.id.play_button);

        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Start.this, Menu.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
