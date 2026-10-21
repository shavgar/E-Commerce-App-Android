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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.mymap.R;
import com.example.android.mymap.models.UserAccountInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupProfile extends AppCompatActivity {
    private static final String TAG = "SetupProfile";

    private EditText mFirstName, mLastName, mCountry, mPhone;
    private CircleImageView mProfileImage;
    private Button mSaveButton;
    private ProgressBar mSetupProfileProgresBar;

    private String mCurrenUserid, mCountryCode, mPhoneNumber;
    private Uri mProfileImageUri;
    private static final int REQUEST_CODE = 1;

    /*Firebase*/
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private CountryCodePicker countryCodePicker;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        /*Assign the views to its variables*/
        mFirstName = findViewById(R.id.activity_setup_profile_first_name);
        mLastName = findViewById(R.id.activity_setup_profile_last_name);
        mCountry = findViewById(R.id.activity_setup_profile_country);
        mProfileImage = findViewById(R.id.activity_setup_profile_image);
        mSaveButton = findViewById(R.id.activity_setup_profile_save_button);
        mSetupProfileProgresBar = findViewById(R.id.activity_setup_profile_progressbar);
        mPhone = findViewById(R.id.activity_setup_profile_phone);
        countryCodePicker = findViewById(R.id.activity_setup_profile_country_code);

        /*get the phone number*/
        mCountryCode =  countryCodePicker.getSelectedCountryCode();

        /*Instaniate Firebase variables*/
        mAuth = FirebaseAuth.getInstance();
        mCurrenUserid = mAuth.getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrenUserid);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Users Profile Images");

        /*send the user to the "Main" activity*/
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: phone number is: "+phoneNumber);
                checkUserAccountInformation();
            }
        });

        /*Click on user profile image*/
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStoragePermissions();
            }
        });


    }//END OF onCreate()

    //grant permissions to use the internal/external storage and camera
    private void checkStoragePermissions() {
        Log.d(TAG, "verifyPermissions: asking user for permissions");

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
            ActivityCompat.requestPermissions(SetupProfile.this, permissions, REQUEST_CODE);
        }
    }//END OF checkPermissions()

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");

        checkStoragePermissions();
    }

    /*save the user profile image*/
    private void selectUserProfileImage() {
        Log.d(TAG, "selectUserProfileImage: called");

        /*Select and crop the profile image*/
        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(SetupProfile.this);
    }// END OF selectUserProfileImage()


    /* Get the result of the intent in selectUserProfileImage()
     https://github.com/ArthurHub/Android-Image-Cropper*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        final String PhoneNumber = mPhone.getText().toString();
        if (TextUtils.isEmpty(FirstName)) {
            Toast.makeText(this, "Enter your first name", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(LastName)) {
            Toast.makeText(this, "Enter your last name", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(Country)) {
            Toast.makeText(this, "Enter your country", Toast.LENGTH_LONG).show();
        } else if (mProfileImageUri == null) {
            Toast.makeText(this, "Upload your profile image", Toast.LENGTH_LONG).show();
        } else if (PhoneNumber.trim().length()< 0) {
            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_LONG).show();
        }else {
            saveUserAccountInformation(FirstName, LastName, Country, PhoneNumber);

        }
    }//END OF checkUserAccountInformation()

    /*upload user account information to the database*/
    private void saveUserAccountInformation(final String firstName, final String lastName, final String country, final String phoneNumber) {
        Log.d(TAG, "saveUserAccountInformation: called");
        mSetupProfileProgresBar.setVisibility(View.VISIBLE);
        final StorageReference profileImageReference = mStorageReference.child(mCurrenUserid);
        profileImageReference.putFile(mProfileImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SetupProfile.this, "Image Saved Successfully in Firebase Storage", Toast.LENGTH_LONG).show();
                    profileImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            /*get a link(uri) to the stored image in firebase storage*/
                            String profileImageUrl = uri.toString();
                            UserAccountInformation userAccountInformation = new UserAccountInformation(firstName, lastName, country, profileImageUrl, phoneNumber);
                            mDatabaseReference.setValue(userAccountInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SetupProfile.this, "Information Saved Successfully", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(SetupProfile.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(SetupProfile.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        mSetupProfileProgresBar.setVisibility(View.GONE);
                                    }
                                }
                            });

                        }//END OF onSuccess()
                    });
                } else {
                    Toast.makeText(SetupProfile.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    mSetupProfileProgresBar.setVisibility(View.GONE);
                }
            }
        });
    }

}//END OF class
