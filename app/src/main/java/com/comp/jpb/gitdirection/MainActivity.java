package com.comp.jpb.gitdirection;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener
{

    private GoogleApiClient ApiClient;
    private LocationRequest locReq;
    private boolean updatingLocation = false;

    private Location lastLocation;

    private TextView coords;

    float userLat = 9001;
    float userLong = 9001;


    //time in ms for compass to update
    private int updateTime = 3000;

    private static final String[] LOCATION_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int LOCATION_REQUEST = 1340;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        getLocationPermission();
        buildGoogleApiClient();
        createLocationRequest();
    }

    private void createLocationRequest(){
        locReq = new LocationRequest();
        locReq.setInterval(updateTime);
        locReq.setFastestInterval(1000);
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    protected void startLocationUpdates() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(ApiClient, locReq, this);
            updatingLocation = true;
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(ApiClient, this);
        updatingLocation = false;
    }

    @Override
    public void onLocationChanged(Location location){
        lastLocation = location;

        if (lastLocation != null) {
            coords.setText(String.format("Latitude: %f \n Longitude: %f",
                    lastLocation.getLatitude(), lastLocation.getLongitude()));
        }
        else {
            coords.setText("Location not Gotten :(");
        }
    }


    @Override
    protected void onStop(){
        super.onStop();

        if (ApiClient.isConnected()){
            ApiClient.disconnect();
        }

        if(updatingLocation){
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        ApiClient.connect();
        if (ApiClient.isConnected() && !updatingLocation){
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ApiClient.isConnected() && !updatingLocation){
            startLocationUpdates();
        }
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

        startLocationUpdates();

        coords = (TextView) findViewById(R.id.compass_text);
        if (lastLocation != null){
            coords.setText(String.format("Latitude: %f \n Longitude: %f",
                    lastLocation.getLatitude(), lastLocation.getLongitude()));}
        else{
            coords.setText("Location not Gotten :(");
        }
    }

    public void showDestination(View view) {
        setContentView(R.layout.destination_page);

        if (userLat < 9001){
            EditText latitude = (EditText)findViewById(R.id.latitude);
            EditText longitude = (EditText)findViewById(R.id.longitude);

            latitude.setText(Float.toString(userLat));
            longitude.setText(Float.toString(userLong));
        }

    }

    public void showSettings(View view) {
        setContentView(R.layout.settings_page);
    }

    public void showInstructions(View view) {

    }

    public void showMainPage(View view){
        setContentView(R.layout.activity_main);
    }

    public void showMainPageFromDestination(View view){
        EditText latitude = (EditText)findViewById(R.id.latitude);
        EditText longitude = (EditText)findViewById(R.id.longitude);

        String userLatString = latitude.getText().toString();
        String userLongString = latitude.getText().toString();

        if (!userLatString.equals("") && !userLongString.equals("")) {
            userLat = Float.parseFloat(userLatString);
            userLong = Float.parseFloat(userLongString);
            setContentView(R.layout.activity_main);
        }

    }

    public void showMainPageAndStopLocationUpdates(View view){
        setContentView(R.layout.activity_main);
        stopLocationUpdates();
    }

}
