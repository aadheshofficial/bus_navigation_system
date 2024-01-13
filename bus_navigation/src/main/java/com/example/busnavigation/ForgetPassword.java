package com.example.busnavigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgetPassword extends AppCompatActivity {
    private TextView login;
    private EditText email;
    private Button send;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        send = findViewById(R.id.send_email_forget_activity);
        email = findViewById(R.id.email_forget_activity);
        login = findViewById(R.id.back_to_login);
        firebaseAuth = FirebaseAuth.getInstance();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_f = email.getText().toString();
                if(!TextUtils.isEmpty(email_f)){
                    resetPassword(email_f,v);
                }
                if(TextUtils.isEmpty(email_f)){
                    Snackbar.make(v, "Please Enter your Email", Snackbar.LENGTH_SHORT).show();
                }

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgetPassword.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
    private void resetPassword(String email,View view){
        hideKeyboard(view);
        if(!isValidEmail(email)){
            Snackbar.make(view, "Please Recheck your Email", Snackbar.LENGTH_SHORT).show();
        }
        if (!email.isEmpty()) {
            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(ForgetPassword.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("resetSuccess", true);
                        startActivity(intent);
                        finish();

                    }
                    else {
                        String errorMessage = task.getException().getMessage();
                        Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
        else {
            Snackbar.make(view, "Please Enter your Email", Snackbar.LENGTH_SHORT).show();
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