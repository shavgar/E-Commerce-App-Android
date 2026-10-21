package com.example.android.mymap.ui;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mymap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class SignUp extends AppCompatActivity {
    private static final String TAG = "SignUp";

    private EditText mSignUpEmail, mSignUpPassword, mSignUpConfirmPassword;
    private TextView mSignInText;
    private Button mSignUpButton;
    private ProgressBar mSignUpProgressBar;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        /*Assign the views to its variables*/
        mSignUpEmail = findViewById(R.id.activity_sign_up_email);
        mSignUpPassword = findViewById(R.id.activity_sign_up_password);
        mSignUpConfirmPassword = findViewById(R.id.activity_sign_up_confirm_password);
        mSignUpButton = findViewById(R.id.activity_sign_up_signup_button);
        mSignUpProgressBar = findViewById(R.id.activity_sign_up_progressbar);
        mSignInText = findViewById(R.id.activity_sign_up_signin_text);

        /*get instance of FirebaseAuth*/
        mAuth = FirebaseAuth.getInstance();
        /*when the user click on the sign up button*/
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        mSignInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /*when a new user register*/
    private void registerNewUser() {
        String SignUpEmail = mSignUpEmail.getText().toString();
        String SignUpPassword = mSignUpPassword.getText().toString();
        String SignUpConfirmPassword = mSignUpConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(SignUpEmail)) {
            Toast.makeText(this, "Enter Your Email", Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(SignUpPassword)) {
            Toast.makeText(this, "Enter your password", Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(SignUpConfirmPassword)) {
            Toast.makeText(this, "Confirm your password", Toast.LENGTH_LONG).show();
            return;
        } else if (!(SignUpPassword.equals(SignUpConfirmPassword))) {
            Toast.makeText(this, "Confirmed password should match password ", Toast.LENGTH_LONG).show();
            return;
        } else {
            mSignUpProgressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(SignUpEmail, SignUpPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUp.this, "Authenticated Successfully", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SignUp.this, SetupProfile.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                mSignUpProgressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }//END OF registerNewUser()

}//END OF class
