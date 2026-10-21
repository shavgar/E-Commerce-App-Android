package com.example.android.mymap.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mymap.R;
import com.example.android.mymap.models.UserAccountInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {
    private static final String TAG = "Settings";

    private EditText mFirstName, mLastName, mCountry, mSettingsEmail, mSettingsPhone;
    private TextView mSettingsPassword;
    private CircleImageView mProfileImage;
    private Button mSaveButton;
    private ProgressBar mSettingsProgressBar;
    private Toolbar mToolbar;
    private String mCurrentUserid;
    private Uri mProfileImageUri;
    private static final int REQUEST_CODE = 1;
    private UserAccountInformation userAccountInformation;

    private CountryCodePicker countryCodePicker;
    private String phone_number;

    /*Firebase*/
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        /*Instantiate the views*/
        mProfileImage = findViewById(R.id.activity_settings_profile_image);
        mFirstName = findViewById(R.id.activity_settings_first_name);
        mLastName = findViewById(R.id.activity_settings_last_name);
        mCountry = findViewById(R.id.activity_settings_country);
        mSettingsEmail = findViewById(R.id.activity_settings_email);
        mSettingsPassword = findViewById(R.id.activity_settings_password);
        mSettingsPhone = findViewById(R.id.activity_settings_phone);
        mSaveButton = findViewById(R.id.activity_settings_save_button);
        mSettingsProgressBar = findViewById(R.id.activity_settings_progressbar);
        mToolbar = findViewById(R.id.activity_settings_toolbar);
        countryCodePicker = findViewById(R.id.activity_settings_country_code);

        /*get the phone number*/
        phone_number = countryCodePicker.getSelectedCountryCode();

        /*set the toolbar settings*/
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        /*add the back arrow to the toolbar*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*Instantiate Firebase variables*/
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserid = mAuth.getCurrentUser().getUid();
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserid);
        mUsersDatabaseReference.keepSynced(true);
        mCurrentUser = mAuth.getCurrentUser();
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Users Profile Images");
        /*fetch users information from database*/
        fetchUserInformation();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermissions();
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserAccountInformation();
            }
        });
    }//END OF onCreate()

    private void checkStoragePermissions() {
        Log.d(TAG, "checkStoragePermissions: called");

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED) {

            /*select and upload profile image*/
            selectUserProfileImage();
        } else {
            ActivityCompat.requestPermissions(Settings.this, permissions, REQUEST_CODE);
        }
    }

    /*fetch current user information from database*/
    private void fetchUserInformation() {
        mUsersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userAccountInformation = dataSnapshot.getValue(UserAccountInformation.class);
                    /*Assign the variable values to the views*/
                    Picasso.get().load(userAccountInformation.getProfileImageUrl()).networkPolicy(NetworkPolicy.OFFLINE)
                            .fit().into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(userAccountInformation.getProfileImageUrl()).fit().into(mProfileImage);
                        }
                    });
                    mFirstName.setText(userAccountInformation.getFirstName());
                    mLastName.setText(userAccountInformation.getLastName());
                    mCountry.setText(userAccountInformation.getCountry());
                    //mSettingsPhone.setText(userAccountInformation.getPhoneNumber);
                    if (mCurrentUser != null) {
                        mSettingsEmail.setText(mCurrentUser.getEmail());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }//END OF fetchUsers Information

    /*select the user profile image*/
    private void selectUserProfileImage() {
        Log.d(TAG, "selectUserProfileImage: called");
        /*Select and crop the profile image*/
        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(Settings.this);
    }// END OF selectUserProfileImage()

    /* Get the result of the intent in selectUserProfileImage()
    https://github.com/ArthurHub/Android-Image-Cropper*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                /*get the link(uri) to the stored image in device storage*/
                mProfileImageUri = result.getUri();
                mProfileImage.setImageURI(mProfileImageUri);
                /*upload the image to the firebase storage*/
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }//END OF onActivityResult()

    /*check user account information */
    private void checkUserAccountInformation() {
        Log.d(TAG, "checkUserAccountInformation: called");

        final String FirstName = mFirstName.getText().toString();
        final String LastName = mLastName.getText().toString();
        final String Country = mCountry.getText().toString();

        if (TextUtils.isEmpty(FirstName)) {
            Toast.makeText(this, "Enter your first name", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(LastName)) {
            Toast.makeText(this, "Enter your last name", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(Country)) {
            Toast.makeText(this, "Enter your country", Toast.LENGTH_LONG).show();
        } else if (mProfileImageUri == null) {
            mSettingsProgressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Upload your profile image", Toast.LENGTH_LONG).show();
            mUsersDatabaseReference.child("firstName").setValue(FirstName);
            mUsersDatabaseReference.child("lastName").setValue(LastName);
            mUsersDatabaseReference.child("country").setValue(Country);
            Toast.makeText(Settings.this, "Information Saved Successfully", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            saveUserAccountInformation(FirstName, LastName, Country);
        }
    }//END OF checkUserAccountInformation()

    /*update user information and save it in the database*/
    private void saveUserAccountInformation(final String firstName, final String lastName, final String country) {
        Log.d(TAG, "saveUserAccountInformation: called");
        mSettingsProgressBar.setVisibility(View.VISIBLE);
        final StorageReference profileImageReference = mStorageReference.child(mCurrentUserid);
        profileImageReference.putFile(mProfileImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Settings.this, "Image Saved Successfully in Firebase Storage", Toast.LENGTH_LONG).show();
                    profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            /*get a link(uri) to the stored image in firebase storage*/
                            String profileImageUrl = uri.toString();
                            UserAccountInformation userAccountInformation = new UserAccountInformation(firstName, lastName, country, profileImageUrl,"11111111");
                            mUsersDatabaseReference.setValue(userAccountInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Settings.this, "Information Saved Successfully", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(Settings.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Settings.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        mSettingsProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });

                        }//END OF onSuccess()
                    });
                } else {
                    Toast.makeText(Settings.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    mSettingsProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }//END OF saveUserInformation()

}


