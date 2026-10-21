package com.example.android.mymap.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mymap.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignIn extends AppCompatActivity {
    private static final String TAG = "SignIn";

    private EditText mSignInEmail, mSignInPassword;
    private TextView mSignupText, mForgotPassword;
    private Button mSignInButton;
    private ProgressBar mSignInProgressBar;
    private CircleImageView mSignInGoogleAuth, mSignInFacebookAuth, mSingInTwitterAuth;
    private Drawable highlight;

    /*Firebase*/
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    /*Google Authentication*/
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        /*Assign the views to its variables*/
        mSignInEmail = findViewById(R.id.activity_sign_in_email);
        mSignInPassword = findViewById(R.id.activity_sign_in_password);
        mSignupText = findViewById(R.id.activity_sign_in_signup_text);
        mForgotPassword = findViewById(R.id.activity_sign_in_forgot_password);
        mSignInButton = findViewById(R.id.activity_sign_in_signin_button);
        mSignInProgressBar = findViewById(R.id.activity_sign_in_progressbar);
        mSignInGoogleAuth = findViewById(R.id.activity_sign_google_auth);
        mSignInFacebookAuth = findViewById(R.id.activity_sign_facebook_auth);
        mSingInTwitterAuth = findViewById(R.id.activity_sign_twitter_auth);

        /*highlight the social media images when it is clicked*/
        highlight = ContextCompat.getDrawable(this, R.drawable.ic_circle_gray);

        /*get instance of Firebase*/
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        //getting the current user from the FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        /*when the activity starts check if the user is authenticated or not*/
        if(currentUser != null){
            Intent intent = new Intent(SignIn.this, MainActivity.class);
            /*this will prevent the user to return to this activity when he press "back" button*/
            startActivity(intent);
            finish();
        }

        /*send the user to the "Sign Up" activity*/
        mSignupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });
        /*send the user to the "Forgot password" activity*/
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
        /*when the user click on sign in button*/
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSignIn();
            }
        });
        /*SignIn using Google account*/
        mSignInGoogleAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignInGoogleAuth.setBackground(highlight);
                signIn();
            }
        });
        /* Configure Google Sign In*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SignIn.this, "Google authentication failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }//END OF onCreate()

    /* Configure Google Sign In
    * https://firebase.google.com/docs/auth/android/google-signin*/
    private void signIn()
    {
        mSignInProgressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSignInProgressBar.setVisibility(View.VISIBLE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult googleSignInResult =  Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (googleSignInResult.isSuccess()){
                mSignInGoogleAuth.setBackground(null);
                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
                if(googleSignInAccount != null) {
                    firebaseAuthWithGoogle(googleSignInAccount);
                }
            }else{
                mSignInProgressBar.setVisibility(View.GONE);
                mSignInGoogleAuth.setBackground(null);
            }

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mSignInProgressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(SignIn.this, "Google authenticated successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignIn.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignIn.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    /* END OF Configure Google Sign In*/
    /*When the user sign in*/
    private void userSignIn() {
        Log.d(TAG, "userSignIn: called");

        String SignInEmail = mSignInEmail.getText().toString();
        String SignInPassword = mSignInPassword.getText().toString();

        if (TextUtils.isEmpty(SignInEmail)) {
            Toast.makeText(this, "Enter Your Email", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(SignInPassword)) {
            Toast.makeText(this, "Enter your password", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "userSignIn: "+SignInEmail +" "+SignInPassword);

            mSignInProgressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(SignInEmail, SignInPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignIn.this, "Signed In Successfully", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SignIn.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SignIn.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                mSignInProgressBar.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }//END OF userSignIn()

}//END OF class
