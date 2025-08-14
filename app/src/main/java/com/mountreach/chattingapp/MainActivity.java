package com.mountreach.chattingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000; // 2 seconds
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        new Handler().postDelayed(() -> {
            if (currentUser != null) {
                // User already logged in
                startActivity(new Intent(MainActivity.this, Home_Screen_Activity.class));
            } else {
                // User not logged in
                startActivity(new Intent(MainActivity.this, Login_Activity.class));
            }
            finish();
        }, SPLASH_TIME);
    }
}
