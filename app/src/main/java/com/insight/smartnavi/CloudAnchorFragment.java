package com.insight.smartnavi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CloudAnchorFragment extends ArFragment implements OnMapReadyCallback {

    private Scene arScene;
    private Button resolveButton;
    private AnchorNode anchorNode;
    private ModelRenderable andyRenderable;
    private GoogleMap myMap;

    private LatLng latLng1,latLng2;


    //Firebase
    private static final String TAG = "sop";
    private static final String KEY_ROOT_DIR = "all_cloud_anchor";
    private static final String KEY_NEXT_SHORT_CODE = "next_short_code";
    private static final String KEY_PREFIX = "anchor;";
    private static final int INITIAL_SHORT_CODE = 142;
    private  DatabaseReference rootRef=null;

    //LOCATION
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



    //Snackbar
    private static final int BACKGROUND_COLOR = 0xbf323232;
    private Snackbar messageSnackbar;

    @Override
    public void onMapReady(GoogleMap googleMap) {

        myMap = googleMap;
        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setCompassEnabled(false);
        myMap.getUiSettings().setMyLocationButtonEnabled(false);
        initializeLocationCallback();
        initializeLocationRequest();



        System.out.println(myLat+"  "+myLon+" ");
    //    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng,12f);
      //  myMap.animateCamera(cu);

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());






    }

    private enum DismissBehavior { HIDE, SHOW, FINISH };
    private int maxLines = 2;
    private String lastMessage = "";






    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void onAttach(Context context) {
        super.onAttach(context);

        ModelRenderable.builder()
                .setSource(context, R.raw.model)
                .build()
                .thenAccept(renderable -> andyRenderable = renderable);


    }
    private synchronized void onResolveButtonPressed() {
        ResolveDialog();
    }
    private void ResolveDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        final EditText edittext = new EditText(getContext());
        alert.setTitle("Resolver");

        alert.setView(edittext);

        alert.setPositiveButton("resolve", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                //OR
                String code = edittext.getText().toString();
                onShortCodeEntered(Integer.parseInt(code));

            }
        });

        alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();

    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate from the Layout XML file.
        View rootView = inflater.inflate(R.layout.cloud_anchor_fragment, container, false);
        LinearLayout arContainer = rootView.findViewById(R.id.ar_container);

        Bundle b=getArguments();

        System.out.println(b.get("Dest"));
        System.out.println();
        System.out.println(b.get("Lng"));

        String l1,l2;
        l1=b.getString("Lat");
        l2=b.getString("Lng");
        System.out.println(l1+l2);
        latLng2=new LatLng(Double.parseDouble(l1),Double.parseDouble(l2));



        //   FragmentManager fm = getActivity().getSupportFragmentManager();
       // Fragment frag = fm.findFragmentById(R.id.maap);
   //     SupportMapFragment=new SupportMapFragment(frag);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maap);
        mapFragment.getMapAsync(this);

        //LOCATION
        initializeLocationCallback();
        initializeLocationRequest();

        //FusedLocationProviedrClient Initialized
        myFusedLocationProviderClient = new FusedLocationProviderClient(getActivity());



        // Call the ArFragment's implementation to get the AR View.
        View arView = super.onCreateView(inflater, arContainer, savedInstanceState);
        arContainer.addView(arView);





        FirebaseApp firebaseApp = FirebaseApp.initializeApp(getContext());
        rootRef = FirebaseDatabase.getInstance(firebaseApp).getReference().child(KEY_ROOT_DIR);
        DatabaseReference.goOnline();

        Button clearButton = rootView.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> onClearButtonPressed());

        resolveButton = rootView.findViewById(R.id.resolve_button);
        resolveButton.setOnClickListener(v -> onResolveButtonPressed());

        arScene = getArSceneView().getScene();
        arScene.addOnUpdateListener(frameTime -> onUpdate());
        setOnTapArPlaneListener((hitResult, plane, motionEvent) -> onArPlaneTap(hitResult));
        return rootView;
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = super.getSessionConfiguration(session);
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
        return config;
    }

    private synchronized void onArPlaneTap(HitResult hitResult) {
        if (anchorNode != null) {
            // Do nothing if there was already an anchor in the Scene.
            return;
        }
        Anchor anchor = hitResult.createAnchor();
        setNewAnchor(anchor);

        // The next line is the new addition.
        resolveButton.setEnabled(false);

        showMessage(getActivity(), "Now hosting anchor...");
        hostCloudAnchor(
                getArSceneView().getSession(), anchor, this::onHostedAnchorAvailable);
    }

    private synchronized void onClearButtonPressed() {
        // Clear the anchor from the scene.
        clearListeners();

        // The next line is the new addition.
        resolveButton.setEnabled(true);

        setNewAnchor(null);
    }
    // Modify the renderables when a new anchor is available.
    private synchronized void setNewAnchor(@Nullable Anchor anchor) {
        if (anchorNode != null) {
            // If an AnchorNode existed before, remove and nullify it.
            arScene.removeChild(anchorNode);
            anchorNode = null;
        }
        if (anchor != null) {
            if (andyRenderable == null) {
                // Display an error message if the renderable model was not available.
                Toast toast = Toast.makeText(getContext(), "Andy model was not loaded.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            // Create the Anchor.
            anchorNode = new AnchorNode(anchor);
            arScene.addChild(anchorNode);

            // Create the transformable andy and add it to the anchor.
            TransformableNode andy = new TransformableNode(getTransformationSystem());
            andy.setParent(anchorNode);
            andy.setRenderable(andyRenderable);
            andy.select();
        }
    }

    private synchronized void onHostedAnchorAvailable(Anchor anchor) {
        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
        if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            String cloudAnchorId = anchor.getCloudAnchorId();
          nextShortCode(shortCode -> {
                if (shortCode != null) {
                    storeUsingShortCode(shortCode, cloudAnchorId);
                    showMessage(getActivity(), "Cloud Anchor Hosted. Short code: " + shortCode);
                } else {
                    // Firebase could not provide a short code.
                    showMessage(getActivity(), "Cloud Anchor Hosted, but could not "
                                    + "get a short code from Firebase.");
                }
            });
            setNewAnchor(anchor);
        } else {
            showMessage(getActivity(), "Error while hosting: " + cloudState.toString());
        }
    }
    private synchronized void onShortCodeEntered(int shortCode) {
        getCloudAnchorId(shortCode, cloudAnchorId -> {
            if (cloudAnchorId == null || cloudAnchorId.isEmpty()) {
                showMessage(
                        getActivity(),
                        "A Cloud Anchor ID for the short code " + shortCode + " was not found.");
                return;
            }
            resolveButton.setEnabled(false);
            resolveCloudAnchor(
                    getArSceneView().getSession(),
                    cloudAnchorId,
                    anchor -> onResolvedAnchorAvailable(anchor, shortCode));
        });
    }

    private synchronized void onResolvedAnchorAvailable(Anchor anchor, int shortCode) {
        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
        if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            showMessage(getActivity(), "Cloud Anchor Resolved. Short code: " + shortCode);
            setNewAnchor(anchor);
        } else {
            showMessage(
                    getActivity(),
                    "Error while resolving anchor with short code "
                            + shortCode
                            + ". Error: "
                            + cloudState.toString());
            resolveButton.setEnabled(true);
        }
    }




    //Snackbar




    public void showMessage(Activity activity, String message) {
        if (!message.isEmpty() && (messageSnackbar == null || !lastMessage.equals(message))) {
            lastMessage = message;
            show(activity, message, DismissBehavior.HIDE);
        }
    }

    private void show(
            final Activity activity, final String message, final DismissBehavior dismissBehavior) {
        activity.runOnUiThread(() -> {
            messageSnackbar =
                    Snackbar.make(
                            activity.findViewById(android.R.id.content),
                            message, Snackbar.LENGTH_INDEFINITE);
            messageSnackbar.getView().setBackgroundColor(BACKGROUND_COLOR);
            if (dismissBehavior != DismissBehavior.HIDE) {
                messageSnackbar.setAction("Dismiss", v -> messageSnackbar.dismiss());
                if (dismissBehavior == DismissBehavior.FINISH) {
                    messageSnackbar.addCallback(
                            new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                @Override
                                public void onDismissed(Snackbar transientBottomBar, int event) {
                                    super.onDismissed(transientBottomBar, event);
                                    activity.finish();
                                }
                            });
                }
            }
            ((TextView)
                    messageSnackbar
                            .getView()
                            .findViewById(com.google.android.material.R.id.snackbar_text))
                    .setMaxLines(maxLines);
            messageSnackbar.show();
        });
    }





    //Firebase
    public void nextShortCode(ShortCodeListener listener) {
        // Run a transaction on the node containing the next short code available. This increments the
        // value in the database and retrieves it in one atomic all-or-nothing operation.
        rootRef
                .child(KEY_NEXT_SHORT_CODE)
                .runTransaction(
                        new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData currentData) {
                                Integer shortCode = currentData.getValue(Integer.class);
                                if (shortCode == null) {
                                    // Set the initial short code if one did not exist before.
                                    shortCode = INITIAL_SHORT_CODE - 1;
                                }
                                currentData.setValue(shortCode + 1);
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(
                                    DatabaseError error, boolean committed, DataSnapshot currentData) {
                                if (!committed) {
                                    Log.e(TAG, "Firebase Error", error.toException());
                                    listener.onShortCodeAvailable(null);
                                } else {
                                    listener.onShortCodeAvailable(currentData.getValue(Integer.class));
                                }
                            }
                        });
    }

    /** Stores the cloud anchor ID in the configured Firebase Database. */
    public void storeUsingShortCode(int shortCode, String cloudAnchorId) {
        rootRef.child(KEY_PREFIX + shortCode).setValue(cloudAnchorId);
    }

    /**
     * Retrieves the cloud anchor ID using a short code. Returns an empty string if a cloud anchor ID
     * was not stored for this short code.
     */
    public void getCloudAnchorId(int shortCode, CloudAnchorIdListener listener) {
        rootRef
                .child(KEY_PREFIX + shortCode)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Listener invoked when the data is successfully read from Firebase.
                                listener.onCloudAnchorIdAvailable(String.valueOf(dataSnapshot.getValue()));
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.e(
                                        TAG,
                                        "The Firebase operation for getCloudAnchorId was cancelled.",
                                        error.toException());
                                listener.onCloudAnchorIdAvailable(null);
                            }
                        });
    }

    public interface CloudAnchorIdListener {
        void onCloudAnchorIdAvailable(String cloudAnchorId);
    }

    /** Listener for a new short code from the Firebase Database. */
    public interface ShortCodeListener {
        void onShortCodeAvailable(Integer shortCode);
    }



//cloudmanager

    public interface CloudAnchorListener {

        /** This method is invoked when the results of a Cloud Anchor operation are available. */
        void onCloudTaskComplete(Anchor anchor);
    }

    private final HashMap<Anchor, CloudAnchorListener> pendingAnchors = new HashMap<>();

    /**
     * This method hosts an anchor. The {@code listener} will be invoked when the results are
     * available.
     */
    public synchronized void hostCloudAnchor(
            Session session, Anchor anchor, CloudAnchorListener listener) {
        Anchor newAnchor = session.hostCloudAnchor(anchor);
        pendingAnchors.put(newAnchor, listener);
    }

    /**
     * This method resolves an anchor. The {@code listener} will be invoked when the results are
     * available.
     */
    public synchronized void resolveCloudAnchor(
            Session session, String anchorId, CloudAnchorListener listener) {
        Anchor newAnchor = session.resolveCloudAnchor(anchorId);
        pendingAnchors.put(newAnchor, listener);
    }

    /** Should be called after a {@link Session#update()} call. */
    public synchronized void onUpdate() {
        Iterator<Map.Entry<Anchor, CloudAnchorListener>> iter = pendingAnchors.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Anchor, CloudAnchorListener> entry = iter.next();
            Anchor anchor = entry.getKey();
            if (isReturnableState(anchor.getCloudAnchorState())) {
                CloudAnchorListener listener = entry.getValue();
                listener.onCloudTaskComplete(anchor);
                iter.remove();
            }
        }
    }

    /** Used to clear any currently registered listeners, so they wont be called again. */
    public synchronized void clearListeners() {
        pendingAnchors.clear();
    }

    private static boolean isReturnableState(Anchor.CloudAnchorState cloudState) {
        switch (cloudState) {
            case NONE:
            case TASK_IN_PROGRESS:
                return false;
            default:
                return true;
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
                latLng1=new LatLng(myLat,myLon);

                if(myLon!=myLat) {
                    latLng1 = new LatLng(myLat, myLon);
                    MarkerOptions marker1 = new MarkerOptions().position(latLng1).title("Starting point");
                    myMap.addMarker(marker1);
                    MarkerOptions marker2 = new MarkerOptions().position(latLng2).title("Destination");
                    myMap.addMarker(marker2);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(latLng1);
                    builder.include(latLng2);

                    LatLngBounds bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
                    myMap.animateCamera(cu);
                }

                String temp = "data : " + myLat +"  " + myLon;
                System.out.println(temp);
           /*     if(connectedToNetwork()){volley(UPDATE_LOCATION);}
                else{NoInternetAlertDialog(UPDATE_LOCATION);}*/


            }
        };
    }



    @SuppressWarnings("MissingPermission")
    private void startLocationUpdate(){
        LocationServices.getSettingsClient(getActivity()).checkLocationSettings(myLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        myFusedLocationProviderClient.requestLocationUpdates(myLocationRequest, myLocationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        switch (((ApiException)e).getStatusCode()){
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try{
                                    ResolvableApiException rae = (ResolvableApiException)e;
                                    rae.startResolutionForResult(getActivity(), LOCATION_SETTINGS_REQUEST_CODE);
                                }catch(IntentSender.SendIntentException sie){
                                    //UNABLE TO EXECUTE REQUEST
                                    Toast.makeText(getActivity(),"Location settings are inadequate. Please, fix in settings.", Toast.LENGTH_LONG).show();
                                }
                                break;

                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Toast.makeText(getActivity(),"Location settings are inadequate. Please, fix in settings.", Toast.LENGTH_LONG).show();
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
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkPhonePermission(){
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
            showSnackbar("location rationale", "ok", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            });
        }
        else{
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void requestPhonePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE)){
            showSnackbar("location permisson rationale", "ok", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.CALL_PHONE},
                            PHONE_PERMISSION_REQUEST_CODE);
                }
            });
        }
        else{
            ActivityCompat.requestPermissions(getActivity(),
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
                  //   mMap.setMyLocationEnabled(true);
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

        }


    //SNACK BAR
    private void showSnackbar(final String mainTextStringId, final String actionStringId, View.OnClickListener listener) {

        Snackbar.make(getActivity().findViewById(android.R.id.content), mainTextStringId, Snackbar.LENGTH_INDEFINITE).setAction(actionStringId, listener).show();
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


}
