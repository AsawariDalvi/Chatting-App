package com.mountreach.chattingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Login_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etMobile = findViewById(R.id.etMobile);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnLogin.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (mobile.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Authenticate with backend or Firebase
                Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, Home_Screen_Activity.class);
                startActivity(i);
                finish();
            }
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login_Activity.this,Registeration_Activity.class);
                startActivity(intent);
            }
        });


    }
}