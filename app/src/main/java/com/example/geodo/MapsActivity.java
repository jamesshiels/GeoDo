package com.example.geodo;

import static java.lang.String.valueOf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.geodo.databinding.ActivityMapsBinding;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private int intentNoteID;
    private GoogleMap mMap;
    private Model model;
    private ActivityMapsBinding binding;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private static final String TAG  = "MapsActivity";
    // TODO Change to take the users preference
    public static float GEOFENCE_RADIUS = 150;
    private String geoFenceID;
    private TaskSharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences = new TaskSharedPreferences(this);
        Intent intent = getIntent();
        intentNoteID = intent.getIntExtra("noteID", -1);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // move camera to user location
        // Add a marker in Sydney and move the camera
        LatLng home = new LatLng(55.250843, -7.264409);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 15));
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        enableUserLocation();

        // If geofence already set just show the marker
        Double lat = preferences.getNoteById(valueOf(intentNoteID)).getLatitude();
        if(lat != null || lat != 0.0){
            model = new Model();
            model = preferences.getNoteById(valueOf(intentNoteID));
            LatLng noteLocation = new LatLng(model.getLatitude(), model.getLongitude());
            addMarker(noteLocation);
            addCircle(noteLocation, GEOFENCE_RADIUS);
        }
        mMap.setOnMapLongClickListener(this);
    }

    private void enableUserLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);

        }else{
            // GET PERMISSION
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION_ACCESS_REQUEST_CODE);
            }else{
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Permission is there
                mMap.setMyLocationEnabled(true);
            }else{
                // We need to get the permissions
            }
    }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        addMarker(latLng);
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(valueOf(intentNoteID), latLng, GEOFENCE_RADIUS);
        System.out.println("Note ID: " + intentNoteID);
        System.out.println("String note id + value of: " + valueOf(intentNoteID));
        model = new Model();
        model = preferences.getNoteById(valueOf(intentNoteID));
        model.setLatitude(latLng.latitude);
        model.setLongitude(latLng.longitude);
        System.out.println(model.getLatitude());
        System.out.println(model.getLongitude());
        preferences.saveNote(model);

        ParseQuery<ParseObject> noteQuery = ParseQuery.getQuery("Notes");
        noteQuery.whereEqualTo("Id", valueOf(intentNoteID));
        noteQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null && object != null) {
                    object.put("latitude", model.getLatitude().toString());
                    object.put("longitude", model.getLongitude().toString());
                    object.saveInBackground();
                }else{
                    System.out.println("Error while updating latitude and longitude: " + e.toString());
                }

            }
        });
    }

    @SuppressLint("MissingPermission")
    public void addGeofence(String geoFenceID, LatLng latLng, float radius){
        Geofence geofence = geofenceHelper.getGeofence(geoFenceID, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> {
                    // Geofence added
                    Log.d(TAG, "addGeofence: Geofence Added...");
                })
                .addOnFailureListener(e -> {
                    // Geofence not added
                    Log.d(TAG, "addGeofence: " + e.getMessage());
                });
    }

    private void addMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }

    private void addCircle(LatLng latLng, float radius){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
}