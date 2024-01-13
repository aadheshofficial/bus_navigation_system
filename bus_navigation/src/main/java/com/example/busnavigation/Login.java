package com.example.busnavigation;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private ImageView passwordEye;
    private EditText email,password;
    private Button login;
    private TextView forgetPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(Login.this, Home.class);
            startActivity(intent);
            finish();
        }

    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        passwordEye = findViewById(R.id.password_eye_login_activity);
        email = findViewById(R.id.email_login_activity);
        password = findViewById(R.id.password_login_activity);
        login = findViewById(R.id.login_button_login_activity);
        forgetPassword = findViewById(R.id.forget_password_login_activity);

        Intent intent1 = getIntent();
        if (intent1 != null && intent1.getBooleanExtra("resetSuccess", false)) {
            Snackbar.make(findViewById(android.R.id.content), "Email sent successfully", Snackbar.LENGTH_SHORT).show();
        }
        login.setOnClickListener(view -> {
            hideKeyboard(view);
            String email_string = email.getText().toString();
            String pass = password.getText().toString();
            if(TextUtils.isEmpty(email_string)){
                Snackbar.make(findViewById(android.R.id.content),"Please Enter Email",Snackbar.LENGTH_LONG).show();
            }
            if(TextUtils.isEmpty(pass)){
                Snackbar.make(findViewById(android.R.id.content),"Please Enter Password",Snackbar.LENGTH_LONG).show();

            }
            if(!TextUtils.isEmpty(pass) && !TextUtils.isEmpty(email_string)){
                if(!isValidEmail(email_string)){
                    Snackbar.make(view, "Please Recheck your Email", Snackbar.LENGTH_SHORT).show();
                }
                else {
                    mAuth.signInWithEmailAndPassword(email_string, pass)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(Login.this, Home.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    if (errorMessage.contains("INVALID_EMAIL") || errorMessage.contains("INVALID_PASSWORD")) {
                                        errorMessage = "Incorrect credentials. Please try again.";
                                    }
                                    Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,ForgetPassword.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }
    public void ShowHidePass(View view) {
        if(view.getId()==R.id.password_eye_login_activity){
            if(password.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                ((ImageView)(view)).setImageResource(R.drawable.eye_close);
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            else{
                ((ImageView)(view)).setImageResource(R.drawable.eye_open);
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        }
    }
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}