package com.example.android.mymap.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import de.hdodenhof.circleimageview.CircleImageView;

import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.android.mymap.MapsActivity;
import com.example.android.mymap.models.NewPostDetails;
import com.example.android.mymap.R;
import com.example.android.mymap.models.PostsAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NewPost extends AppCompatActivity {
    private static final String TAG = "NewPost";

    private Toolbar mToolbar;
    private EditText mPostType, mPostPrice, mPostDescription;
    private ProgressBar mPostProgressBar;
    private ImageView mPostImage;
    private Button mPostButton;
    private Uri mPostImageUri;
    private Switch mSwitchButton;
    /*Firebase Database & Storage*/
    private FirebaseAuth mAuth;
    private DatabaseReference mPostDatabaseReference, mUserDatabaseReference, mMypostsDatabaseReference;
    private StorageReference mPostStorageReference, mMyPostsStorageReference;

    /*variables*/
    private String currentUserid, currentDate, currentTime, currentDateandTime;
    private Calendar calendar;
    private Boolean phoneAvailable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        /*adding toolbar to the activity*/
        mToolbar = findViewById(R.id.activity_new_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("New Post");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /*Assign the views to its variables*/
        mPostImage = findViewById(R.id.activity_new_post_product_image);
        mPostType = findViewById(R.id.activity_new_post_type);
        mPostPrice = findViewById(R.id.activity_new_post_price);
        mPostDescription = findViewById(R.id.activity_new_post_description);
        mPostButton = findViewById(R.id.activity_new_post_post_button);
        mPostProgressBar = findViewById(R.id.activity_new_post_progressbar);
        mSwitchButton = findViewById(R.id.activity_setup_profile_switch_button);

        /*Instaniate Firebase variables*/
        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getCurrentUser().getUid();
        mPostDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(currentUserid);
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserid);
        mPostStorageReference = FirebaseStorage.getInstance().getReference().child("Users Posts Images");
        mMypostsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("MyPosts").child(currentUserid);
        mMyPostsStorageReference = FirebaseStorage.getInstance().getReference().child("My Posts Images");

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importPostImagefromGallery();
            }
        });
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPostDetails();
            }
        });

        mSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSwitchButton.isChecked()) {
                    phoneAvailable = true;
                }else{
                    phoneAvailable = false;
                }
            }
        });
    }//END OF onCreate()

    /*return to the main activity when the user clicks on "<--" (home) */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(NewPost.this, MapsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /*import product image from your device storage*/
    private void importPostImagefromGallery() {
        CropImage.activity()
                .start(NewPost.this);
    }//END OF importPostImagefromGallery()


    /*get the result of importPostImagefromGallery()*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mPostImageUri = result.getUri();
                Picasso.get().load(mPostImageUri).fit().into(mPostImage);
                //mPostImage.setImageURI(mPostImageUri);
                /*upload the image to the firebase storage*/
                ///uploadProductImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }//END OF onActivityResult()


    private void checkPostDetails() {
        final String postType = mPostType.getText().toString();
        final String postPrice = mPostPrice.getText().toString();
        final String postDescription = mPostDescription.getText().toString();

        /*check the post details*/
        if (TextUtils.isEmpty(postType)) {
            Toast.makeText(this, "Enter the product type", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(postPrice)) {
            Toast.makeText(this, "Enter the product price", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(postDescription)) {
            Toast.makeText(this, "Enter the product description", Toast.LENGTH_LONG).show();
        } else if (mPostImageUri == null) {
            Toast.makeText(this, "Upload post image", Toast.LENGTH_LONG).show();
        } else {
            uploadPostDetails(postType, postPrice, postDescription);
        }
    }//END OF checkPostDetails()

    /*upload post details to firebase*/
    private void uploadPostDetails(final String postType, final String postPrice, final String postDescription) {
        Log.d(TAG, "uploadPostDetails: called");
        mPostProgressBar.setVisibility(View.VISIBLE);
        /*In order to have a unique name for the image that is stored in firebase storage
         * we give it a name that has the time and date of uploading + the image type*/
        String postImageName = getUploadCurrentDateAndTime() + getFileExtension(mPostImageUri);
        /*In order to have a unique name for the post that is stored in firebase database
         * we give it a name that has the time and date of uploading */
        final String postNodeName = getUploadCurrentDateAndTime();
        /*String fileExtension = getFileExtension(mPostImageUri);*/
        final StorageReference postImageReference = mPostStorageReference.child(currentUserid).child(postImageName);
        final StorageReference myPostsImageReference = mMyPostsStorageReference.child(currentUserid).child(postImageName);
        /*upload post image to firebase storage*/
        postImageReference.putFile(mPostImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NewPost.this, "Post Image Uploaded Successfully to Firebase Storage", Toast.LENGTH_SHORT).show();
                    postImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String postImageUrl = uri.toString();
                            NewPostDetails newPostDetails = new NewPostDetails(postType, postPrice, postDescription, postImageUrl, currentDate, currentTime);
                            newPostDetails.setPhoneAvailable(phoneAvailable);
                            /*upload post object to firebase database*/
                            mPostDatabaseReference.child(postNodeName).setValue(newPostDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mPostProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(NewPost.this, "Post details uploaded successfully to database", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(NewPost.this, MapsActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(NewPost.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        mPostProgressBar.setVisibility(View.GONE);
                                    }
                                }
                            });//END OF mPostDatabaseReference.child(postNodeName)

                            /*add the post details to my posts activity*/
                            myPostsImageReference.putFile(mPostImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        myPostsImageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                final String myPostImageUrl = uri.toString();
                                                NewPostDetails newPostDetails = new NewPostDetails(postType, postPrice, postDescription, myPostImageUrl, currentDate, currentTime);
                                                mMypostsDatabaseReference.child(postNodeName).setValue(newPostDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                            });//END OF myPostsImageReference.putFile(mPostImageUri)

                        }//END OF onSuccess()
                    });
                } else {
                    Toast.makeText(NewPost.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    mPostProgressBar.setVisibility(View.GONE);
                }
            }
        });//END OF postImageReference.putFile(mPostImageUri)
    }//END OF uploadPostDetails()

    /*get selected image extension*/
    private String getFileExtension(Uri fileUri) {
        String fileExtension;
        //Check uri format to avoid null
        if (fileUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            fileExtension = mime.getExtensionFromMimeType(getApplicationContext().getContentResolver().getType(fileUri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            fileExtension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(fileUri.getPath())).toString());
        }
        return fileExtension;

    }//END OF getUploadedFileExtension(Uri fileUri)


    /*get the current date and time*/
    private String getUploadCurrentDateAndTime() {
        /*get the current date*/
        calendar = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = currentDateFormat.format(calendar.getTime());

        /*get the current time*/
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm:ss");
        currentTime = currentTimeFormat.format(calendar.getTime());
        currentDateandTime = currentDate + " " + currentTime;

        return currentDateandTime;
    }

}//END OF class
