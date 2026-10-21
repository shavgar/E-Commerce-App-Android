package com.example.android.mymap.ui;

import com.google.android.material.appbar.AppBarLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.android.mymap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class CustomInfoWindow extends AppCompatActivity implements OnMapReadyCallback {

    private Button btn;
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infowindow);

        /*SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.actvity_infowindow_map);
        mapFragment.getMapAsync(CustomInfoWindow.this);*/

      appBarLayout = findViewById(R.id.appBarLayout);
      //appBarLayout.setExpanded(false);
      btn = findViewById(R.id.actvity_infowindow_add_post_button);
      btn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              if (appBarLayout.getTop() < 0)
                  appBarLayout.setExpanded(true);
              else
                  appBarLayout.setExpanded(false);

          }
      });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}

// Setting a custom info window adapter for the google map
//        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

// Use default InfoWindow frame

/*
@Override
public View getInfoWindow(Marker marker) {
        return null;
        }

// Defines the contents of the InfoWindow
@Override
public View getInfoContents(Marker marker) {
        Log.d(TAG, "getInfoContents: called");

        View view = null;
        try {
        view = getLayoutInflater().inflate(R.layout.activity_infowindow, null);
        // Getting reference to the TextView to set latitude
                   */
/* TextView addressTxt = (TextView) findViewById(R.id.title);
                    addressTxt.setText(marker.getTitle());
                    Button button = (Button) findViewById(R.id.button);
                    button.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), "You Clicked", Toast.LENGTH_LONG).show();
                        }
                    });*//*


        } catch (Exception e) {
        System.out.print(e.getMessage());
        }
        return view;
        }
        });*/
