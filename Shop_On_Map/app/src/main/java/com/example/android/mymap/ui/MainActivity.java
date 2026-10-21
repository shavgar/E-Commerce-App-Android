package com.example.android.mymap.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;

import com.example.android.mymap.models.MyPostsAdapter;
import com.example.android.mymap.models.NewPostDetails;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.example.android.mymap.MapsActivity;
import com.example.android.mymap.R;
import com.example.android.mymap.models.InternetConnectivityCheck;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.MapView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private RecyclerView mRecyclerView;
    private CircleImageView mNavigationMenuCircleImage;
    private TextView mNavigationMenuUsername;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private View mActivityMainNavigationMenuHeader;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabaseReference, mMyPostsDatabaseReference;

    //variables
    private String currentUserid;
    private ArrayList<NewPostDetails> myPostsArraylist;
    private RecyclerView mMyPostsRecylerView;
    private MyPostsAdapter myPostsAdapter;


    ///New Code///
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    private boolean mLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    private static final int ERROR_DIALOG_REQUEST = 1;
    private MapView mapView;
    private FusedLocationProviderClient mFusedLocationClient;
    private BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: called");


        /*Assign the views to its variables*/
        mDrawerLayout = findViewById(R.id.activity_main_drawerlayout);
        mNavigationView = findViewById(R.id.activity_main_navigationview);
        mRecyclerView = findViewById(R.id.activity_main_myposts_recyclerview);
        mToolbar = findViewById(R.id.activity_main_toolbar);
        mActivityMainNavigationMenuHeader = mNavigationView.inflateHeaderView(R.layout.navigation_menu_header_layout);
        mNavigationMenuCircleImage = mActivityMainNavigationMenuHeader.findViewById(R.id.navigation_menu_header_profile_image);
        mNavigationMenuUsername = mActivityMainNavigationMenuHeader.findViewById(R.id.navigation_menu_header_username);
        mMyPostsRecylerView = findViewById(R.id.activity_main_myposts_recyclerview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mMyPostsRecylerView.setHasFixedSize(true);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);


        /*check internet connection*/
        checkInternetConnectivity();

        /*Get Instance of FirebaseAuth*/
        mAuth = FirebaseAuth.getInstance();
        /*currentUserid = mAuth.getCurrentUser().getUid();*/
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mMyPostsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("MyPosts");
        mUserDatabaseReference.keepSynced(true);

        /*Add toolbar to the "Home" activity*/
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        /*Add the white drawer toggle (Sandwish shape) to the toolbar*/
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*When the user click on the navigation menu item*/
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                navigationMenuItemSelected(item);
                return false;
            }
        });

        /*check if the user has data saved in Firebase Database*/
        checkUserExistenceInDatabase();

    }//END OF onCreate()


    @Override
    protected void onStart() {
        super.onStart();
        //getting the current user from the FirebaseAuth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserid = currentUser.getUid();
        mUserDatabaseReference.child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String firstName, lastName, profileImageUrl;
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("firstName") && dataSnapshot.hasChild("lastName")) {
                        firstName = dataSnapshot.child("firstName").getValue().toString();
                        lastName = dataSnapshot.child("lastName").getValue().toString();
                        mNavigationMenuUsername.setText(firstName + " " + lastName);
                    }
                    if (dataSnapshot.hasChild("profileImageUrl")) {
                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        Picasso.get().load(profileImageUrl).into(mNavigationMenuCircleImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*fetch all my posts*/
        myPostsArraylist = new ArrayList<>();
        mMyPostsDatabaseReference.child(currentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    NewPostDetails myPostDetails;
                    for (DataSnapshot postdetails : dataSnapshot.getChildren()) {
                        myPostDetails = postdetails.getValue(NewPostDetails.class);
                        Log.d(TAG, "onDataChange: post key: " + postdetails.getKey() + " post type: " + myPostDetails.getPostType());
                        myPostsArraylist.add(myPostDetails);
                    }
                    Log.d(TAG, "onDataChange: mypostarraylist size  : " + myPostsArraylist.size());
                    myPostsAdapter = new MyPostsAdapter(MainActivity.this, myPostsArraylist);
                    mMyPostsRecylerView.setAdapter(myPostsAdapter);
                    myPostsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*Check whether the user has an account in the firebase database*/
    private void checkUserExistenceInDatabase() {
        final String currenUserid = mAuth.getCurrentUser().getUid();
        mUserDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currenUserid)) {
                    Intent intent = new Intent(MainActivity.this, SetupProfile.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }//END OF checkUserExistenceInDatabase()


    /* When the user click on the drawer toggle the navigation menu appear*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: called");

        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*When the user click on the navigation menu item*/
    private void navigationMenuItemSelected(MenuItem item) {
        Log.d(TAG, "navigationMenuItemSelected: called");

        switch (item.getItemId()) {
            case R.id.navigation_menu_map:
                Toast.makeText(this, "Home Clicked", Toast.LENGTH_LONG).show();
                Intent MapsIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(MapsIntent);
                break;
            case R.id.navigation_menu_settings:
                Toast.makeText(this, "Settings Clicked", Toast.LENGTH_LONG).show();
                Intent settingsIntent = new Intent(MainActivity.this, Settings.class);
                startActivity(settingsIntent);
                break;
            case R.id.navigation_menu_help:
                Toast.makeText(this, "Help Clicked", Toast.LENGTH_LONG).show();
                break;
            case R.id.navigation_menu_about:
                Toast.makeText(this, "About Clicked", Toast.LENGTH_LONG).show();
                break;
            case R.id.navigation_menu_signout:
                Toast.makeText(this, "Sign Out Clicked", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                Intent signOutIntent = new Intent(MainActivity.this, SignIn.class);
                signOutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signOutIntent);
                break;
        }
    }//END OF navigationMenuItemSelected

    /*check network Connectivity*/
    private void checkInternetConnectivity() {
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int[] networkTypes = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
                if (InternetConnectivityCheck.isNetworkAvailbale(context, networkTypes) == true) {
                    return;

                } else {
                    Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Internet Connection Alert")
                            .setMessage("Please check your internet connection")
                            .setPositiveButton("close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .create()
                            .show();
                }

            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }//END OF checkInternetConnectivity(){

    /*when the activity is destroyed*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}//END OF class
