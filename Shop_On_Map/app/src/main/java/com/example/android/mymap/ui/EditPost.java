package com.example.android.mymap.ui;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.mymap.MapsActivity;
import com.example.android.mymap.R;
import com.example.android.mymap.models.NewPostDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class EditPost extends AppCompatActivity {
    private static final String TAG = "EditPost";

    private Toolbar mToolbar;
    private EditText mPostType, mPostPrice, mPostDescription;
    private ProgressBar mPostProgressBar;
    private ImageView mPostImage;
    private Button mUpdateButton;
    private TextView mPostDate;

    private String postKey, currentUserid, productType, productPrice,
            productDescription, productDate, productImageUrl;
    private NewPostDetails newPostDetails;

    /*Firebase Database & Storage*/
    private FirebaseAuth mAuth;
    private DatabaseReference mPostDatabaseReference, mUserDatabaseReference;
    private StorageReference mStorageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        /*Assign the views to its variables*/
        mPostImage = findViewById(R.id.activity_edit_post_product_image);
        mPostType = findViewById(R.id.activity_edit_post_type);
        mPostPrice = findViewById(R.id.activity_edit_post_price);
        mPostDescription = findViewById(R.id.activity_edit_post_description);
        mUpdateButton = findViewById(R.id.activity_edit_update_post_button);
        mPostProgressBar = findViewById(R.id.activity_edit_post_progressbar);
        mPostDate = findViewById(R.id.activity_edit_post_publish_date);
        mToolbar = findViewById(R.id.activity_edit_post_toolbar);

        /*set the toolbar settings*/
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*get the post key from PostsAdapter Intent*/
        postKey = getIntent().getExtras().get("PostKey").toString();

        /*Instantiate firebase*/
        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getCurrentUser().getUid();
        mPostDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts").child(currentUserid).child(postKey);

        /*fetch posts details from firebase database*/
        fetchPostDetails();
        /*update button click*/
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPostDetails();
            }
        });
    }//END OF onCreate()

    /*fetch posts details from firebase database*/
    private void fetchPostDetails() {
        Log.d(TAG, "fetchPostDetails: called");

        mPostDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: called");

                if (dataSnapshot.exists()) {
                    /*get the post details*/
                    newPostDetails = dataSnapshot.getValue(NewPostDetails.class);

                    productType = newPostDetails.getPostType();
                    productPrice = newPostDetails.getPostPrice();
                    productDescription = newPostDetails.getPostDescription();
                    productDate = postKey;
                    productImageUrl = newPostDetails.getPostImageUrl();
                    /*load the post details into the views*/
                    mPostType.setText(productType);
                    mPostPrice.setText(productPrice);
                    mPostDescription.setText(productDescription);
                    mPostDate.setText(productDate);
                    Picasso.get().load(productImageUrl).fit().into(mPostImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }//END OF fetchPostDetails()

    /*Check Posts Details to see if an field has null values*/
    private void checkPostDetails() {
        Log.d(TAG, "checkPostDetails: called");

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
        } else {
            mPostDatabaseReference.child("postType").setValue(postType);
            mPostDatabaseReference.child("postPrice").setValue(postPrice);
            mPostDatabaseReference.child("postDescription").setValue(postDescription);
            Toast.makeText(this, "Post updated successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(EditPost.this, MapsActivity.class);
            startActivity(intent);
            finish();
        }
    }//END OF checkPostDetails()

}//END OF CLASS
