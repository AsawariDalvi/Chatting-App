package com.mountreach.chattingapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registeration_Activity extends AppCompatActivity {

    EditText etName, etMobileNo, etPassword, etEmail;
    Button btnRegister;
    ImageButton btnTogglePassword;
    TextView tvSignIn;
    boolean isPasswordVisible = false;

    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobileNo = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        tvSignIn = findViewById(R.id.tvAlreadyHaveAccount);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.icon_eye_closed);
            } else {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setImageResource(R.drawable.icon_eye_open);
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length());
        });

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String mobile = etMobileNo.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (name.isEmpty()) {
                etName.setError("Please Enter Your Name");
                return;
            }
            if (email.isEmpty()) {
                etEmail.setError("Please Enter Your Email");
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                etEmail.setError("Invalid email format");
                return;
            }
            if (mobile.isEmpty()) {
                etMobileNo.setError("Please Enter your Mobile Number");
                return;
            }
            if (mobile.length() != 10) {
                etMobileNo.setError("Mobile number must be 10 digits");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Please Enter a Strong Password");
                return;
            }
            if (password.length() < 8) {
                etPassword.setError("Password must be at least 8 characters");
                return;
            }

            // Create user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            // ✅ Store only name and mobile in Firestore
                            Map<String, Object> user = new HashMap<>();
                            user.put("uid", uid);
                            user.put("name", name);
                            user.put("mobile", mobile);

                            firestore.collection("users").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(Registeration_Activity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, Login_Activity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Registeration_Activity.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                        } else {
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(loginTask -> {
                                            if (loginTask.isSuccessful()) {
                                                Toast.makeText(Registeration_Activity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Registeration_Activity.this, Login_Activity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(Registeration_Activity.this, "Email already registered. Try logging in with correct password.", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(Registeration_Activity.this, "Register failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(Registeration_Activity.this, Login_Activity.class);
            startActivity(intent);
        });
    }
}
