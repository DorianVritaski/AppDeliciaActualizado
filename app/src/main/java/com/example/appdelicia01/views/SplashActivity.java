package com.example.appdelicia01.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdelicia01.R;

public class SplashActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView txtPercent;
    private int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);
        txtPercent = findViewById(R.id.txtPercent);

        Handler handler = new Handler();
        int[] steps = {10, 40, 70, 100};   // etapas
        int delay = 1000;                  // 1s por etapa

        for (int i = 0; i < steps.length; i++) {
            int value = steps[i];
            handler.postDelayed(() -> {
                progress = value;
                progressBar.setProgress(progress);
                txtPercent.setText("Cargando... " + progress + "%");

                if (progress == 100) {
                    // Abrir CatalogActivity al llegar a 100%
                    startActivity(new Intent(SplashActivity.this, CatalogActivity.class));
                    finish();
                }
            }, delay * (i + 1));
        }
    }
}
