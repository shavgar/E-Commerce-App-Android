package com.example.android.mymap;

import android.Manifest;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.android.mymap.models.ClusterManagerRender;
import com.example.android.mymap.models.ClusterMarker;
import com.example.android.mymap.models.PostsAdapter;
import com.example.android.mymap.models.SearchDialog;
import com.example.android.mymap.ui.ChatList;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.app.ActivityCompat;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.android.mymap.models.LatLngInterpolator;
import com.example.android.mymap.models.MarkerAnimation;
import com.example.android.mymap.models.NewPostDetails;
import com.example.android.mymap.ui.MainActivity;
import com.example.android.mymap.ui.NewPost;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private static final String ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int User_Location_Permission_Request_Code = 0;
    private static final int ERROR_DIALOG_REQUEST = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private RelativeLayout slideshowContainer;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker currentUserLocationMarker;
    private FloatingActionButton addPostButton;
    private FloatingActionButton cancelSearchButton;
    private boolean isSlideShowVisible = false;
    private RecyclerView mapsActivityRecyclerView;
    private boolean onGeoLocationUpdates = false;
    private LinearLayoutManager layoutManager;
    private boolean firstTimeFlag = true;
    /*Firebase Auth & Database*/
    private FirebaseAuth mAuth;
    private DatabaseReference mPostsDatabaseReference, mUsersDatabaseReference, mUserLocationsDatabaseReference;
    private String currentUserid;
    private Location mCurrentLocation;
    private String distanceRadius;
    private LatLng mCurrentLocationLatLng;

    private Boolean mLocationPermissionsGranted = false;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    private static float ZOOM_VALUE = 13f;


    /*Recyclerview for cardviews*/
    private ArrayList<NewPostDetails> postsArrayList;
    private ArrayList<NewPostDetails> clusterPostsDetailsArrayList;
    private PostsAdapter adapter;

    /* marker vars*/
    private CircleImageView customMarkerImage;
    private ImageButton mapZoomOut;
    private ArrayList<Marker> markers = new ArrayList<Marker>();


    private ArrayList<String> nearbyUserIDs = new ArrayList<>();
    private String UserId;
    private AppBarLayout appBarLayout;
    private ShimmerFrameLayout shimmerLayout;
    private BottomNavigationView bottomNavigationView;

    /*cluster variables*/
    private ClusterManager mClusterManager;
    private ClusterManagerRender mClusterManagerRender;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private Circle mCircle;

    private static final int REQUES_PHONE_CALL = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        /*start initializing the map on the screen*/
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        /*assign the views to its variables*/
        slideshowContainer = findViewById(R.id.activity_maps_slideshow_container);
        addPostButton = findViewById(R.id.activity_maps_add_post_button);
        cancelSearchButton = findViewById(R.id.activity_maps_cancel_search_button);
        mapZoomOut = findViewById(R.id.activity_maps_zoom_out_button);
        //customMarkerImage = findViewById(R.id.custom_marker_image);
        /*viewMore_button = findViewById(R.id.activity_maps_viewMore_button);*/
        appBarLayout = findViewById(R.id.activity_maps_appBarLayout);
        appBarLayout.setExpanded(false);

        /*Instantiate Firebase variables*/
        mAuth = FirebaseAuth.getInstance();
        currentUserid = mAuth.getCurrentUser().getUid();
        mPostsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserLocationsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("User Locations");
        /*load the data(texts)offline*/
        mPostsDatabaseReference.keepSynced(true);
        mUsersDatabaseReference.keepSynced(true);
        mUserLocationsDatabaseReference.keepSynced(true);


        /*Instantiate location services*/
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, NewPost.class);
                startActivity(intent);

            }
        });

        /*show recyclerview content placeholder*/
        shimmerLayout = findViewById(R.id.shimmerLayout);
        showShimmer();

        /*call card slideshow method*/
        mapsActivityRecyclerView = findViewById(R.id.actvity_maps_recyclerview);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        /*reverse the items order in the recyclerview (make the new added element the first item)*/
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        /*add the layout manager to the recyclerview*/
        mapsActivityRecyclerView.setLayoutManager(layoutManager);
        /*SnapHelper and SetItemAnimator are features but they are not required*/
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mapsActivityRecyclerView);
        mapsActivityRecyclerView.setItemAnimator(new SlideInLeftAnimator());

        /*bottom navigation menu*/
        bottomNavigationView = findViewById(R.id.activity_maps_bottom_navigation_menu);

        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_navigation_menu_map:
                        bottomNavigationView.getMenu().getItem(0).setChecked(true);
                        Intent mapIntent = new Intent(MapsActivity.this, MapsActivity.class);
                        startActivity(mapIntent);
                        finish();
                        break;
                    case R.id.bottom_navigation_menu_home:
                        bottomNavigationView.getMenu().getItem(1).setChecked(true);
                        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.bottom_navigation_menu_messages:
                        Intent chatListIntent = new Intent(MapsActivity.this, ChatList.class);
                        startActivity(chatListIntent);
                        bottomNavigationView.getMenu().getItem(2).setChecked(true);
                        break;
                    case R.id.bottom_navigation_menu_search:
                        bottomNavigationView.getMenu().getItem(3).setChecked(true);
                        openSearchDialog();

                }
                return false;
            }
        });

        /*get the distance from search dialog intent*/
        try {
            Intent searchIntent = getIntent();
            distanceRadius = searchIntent.getExtras().get("distance").toString();
            Log.d(TAG, "onCreate: distanceRadius= " + distanceRadius);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        cancelSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }//END OF onCreate()

    private void openSearchDialog() {
        SearchDialog searchDialog = new SearchDialog();
        searchDialog.show(getSupportFragmentManager(), "Search Dialog");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: callcustom_marker_imageed");

        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionsGranted) {
                getCurrentUserLocation();
            } else {
                requestLocationPermission();
            }
        }

    }//END OF onResume

    @Override
    protected void onStop() {
        super.onStop();
        /*stop location requests when the app is stopped*/
        if (mFusedLocationProviderClient != null)
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
      /*  GeoFire geoFire = new GeoFire(mUserLocationsDatabaseReference);
        geoFire.removeLocation(currentUserid, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Toast.makeText(MapsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });*/
    }

    /*check if the map services is available on the device*/
    private boolean checkMapServices() {
        Log.d(TAG, "checkMapServices: called");

        if (isGooglePlayServiciesAvailable()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }//END OF checkMapServices()

    /*check if the mobile has google services*/
    public boolean isGooglePlayServiciesAvailable() {
        Log.d(TAG, "isServicesOK: called");

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int available = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (googleApiAvailability.isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Google Play Services can't be installed on this device", Toast.LENGTH_SHORT).show();
        }
        return false;

    }//END OF isServicesOK()

    /*check if the map is enabled*/
    public boolean isMapsEnabled() {
        Log.d(TAG, "isMapsEnabled: called");

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean mapEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!mapEnabled) {
            noGPSAlertDialog();
            return false;
        }
        return true;
    }//END OF isMapsEnabled()

    /*Alert dialog when the GPS is turned off on the device*/
    private void noGPSAlertDialog() {
        Log.d(TAG, "buildAlertMessageNoGps: called");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS services is required, do you want to enable it?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }//END OF buildAlertMessageNoGps()

    /*manually created method to check the user's permission for his/her location*/
    public void requestLocationPermission() {
        Log.d(TAG, "checkUserLocationPermission: called");

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionsGranted = true;
            getCurrentUserLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }//END OF requestLocationPermission()

    /*get the user's permission result (the result of requestLocationPermission())*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");

        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                }
            }
        }
    }//END OF onRequestPermissionsResult()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionsGranted) {
                    getCurrentUserLocation();
                } else {
                    requestLocationPermission();
                }
            }
        }

    }//END OF nActivityResult()

    /*Get the user location coordinates without showing the blue dot on the location*/
    private void getCurrentUserLocation() {
        Log.d(TAG, "getUserLocation: called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
        }

        /*Create the location request to start receiving updates*/
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(2000);
        /*This will make a new request to get the user location every 4 seconds*/
        /*the looper is responsible for looping the request every 3 seconds*/
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());


    }//END OF getUserLocation()

    /* A locationCallback object is required for mFusedLocationProviderClient.requestLocationUpdates()*/
    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "onLocationResult: called ");

            mCurrentLocation = locationResult.getLastLocation();
            mCurrentLocationLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            /*in order to move the camera once, we make firsttimeflag true */
            if (firstTimeFlag && mCurrentLocation != null) {
                moveCamera(mCurrentLocationLatLng, ZOOM_VALUE);
                firstTimeFlag = false;
            }
            uploadCurrentUserLocationUpdates(mCurrentLocation);
            displayAllNearbyUsers();
        }
    };

    /*upload the user locations updates to firebase database*/
    private void uploadCurrentUserLocationUpdates(Location location) {
        Log.d(TAG, "uploadCurrentUserLocation: called");
        /*clear map every 2 seconds to remove old markers
        to stop marker blink remove the below line mGoogleMap.clear();*/
        //mGoogleMap.clear();
        markers.clear();
        if (mClusterManager != null) {
            mClusterManager.clearItems();
            mClusterManagerRender = null;
            mClusterMarkers.clear();

        }
        GeoLocation userGeoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
        GeoFire geoFire = new GeoFire(mUserLocationsDatabaseReference);
        geoFire.setLocation(currentUserid, userGeoLocation, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    Toast.makeText(MapsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }//END OF uploadCurrentUserLocationUpdates()

    /*
    display the location of all users on map*
    Retrieved from : https://github.com/SimCoderYoutube/UberClone/blob/master/android/app/src/main/java/com/simcoder/uber/CustomerMapActivity.java/
     */
    private void displayAllNearbyUsers() {
        Log.d(TAG, "displayAllNearbyUsers: called");

        GeoFire geoFire = new GeoFire(mUserLocationsDatabaseReference);
        GeoLocation geoLocation = new GeoLocation(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());
        GeoQuery geoQuery = geoFire.queryAtLocation(geoLocation, 100000);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            /*user has been found/entered within the radius
             * "key" is the child node under "User Locations" which is the "user id" in my project*/
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                /*adding the user ids (keys) to the arraylist*/
                nearbyUserIDs.add(key);
                Log.d(TAG, "onKeyEntered: nearbyUserIDs " + nearbyUserIDs);

                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key))
                        return;
                }
                final LatLng userLocation = new LatLng(location.latitude, location.longitude);

                //new code
                addMapMarkers(key, userLocation);

               /* View marker_layout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
                final MarkerOptions markerOptions = new MarkerOptions()
                        .position(userLocation)
                        .title(key)
                        .icon(BitmapDescriptorFactory.defaultMarker());
                        //.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(MapsActivity.this, marker_layout)));
                Marker userMarker = mGoogleMap.addMarker(markerOptions);*/
              /*  Marker userLocationMarker = mGoogleMap.addMarker(markerOptions);
                userLocationMarker.setTag(key);
                *//*add the markers to the arraylist<Marker> markers*//*
                markers.add(userLocationMarker);
                Log.d(TAG, "onKeyEntered: marker tag is" + userLocationMarker.getTag());*/


            }//END OF onKeyEntered

            /*when the user exited/left the radius*/
            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, "onKeyExited: called");
                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key)) {
                        markerIt.remove();
                    }
                }
            }

            /*when the user moves within the radius*/
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker markerIt : markers) {
                    if (markerIt.getTag().equals(key)) {
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            /*all users within the radius have been identified*/
            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void addMapMarkers(final String key, final LatLng userLocation) {
        if (mGoogleMap != null) {

            mPostsDatabaseReference.addValueEventListener(new ValueEventListener() {
                ClusterMarker newClusterMarker;

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        /*show on map only the users that have posts by following the steps
                         * check if the user (key) exists in mPostsDatabaseReference
                         * if it exists fetch all of its childern and put them in an arraylist
                         * */
                        if (dataSnapshot.hasChild(key)) {
                            Log.d(TAG, "onDataChange: key: " + key + " exists");
                            clusterPostsDetailsArrayList = new ArrayList<>();
                            mPostsDatabaseReference.child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    NewPostDetails postDetail;
                                    String postImageUrl;
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                            String childPostKey = dataSnapshot1.getKey();
                                            postDetail = dataSnapshot1.getValue(NewPostDetails.class);
                                            clusterPostsDetailsArrayList.add(postDetail);
                                            Log.d(TAG, "onDataChange: postKeys: " + childPostKey + "postType : " + postDetail.getPostType());
                                        }
                                        /*postsDetailsArrayList.get(postsDetailsArrayList.size()-1) returns the last item added to the firsbase*/
                                        postImageUrl = clusterPostsDetailsArrayList.get(clusterPostsDetailsArrayList.size() - 1).getPostImageUrl();
                                        Log.d(TAG, "onDataChange: lastpostimageurl: " + postImageUrl);
                                        /*create a new cluster marker*/
                                        /*ClusterMarker(LatLng position, String title, String snippet, String markerImage)*/
                                        newClusterMarker = new ClusterMarker(userLocation, "Shavo", key, postImageUrl);

                                        /*show only the markers within the given radius from the search intent*/
                                        float[] distance = new float[2];
                                        Location.distanceBetween(mCurrentLocationLatLng.latitude, mCurrentLocationLatLng.longitude,
                                                userLocation.latitude, userLocation.longitude, distance);
                                        Log.d(TAG, "addMapMarkers: distance[0]: " + distance[0]);
                                        try {
                                            if (distanceRadius == null) {
                                                /*when distanceRadius is null, it means that there is no search query
                                                 * so, add all the markers to the map regardless of the distance*/
                                                mClusterManager.addItem(newClusterMarker);
                                            } else {
                                                double distanceInMetres = Integer.parseInt(distanceRadius);
                                                if (distance[0] < distanceInMetres) {
                                                    Log.d(TAG, "addMapMarkers: " + key + " is inside the radius");
                                                    mClusterManager.addItem(newClusterMarker);
                                                    mClusterManager.cluster();
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        //*add the marker to the map*//*
                                        //mClusterManager.addItem(newClusterMarker);
                                        /*add the markers to an array of markers (optional) */
                                        mClusterMarkers.add(newClusterMarker);
                                        //*lunch the cluster on the map*//*
                                        mClusterManager.cluster();

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            //newClusterMarker = new ClusterMarker(userLocation, "Shavo","hello", R.drawable.add_new_post);
                        } else {
                            /*Log.d(TAG, "onDataChange: key: "+key+" doesn't exists");
                            newClusterMarker = new ClusterMarker(userLocation, "shavgar","hiiii",
                                    "https://firebasestorage.googleapis.com/v0/b/maps-2018-ed100.appspot.com/o/Users%20Posts%20Images%2FsAI17i4bbuUgBvqVUVMpZroM52i2%2F05-04-2019%2002%3A31%3A22jpg?alt=media&token=a026884c-3ccb-45a5-84fa-0895a60d48ee", "hfhgfjgjjhg");
                            //*add the markers to the map*//*
                            mClusterManager.addItem(newClusterMarker);
                            //*add the markers to array of markers*//*
                            mClusterMarkers.add(newClusterMarker);
                            //*lunch the cluster on the map*//*
                            mClusterManager.cluster();*/
                        }
                    }
                }//END OF onDataChange()

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }//END OF if(mGoogleMap != null)
    }//END OF addMapMarkers()

    /*This method gets called after the map is successfully loaded*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called");
        mGoogleMap = googleMap;
        if (mLocationPermissionsGranted) {
            /*This request is required because of mGoogleMap.setMyLocationEnabled(true) */
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access user location is not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            /*mGoogleMap.setMyLocationEnabled(true) puts the blue dot on the location*/
            mGoogleMap.setMyLocationEnabled(true);
            getCurrentUserLocation();

        }//end of (mLocationPermissionsGranted)

        if (mClusterManager == null) {
            mClusterManager = new ClusterManager<ClusterMarker>(this, mGoogleMap);
        }
        if (mClusterManagerRender == null) {
            mClusterManagerRender = new ClusterManagerRender(this, mGoogleMap, mClusterManager);
            mClusterManager.setRenderer(mClusterManagerRender);
        }

        /*cluster the marker properly*/
        mGoogleMap.setOnCameraIdleListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);

        /*show or hide the card when the marker is clicked*/
        clickOnMapMarker();
        //zoom out the map
        mapZoomOut.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomOut());
                return false;
            }
        });
        mapZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

    }//END OF onMapReady()

    /*when a marker is clicked, show the product details*/
    private void clickOnMapMarker() {
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //new code
                /*show only the markers within the given radius from the search intent*/
                float[] distance = new float[2];
                Location.distanceBetween(mCurrentLocationLatLng.latitude, mCurrentLocationLatLng.longitude,
                        marker.getPosition().latitude, marker.getPosition().longitude, distance);
                float distanceInKilometers = distance[0]/1000;

                Toast.makeText(MapsActivity.this, "The distance s about "+Math.round(distanceInKilometers*10)/10.0+" km", Toast.LENGTH_SHORT).show();
                //end

                //moveCamera(marker.getPosition(), 12);
                UserId = marker.getSnippet();
                marker.hideInfoWindow();
                Log.d(TAG, "onMarkerClick: useridsnippet : " + UserId + "  markerTag: " + marker.getTag());
                if (UserId != null) {
                    /*remove the recyclerview items*/
                    mapsActivityRecyclerView.setAdapter(null);
                    showShimmer();
                    /*show the content placeholder layout*/
                    if (appBarLayout.getTop() < 0) {
                        appBarLayout.setExpanded(true);
                        /*keep the content place holder for 2 seconds
                         * then hide it and show the recyclerview items*/
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cardViewSlideshow(UserId);
                                hideShimmer();
                            }
                        }, 2000);
                    } else {
                        /* appBarLayout.setExpanded(false);*/
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cardViewSlideshow(UserId);
                                hideShimmer();
                            }
                        }, 2000);
                    }//END OF IF & ELSE

                }

                return true;
            }
        });

    }//END OF clickOnMapMarker()


    /*Move the camera to the user location*/
    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: called");

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        /*creat circle around my location, when there is a search request*/
        try {
            if (!distanceRadius.isEmpty()) {
                drawMarkerWithCircle(latLng, (double) Integer.parseInt(distanceRadius));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f));
                addPostButton.setVisibility(View.GONE);
                cancelSearchButton.setVisibility(View.VISIBLE);
                bottomNavigationView.setVisibility(View.GONE);
                Log.d(TAG, "moveCamera: distanceRadiusDouble= " + (double) Integer.parseInt(distanceRadius));
            } else {
                cancelSearchButton.setVisibility(View.GONE);
                addPostButton.setVisibility(View.VISIBLE);
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        //drawMarkerWithCircle(latLng);

    }//END OF moveCamera()

    /*draw circle around my loction when searching for nearby posts*/
    private void drawMarkerWithCircle(LatLng position, final double radiusInMeters) {
        //final double radiusInMeters = 1000.0;
        final Circle circle = mGoogleMap.addCircle(new CircleOptions().center(position)
                .strokeColor(Color.RED).radius(radiusInMeters).fillColor(0x44ff0000));

        ValueAnimator vAnimator = new ValueAnimator();
        vAnimator.setRepeatCount(ValueAnimator.INFINITE);
        vAnimator.setRepeatMode(ValueAnimator.RESTART);  /* PULSE */
        vAnimator.setIntValues(0, 100);
        vAnimator.setDuration(3000);
        vAnimator.setEvaluator(new IntEvaluator());
        vAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                // Log.e("", "" + animatedFraction);
                circle.setRadius(animatedFraction * radiusInMeters);
            }
        });
        vAnimator.start();


       /* double radiusInMeters = 100000.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mGoogleMap.addCircle(circleOptions);*/

    }

    /*create marker on the location*/
    public void createMarker(LatLng latLng) {
        Log.d(TAG, "createMarker: called");

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Shavooooooooo");
        View marker_layout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        /* markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_layout)));*/
        if (currentUserLocationMarker == null) {
            //currentUserLocationMarker = mGoogleMap.addMarker(markerOptions);
            currentUserLocationMarker = mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLng));
        } else {
            /*move the marker to the new location update coordinates*/
            MarkerAnimation.animateMarkerToGB(currentUserLocationMarker, latLng, new LatLngInterpolator.Spherical());
        }

    }//END OF createMarker()


    /*add the cards to the horizontal slideshow*/
    public void cardViewSlideshow(final String userid) {
        Log.d(TAG, "cardSlideShow: called");

        postsArrayList = new ArrayList<>();
        mPostsDatabaseReference.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                NewPostDetails postDetails;
                postsArrayList.clear();
                if (dataSnapshot.exists()) {
                    /*looping through all the nodes */
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        /*get the post objects from the firebase database*/
                        postDetails = data.getValue(NewPostDetails.class);
                        /*get the key(child node or id) for each post
                         * add the each post key to its object in order to be able to edit the post later */
                        postDetails.setPostKey(data.getKey());
                        /*check if the post belongs to the current user or to ther users
                         * if it belongs to the current user, the post will be editable
                         * if it belongs to other users, the post won't be editable*/
                        if (currentUserid.equals(userid)) {
                            postDetails.setPostEditable(true);
                        }
                        /*assign the user id to newPostDetails object in order to
                         * assign the user profile to the end of each post
                         * assign the user name to the end of each post*/
                        postDetails.setUserid(userid);
                        postsArrayList.add(postDetails);
                        Log.d(TAG, "onDataChange: description" + postDetails.getPostDescription());
                    }
                    adapter = new PostsAdapter(MapsActivity.this, postsArrayList);
                    mapsActivityRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }//END OF cardViewSlideshow()


    /*Convert a view to bitmap
    www.nasc.fr/android/android-using-layout-as-custom-marker-on-google-map-api/*/
    public static Bitmap createDrawableFromView(Context context, View view) {
        Log.d(TAG, "createDrawableFromView: called");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;

    }//END OF createDrawableFromView()

    private void hideShimmer() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
    }

    private void showShimmer() {
        shimmerLayout.startShimmer();
        shimmerLayout.setVisibility(View.VISIBLE);
    }


}//end of class
