package com.example.android.mymap.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.auth.User;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;
    private String markerImage;

    public ClusterMarker() {

    }

    public ClusterMarker(LatLng position, String title, String snippet, String markerImage) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.markerImage = markerImage;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getMarkerImage() {
        return markerImage;
    }

    public void setMarkerImage(String markerImage) {
        this.markerImage = markerImage;
    }

}
