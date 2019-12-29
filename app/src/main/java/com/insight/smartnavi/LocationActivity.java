package com.insight.smartnavi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcel;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LocationActivity extends AppCompatActivity {

    EditText textLoc;
    //LatLng
    LatLng latLng1,latLng2, latLng3, latLng4;

    String Dest="";
 Button btnext;

    private FusedLocationProviderClient myFusedLocationProviderClient;
    private Location myCurrentLocation;
    private LocationRequest myLocationRequest;
    private LocationSettingsRequest myLocationSettingsRequest;
    private LocationCallback myLocationCallback;
    private double myLat, myLon;
    private static final long locationInterval = 2000; //in milliseconds
    private static final long fastestLocationInterval = locationInterval/2; //in milliseconds
    private static final long minimumDisplacement = 5; //in meter

    private static final int LOCATION_SETTINGS_REQUEST_CODE = 0x2;//OPEN SETTING
    private static final int PHONE_PERMISSION_REQUEST_CODE = 0x3; //Phone


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0x1; //LOCATION





    //REQUEST CODE
    private static final int AUTOCOMPLETE_REQUEST_CODE1 = 100;

    private static final int REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        textLoc=findViewById(R.id.textLoc);
        btnext=findViewById(R.id.btnext);

        //PLACES
        Places.initialize(getApplicationContext(), "AIzaSyC6o68BqvRLZ_GMWkFr2f2I-BDwHxqCvLc");
        PlacesClient placesClient = Places.createClient(this);

        //LOCATION
        initializeLocationCallback();
        initializeLocationRequest();

        //FusedLocationProviedrClient Initialized
        myFusedLocationProviderClient = new FusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
        }





      /*  textLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME))
                     .setCountry("IN")
                        .build(LocationActivity.this);

                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE1);
            }
        });*/



        btnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dest=textLoc.getText().toString().trim();
latLng1=getLocationFromAddress(getApplicationContext(),Dest);

                if(!TextUtils.isEmpty(Dest))
                {
                    Intent i=new Intent(LocationActivity.this,MainActivity.class);
                    i.putExtra("Dest",Dest);
                    i.putExtra("Lat",latLng1.latitude+"");
                    i.putExtra("Lng",latLng1.longitude+"");
                    startActivity(i);


                }


            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode) {
            case AUTOCOMPLETE_REQUEST_CODE1:
                if (resultCode == RESULT_OK) {

                    System.out.println("blla");

                    Place place = Autocomplete.getPlaceFromIntent(data);
                 //   startLatitude = place.getLatLng().latitude;
                   // startLongitude = place.getLatLng().longitude;
                    textLoc.setText(place.getName());
                    textLoc.setTextColor(Color.parseColor("#000000"));
                    Dest=place.getName();

                  //  latLng1 = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);

   //                 MarkerOptions marker = new MarkerOptions().position(latLng1).title("starting point").icon(bitmapDescriptorFromVector(this, R.drawable.start));
     //               mMap.addMarker(marker);

//                    System.out.println("MARKER ORIGINAL POSITION : " + startMarker.getPosition());


                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Log.i(" place tag", status.getStatusMessage());
                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                break;
        }
    }



    //LOCATION
    private void initializeLocationRequest(){
        //INITIALIZE
        myLocationRequest = new LocationRequest();
        myLocationRequest.setInterval(locationInterval);
        myLocationRequest.setFastestInterval(fastestLocationInterval);
        myLocationRequest.setSmallestDisplacement(minimumDisplacement);
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //BUILD LOCATION SETTINGS
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(myLocationRequest);
        myLocationSettingsRequest = builder.build();
    }

    private void initializeLocationCallback(){
        myLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                myCurrentLocation = locationResult.getLastLocation();
                myLat = myCurrentLocation.getLatitude();
                myLon = myCurrentLocation.getLongitude();
                String temp = "data : " + myLat +"  " + myLon;
                System.out.println(temp);

           /*     if(connectedToNetwork()){volley(UPDATE_LOCATION);}
                else{NoInternetAlertDialog(UPDATE_LOCATION);}*/


            }
        };
    }



    @SuppressWarnings("MissingPermission")
    private void startLocationUpdate(){
        LocationServices.getSettingsClient(this).checkLocationSettings(myLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        myFusedLocationProviderClient.requestLocationUpdates(myLocationRequest, myLocationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        switch (((ApiException)e).getStatusCode()){
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try{
                                    ResolvableApiException rae = (ResolvableApiException)e;
                                    rae.startResolutionForResult(LocationActivity.this, LOCATION_SETTINGS_REQUEST_CODE);
                                }catch(IntentSender.SendIntentException sie){
                                    //UNABLE TO EXECUTE REQUEST
                                    Toast.makeText(LocationActivity.this,"Location settings are inadequate. Please, fix in settings.", Toast.LENGTH_LONG).show();
                                }
                                break;

                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Toast.makeText(LocationActivity.this,"Location settings are inadequate. Please, fix in settings.", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
    }
    private void stopLocationUpdates(){
        myFusedLocationProviderClient.removeLocationUpdates(myLocationCallback);
    }

    //PERMISSION
    private boolean checkLocationPermission(){
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPhonePermission(){
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            showSnackbar("location rationale", "ok", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(LocationActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            });
        }
        else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void requestPhonePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)){
            showSnackbar("location permisson rationale", "ok", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(LocationActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            PHONE_PERMISSION_REQUEST_CODE);
                }
            });
        }
        else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    PHONE_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==LOCATION_PERMISSION_REQUEST_CODE){
            if(grantResults.length<=0){
                //USER INTERACTION CANCELLED
            }
            else if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(checkLocationPermission()){
                    startLocationUpdate();
                    // mMap.setMyLocationEnabled(true);
                }
            }
            else{
                showSnackbar("enable your location", "settings", new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Intent openSettings = new Intent();
                        openSettings.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                        openSettings.setData(uri);
                        openSettings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(openSettings);
                    }
                });
            }

        }

        if (grantResults.length > 0 && requestCode==REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

            }
        }

    }


    //SNACK BAR
    private void showSnackbar(final String mainTextStringId, final String actionStringId, View.OnClickListener listener) {

        Snackbar.make(this.findViewById(android.R.id.content), mainTextStringId, Snackbar.LENGTH_INDEFINITE).setAction(actionStringId, listener).show();
        //Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onStart() {
        if(checkLocationPermission()){ startLocationUpdate(); }
        else{ requestLocationPermission(); }
        super.onStart();
    }

    @Override
    public void onStop() {
        stopLocationUpdates();
        super.onStop();
    }


    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }
        catch (IndexOutOfBoundsException ex) {

            ex.printStackTrace();
        }


        return p1;
    }



}
