package com.foodorder.it.foodorderserver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.foodorder.it.foodorderserver.Common.Common;
import com.foodorder.it.foodorderserver.Remote.IGeoCoordinates;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks,
                                 GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private GoogleMap mMap;

    private final static int PLAY_SERVICES_RESOLUTIONS_REQUEST = 1000;
    private final static int LOCATION_PERMISSIONS_REQUEST = 1001;
    private final static int UPFATE_INTERVAL = 1000;
    private final static int FATEST_INTERVAL = 5000;
    private final static int DISPLACEMENT = 10;

    private Location mLastLocation;
    public GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;

    private IGeoCoordinates mService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order);

        mService = Common.getGeoCodeService();

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {

            requestRunTimePermission();
        }
        else
        {
            if(checkPlayService())
            {
                buildGoogleApiClient();
                createLocationRequest();
            }

        }

        mGoogleApiClient.connect();
        displayLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void displayLocation() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {

            requestRunTimePermission();
        }
        else
        {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mGoogleApiClient.isConnected())
            Toast.makeText(this, "mmmmm", Toast.LENGTH_SHORT).show();

            if(mLastLocation!=null)
            {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                // add mark to your location and move the camera
                LatLng yourLocation = new LatLng(latitude,longitude);
                mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

                // after add marker for your location , add marker for order location and draw route
          //      drawRoute(yourLocation , Common.CurrentRequest.getAddress());

            }
            else
            {
                Toast.makeText(this, "couldn't get the Location", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void drawRoute(LatLng yourLocation, String address) {

        mService.getGeoCode(address).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try
                {
                    JSONObject jsonObject = new JSONObject(response.body().toString());

                     String lat = ((JSONArray)jsonObject.get("results"))
                                  .getJSONObject(0)
                                  .getJSONObject("geometry")
                                  .getJSONObject("location")
                                  .get("lat").toString();


                    String lon = ((JSONArray)jsonObject.get("results"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lng").toString();

                    LatLng orderLocation =  new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.food_box);
                    bitmap = Common.scaleBitmap(bitmap ,70,70);

                    MarkerOptions mark = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                       .title("order of "+Common.CurrentRequest.getPhone())
                                       .position(orderLocation);

                    mMap.addMarker(mark);

                }catch (JSONException e)
                {
                    Toast.makeText(TrackingOrder.this, "ERROR :"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPFATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                          .addConnectionCallbacks(this)
                          .addOnConnectionFailedListener(this)
                          .addApi(LocationServices.API)
                          .build();

        mGoogleApiClient.connect();

    }

    private boolean checkPlayService() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTIONS_REQUEST).show();
            } else {
                Toast.makeText(this, "This device is not support", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }
    private void requestRunTimePermission() {

        ActivityCompat.requestPermissions(this ,new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        },LOCATION_PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST:

                if (checkPlayService())
                {
                    buildGoogleApiClient();
                    createLocationRequest();

                    displayLocation();
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null)
            mGoogleApiClient.connect();
    }
}
