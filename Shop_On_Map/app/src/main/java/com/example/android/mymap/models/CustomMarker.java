package com.example.android.mymap.models;

public class CustomMarker {
    private String makerImage;
    private String markerColor;

    public CustomMarker(String makerImage) {
        this.makerImage = makerImage;
    }
    public CustomMarker() {

    }

    public String getMakerImage() {
        return makerImage;
    }

    public void setMakerImage(String makerImage) {
        this.makerImage = makerImage;
    }

    public String getMarkerColor() {
        return markerColor;
    }

    public void setMarkerColor(String markerColor) {
        this.markerColor = markerColor;
    }
}
