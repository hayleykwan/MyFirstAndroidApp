package hayleykwan.myfirstandroidapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapFragment;


import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final int PERMISSIONS_REQUEST_ACCESS_PHOTOS = 2;
    private boolean locationPermissionGranted;
    private boolean photosPermissionGranted;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private final LatLng mDefaultLocation = new LatLng(27.715298, 85.290431); // Stupa in Kathmandu, Nepal
    private static final int DEFAULT_ZOOM = 15;

    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private Location mCurrentLocation;

    // A request object to store parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;
    private final int UPDATE_LOCATION_IN_MILLISECONDS = 10000;
    private final int FASTEST_UPDATE_IN_MILLISECONDS = 5000;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient googleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_main);
        buildGoogleApiClient();

        googleApiClient.connect();
    }

    protected void onStart() {
        System.out.println("On Start called.");
        super.onStart();
    }

    protected void onStop() {
        System.out.println("On Stop called.");
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        System.out.println("On resume called.");
        super.onResume();
        if (googleApiClient.isConnected()) {
            getDeviceLocation();
        }
        updateMarkers();
    }

    /**
     * Stop location updates when the activity is no longer in focus, to reduce battery consumption.
     */
    @Override
    protected void onPause() {
        System.out.println("On Pause called.");
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        System.out.println("stop location updates called.");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }


    /*********** OnConnectionFailedLister methods */
    /**
     * Gets the device's current location and builds the map
     * when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("On Google API client connected called.");
        //connected with google API client (called from onStart)
        //can request from client and build map
        getDeviceLocation();
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //register for the map callback
        System.out.println("finish layout");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
        System.out.println("Google Play services suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
        System.out.println("Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }
    /************ OnConnectionFailedLister methods */

    /************ LocationLister methods */
    @Override
    public void onLocationChanged(Location location) {
        System.out.println("On location changed.");
        mCurrentLocation = location;
        updateMarkers();
    }
    /************ LocationLister methods */

    /**
     * Builds a GoogleApiClient.
     * Uses the addApi() method to request the Google Places API and the Fused Location Provider.
     */
    private synchronized void buildGoogleApiClient() {
        System.out.println("build Google API client called.");
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        createLocationRequest();
    }

    /*
     * sets up a location request for fused location provider
     */
    private void createLocationRequest(){
        System.out.println("Create location request for fused location provider called");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_LOCATION_IN_MILLISECONDS); //inexact interval due to other apps
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_IN_MILLISECONDS); //exact interval
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    /**
     * Gets the current location of the device and starts the location update notifications.
     */
    @SuppressWarnings("MissingPermission")
    private void getDeviceLocation(){
        System.out.println("get device location called.");
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if(locationPermissionGranted){
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            System.out.println("my current location: " + mCurrentLocation);
//            System.out.println(mCurrentLocation.getLatitude() +", " + mCurrentLocation.getLongitude());

            //request regular updates about the device location.
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        updateLocationUI(); // Turn on the My Location layer and the related control on the map.

        updateMarkers(); // Add markers for nearby places.

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());
                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());
                return infoWindow;
            }
        });

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) { //zoom into current location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    //auto here from requestPermissions, handles result of permission requests from user
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        System.out.println("request permission result called");
        locationPermissionGranted = false;
        photosPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        /* && permissions.length == 1 &&
                        permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION */) {
                    locationPermissionGranted = true;
                    // permission was granted, yay! Do the
                    // location-related task you need to do.

                } else {
                    // permission denied. Disable the functionality that depends on this permission.
                    googleApiClient.disconnect();
                    Toast.makeText(this, "Google Location API disabled", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case PERMISSIONS_REQUEST_ACCESS_PHOTOS: {
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings incl. my location button and layer
     * based on whether the user has granted location permission.
     */
    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        if (locationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mCurrentLocation = null;
        }
    }

    @SuppressWarnings("MissingPermission")
    private void updateMarkers(){
        System.out.println("Update markers called.");
        if(mMap == null){
            return;
        }
        if (locationPermissionGranted){
            // Get the businesses and other points of interest located
                    // nearest to the device's current location.
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(googleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        // Add a marker for each place near the device's current location, with an
                        // info window showing place information.
                        String attributions = (String) placeLikelihood.getPlace().getAttributions();
                        String snippet = (String) placeLikelihood.getPlace().getAddress();
                        if (attributions != null) {
                            snippet = snippet + "\n" + attributions;
                        }

                        mMap.addMarker(new MarkerOptions()
                                .position(placeLikelihood.getPlace().getLatLng())
                                .title((String) placeLikelihood.getPlace().getName())
                                .snippet(snippet));
                    }
                    // Release the place likelihood buffer to prevent memory leak.
                    likelyPlaces.release();
                }
            });
        } else {
            System.out.println("Show default hardcoded location");
            mMap.addMarker(new MarkerOptions()
                    .position(mDefaultLocation)
                    .title(getString(R.string.default_marker_info_title))
                    .snippet(getString(R.string.default_marker_info_snippet)));
        }
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
            //can also put keys to represent whether requesting location updates, and last updated time
            super.onSaveInstanceState(outState);
        }
    }

    static final int TEST = 1;

    /* called when user clicks button */
    public void sendMessage(View view) {
        //code to respond to button
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void startTracking(View view) {
        Intent intent = new Intent(this, RecyclerViewActivity.class);
        startActivityForResult(intent, TEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TEST) {
            if(resultCode == RESULT_OK){
                Toast.makeText(this, data.getStringExtra("testing"), Toast.LENGTH_LONG).show();
            }
        }
    }



    private void checkAccessPhotoPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_ACCESS_PHOTOS);
            }
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }
}
