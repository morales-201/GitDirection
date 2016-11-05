package com.comp.jpb.gitdirection;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.comp.jpb.gitdirection.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.drive.Drive;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import android.Manifest.permission;

public class MainActivity
        extends AppCompatActivity
        implements ConnectionCallbacks, OnConnectionFailedListener
{

    private GoogleApiClient ApiClient;

    private Location lastLocation;

    private String longitude;
    private String latitude;
    private TextView coords;

    private static final String[] LOCATION_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int LOCATION_REQUEST = 1340;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        getLocationPermission();

        buildGoogleApiClient();
    }

    private void getLocationPermission(){
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
        }
    }

    private synchronized void buildGoogleApiClient() {
        ApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStop(){
        super.onStop();

        if (ApiClient.isConnected()){
            ApiClient.disconnect();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        ApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(ApiClient);
        }


    }


    @Override
    public void onConnectionFailed(ConnectionResult result){

    }

    @Override
    public void onConnectionSuspended(int clause){
        ApiClient.connect();
    }




    public void showCompass(View view) {

        setContentView(R.layout.compass_page);

        coords = (TextView) findViewById(R.id.compass_text);
        if (lastLocation != null){
            coords.setText(String.format("Latitude: %f \n Longitude: %f",
                    lastLocation.getLatitude(), lastLocation.getLongitude()));}
        else{
            coords.setText("Location not Gotten :(");
        }
    }

    public void showMainPage(View view){
        setContentView(R.layout.activity_main);
    }

}
