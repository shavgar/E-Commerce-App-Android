package com.example.android.mymap.models;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/*check network connection state in the background of the application
* check network connection state periodically*/
public class InternetConnectivityCheck extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int networkTypes[] = {ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI};
        if (isNetworkAvailbale(context, networkTypes)){
           /*do nothing*/
            return;
        }else{
            Toast.makeText(context, "No network connection", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isNetworkAvailbale(Context context, int [] types){
        try{
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : types){
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }
}
