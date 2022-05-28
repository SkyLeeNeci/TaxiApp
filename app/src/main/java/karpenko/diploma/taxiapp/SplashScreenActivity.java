package karpenko.diploma.taxiapp;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

public class SplashScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.progressBar);

        Thread thread = new Thread(() -> {
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                startActivity(new Intent(this, ChooseModeActivity.class));
            }
        });
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}