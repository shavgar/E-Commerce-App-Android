package com.example.android.mymap.models;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class OfflineFirebase extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /*By enabling persistence, any data that the Firebase Realtime Database client would sync while online persists to disk
         *and is available offline, even when the user or operating system restarts the app.
         *This means your app works as it would online by using the local data stored in the cache
         * Note: this only loads texts offline not images*/
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        /*load images offline*/
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
