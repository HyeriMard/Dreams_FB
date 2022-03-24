package com.example.dreams;

import static androidx.core.content.res.ResourcesCompat.getFont;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kiprotich.japheth.TextAnim;

import java.util.regex.Pattern;

import www.sanju.motiontoast.MotionToast;

public class RegistrationActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    Toolbar toolbar;
    private DatabaseReference ref;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "firebase - REGISTER";
    TextInputEditText edtUsername;
    EditText edtSDT, edtMK, edtNLMK;
    CheckBox ckbHienMK;
    AppCompatButton btnDangKy, btnHuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        edtUsername = findViewById(R.id.edtUsername);
        edtMK = findViewById(R.id.edtMK);
        edtNLMK = findViewById(R.id.edtNLMK);
        ckbHienMK = findViewById(R.id.ckbHienMK);
        btnDangKy = findViewById(R.id.btnDangKy);
        btnHuy = findViewById(R.id.btnHuy);
        //Hiện mật khẩu
        ckbHienMK.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                edtMK.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                edtNLMK.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                edtMK.setTransformationMethod(PasswordTransformationMethod.getInstance());
                edtNLMK.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        btnHuy.setOnClickListener(view -> startActivity(new Intent(RegistrationActivity.this, Login.class)));
        btnDangKy.setOnClickListener(view -> {
            String userName = edtUsername.getText().toString().trim();
            String pass = edtMK.getText().toString().trim();
            String rePass = edtNLMK.getText().toString().trim();
            Boolean checkError = true;
            if (userName.isEmpty()) {
                edtUsername.setError("Mail cannot be blank");
                checkError = false;
            }
            if (!Pattern.matches("^[a-zA-Z][\\w-]+@([\\w]+\\.[\\w]+|[\\w]+\\.[\\w]{2,}\\.[\\w]{2,})$", userName)) {
                edtUsername.setError("Email is wrong format");
                checkError = false;
            }
            if (rePass.isEmpty()) {
                edtMK.setError("Re-enter password cannot be left blank");
                checkError = false;
            }
            if (pass.length() < 6) {
                edtMK.setError("Password must be at least 6 characters");
                checkError = false;
            }
            if (!rePass.equals(pass)) {
                edtNLMK.setError("Re-confirm incorrect password");
                checkError = false;
            }
            if (checkError) {

                createAccount(userName, pass);
            }
        });
    }

    private void createAccount(String email, String pass) {
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Sign Up Success");
                        MotionToast.Companion.darkToast(this,
                                "Register",
                                "Sign Up Success !",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                getFont(this, R.font.helvetica_regular));
                        startActivity(new Intent(RegistrationActivity.this, Login.class));
                    } else {
                        Log.w(TAG, "Registration failed", task.getException());
                        MotionToast.Companion.darkToast(this,
                                "Error 404",
                                "Registration Failed",
                                MotionToast.TOAST_ERROR,
                                MotionToast.GRAVITY_CENTER,
                                MotionToast.LONG_DURATION,
                                getFont(this, R.font.helvetica_regular));
                    }
                });
    }

    @Override
    public void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }
}