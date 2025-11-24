package com.example.smd_project.SharedActivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smd_project.R;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_SCREEN_DURATION = 3500;


    Animation topAnim;
    ImageView image;
    TextView logoText, taglineText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);


        image = findViewById(R.id.iv_splash_logo);
        logoText = findViewById(R.id.tv_splash_app_name);
        taglineText = findViewById(R.id.tv_splash_tagline);

        image.setAnimation(topAnim);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO: CHANGE 'Login.class' to whatever your next activity is (e.g., MainActivity.class)
                Intent intent = new Intent(SplashScreen.this, Login.class);
                startActivity(intent);


                finish();
            }
        }, SPLASH_SCREEN_DURATION);
    }
}