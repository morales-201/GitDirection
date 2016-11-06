package com.comp.jpb.gitdirection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.location.Location;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;




public class MainActivity
        extends AppCompatActivity
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, SensorEventListener
{

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    TextView tvHeading;

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
    private boolean gotLastAccSet = false;
    private boolean gotLastMagnetSet = false;
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
            gotLastAccSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetic = event.values;
            gotLastMagnetSet = true;
        }
        if (gotLastAccSet && gotLastMagnetSet) {
            float O[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(O, null, gravity, magnetic);
            if (success) {
                float orientation[] = new float[3];
                sensorManager.getOrientation(O, orientation);
                azimuthValue = orientation[0];
                azimuthValue = (float)(Math.toDegrees(azimuthValue)+360)%360;
                azimuthValue = Math.round(azimuthValue);

                tvHeading.setText("Heading: " + Float.toString(azimuthValue) + " degrees");

                // create a rotation animation (reverse turn degree degrees)
                RotateAnimation ra = new RotateAnimation(
                        currentDegree,
                        -azimuthValue,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);

                // how long the animation will take place
                ra.setDuration(1000);

                // set the animation after the end of the reservation status
                ra.setFillAfter(true);

                // Start the animation
                image.startAnimation(ra);
                currentDegree = -azimuthValue;
            }
        }
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
        // to stop the listener and save battery
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ApiClient.isConnected() && !updatingLocation){
            startLocationUpdates();
        }
        // for the system's orientation sensor registered listeners
        if(sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    SensorManager.SENSOR_DELAY_GAME);
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

        tvHeading = (TextView) findViewById(R.id.tvHeading);
        image = (ImageView) findViewById(R.id.imageViewCompass);
        coords = (TextView) findViewById(R.id.compass_text);
        String coordsString;
        if (lastLocation != null){
            coordsString = String.format("Latitude: %f \n Longitude: %f",
                    lastLocation.getLatitude(), lastLocation.getLongitude());
        } else{
            coordsString = "Location not Gotten :(";
        }
        coords.setText(coordsString);

        buildSensorManager();
        startSensorUpdates();

        azimuthText = (TextView) findViewById(R.id.azimuth_text);
        String azimuthString;
        if (azimuthValue != 9001){
           azimuthString = Integer.toString((int)azimuthValue);
        }else{
            azimuthString = "Azimuth not gotten :(";
        }
        azimuthText.setText(azimuthString);

    }

    public void showMainPageAndStopLocationUpdatesAndSensorUpdates(View view){
        setContentView(R.layout.activity_main);

        // our compass image
        image = (ImageView) findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        stopLocationUpdates();
        stopSensorUpdates();
    }
}