package com.example.dreams;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kiprotich.japheth.TextAnim;

import su.levenetc.android.textsurface.TextSurface;

public class IntroActivity extends AppCompatActivity {
    private TextSurface textSurface;
    private TextAnim textWriter;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        textSurface = findViewById(R.id.textSurface);
        textWriter = findViewById(R.id.textWriter);
        textWriter
                .setWidth(12)
                .setDelay(30)
                .setColor(Color.BLACK)
                .setConfig(TextAnim.Configuration.INTERMEDIATE)
                .setSizeFactor(35f)
                .setLetterSpacing(25f)
                .setText("ALO CHAT")
                .startAnimation();
        textSurface.postDelayed(() -> {
            show();
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(IntroActivity.this, Login.class);
                startActivity(intent);
                finish();
            }, 6500);
        }, 1000);

//        Runnable runnable = () -> {
//            Intent intent = new Intent(IntroActivity.this,Login.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            finish();
//        };
//        Handler handler = new Handler();
//        handler.postDelayed(runnable,5000);
    }
    private void show() {
        Rotation3DSample.play(textSurface);
    }
}