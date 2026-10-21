package com.example.android.mymap.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.mymap.R;
import com.facebook.shimmer.ShimmerDrawable;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import de.hdodenhof.circleimageview.CircleImageView;

public class ClusterManagerRender extends DefaultClusterRenderer<ClusterMarker> {
    /*IconGenerator responsible for rendering the markers images*/
    private final IconGenerator iconGenerator;
    private final ImageView markerImageView;
    private final int markerImageViewWidth;
    private final int markerImageViewHeight;
    private final Context context;
    //ShimmerFrameLayout container;

    public ClusterManagerRender(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);
        // initialize cluster item icon generator
        iconGenerator = new IconGenerator(context.getApplicationContext());
        iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
        markerImageView = new ImageView(context.getApplicationContext());
        markerImageViewWidth = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        markerImageViewHeight = (int) context.getResources().getDimension(R.dimen.custom_marker_image);
        markerImageView.setLayoutParams(new ViewGroup.LayoutParams(markerImageViewWidth, markerImageViewHeight));
        //markerImageView.setBackgroundColor(Color.TRANSPARENT);
        markerImageView.setBackground(context.getResources().getDrawable(R.drawable.ic_circle_white));
        int padding = 10;
        markerImageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(markerImageView);
        this.context = context;
    }

    /**
     * Rendering of the individual ClusterItems
     *
     * @param item
     * @param markerOptions
     */
    @Override
    protected void onBeforeClusterItemRendered(final ClusterMarker item, MarkerOptions markerOptions) {

        Picasso.get().load(item.getMarkerImage()).networkPolicy(NetworkPolicy.OFFLINE)
                .fit().into(markerImageView, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(item.getMarkerImage()).fit().into(markerImageView);
            }
        });
        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());


    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ClusterMarker> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterMarker> cluster) {
        //return super.shouldRenderAsCluster(cluster);
        if (cluster.getSize() > 2) {
            return true;
        }// if markers <=2 then not clustering
        return false;

    }
}
