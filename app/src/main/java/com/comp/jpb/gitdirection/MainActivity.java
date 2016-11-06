package com.comp.jpb.gitdirection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, SensorEventListener
{

    private GoogleApiClient ApiClient;
    private LocationRequest locReq;
    private boolean updatingLocation = false;
    private Location lastLocation;
    private TextView coords;


    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    float[] gravity;
    float[] magnetic;
    private float azimuthValue = 9001;
    private TextView azimuthText;

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

        buildSensorManager();
    }

    private void buildSensorManager(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void startSensorUpdates(){
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    private void stopSensorUpdates(){
        sensorManager.unregisterListener(this);
    }


    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magnetic = event.values;
        if (gravity != null && magnetic != null) {
            float O[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(O, null, gravity, magnetic);
            if (success) {
                float orientation[] = new float[3];
                sensorManager.getOrientation(O, orientation);
                azimuthValue = orientation[0];
                azimuthValue *= 180;
                azimuthValue /= Math.PI;
            }
        }

        String azimuthString;
        if (azimuthValue != 9001){
            azimuthString = Float.toString(azimuthValue);
        }else{
            azimuthString = "Azimuth not gotten :(";
        }
        azimuthText.setText(azimuthString);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

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

        String coordsString;
        if (lastLocation != null){
            coordsString = String.format("Latitude: %f \n Longitude: %f",
                    lastLocation.getLatitude(), lastLocation.getLongitude());
        } else{
            coordsString = "Location not Gotten :(";
        }
        coords.setText(coordsString);
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
        //stopSensorUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ApiClient.isConnected() && !updatingLocation){
            startLocationUpdates();
        }
        //startLocationUpdates();
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
        String coordsString;
        if (lastLocation != null){
            coordsString = String.format("Latitude: %f \n Longitude: %f",
                    lastLocation.getLatitude(), lastLocation.getLongitude());
        } else{
            coordsString = "Location not Gotten :(";
        }
        coords.setText(coordsString);
        
        
        startSensorUpdates();

        azimuthText = (TextView) findViewById(R.id.azimuth_text);
        String azimuthString;
        if (azimuthValue != 9001){
           azimuthString = Float.toString(azimuthValue);
        }else{
            azimuthString = "Azimuth not gotten :(";
        }
        azimuthText.setText(azimuthString);
    }

    public void showMainPageAndStopLocationUpdatesAndSensorUpdates(View view){
        setContentView(R.layout.activity_main);
        stopLocationUpdates();
        stopSensorUpdates();
    }

}
