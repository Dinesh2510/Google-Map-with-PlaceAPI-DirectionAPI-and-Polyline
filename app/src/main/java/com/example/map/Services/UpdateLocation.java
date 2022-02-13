package com.example.map.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.map.Constants.AllConstants;
import com.example.map.Constants.Config;
import com.example.map.Constants.PreferenceClass;
import com.example.map.Models.TrackingModelClass;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class UpdateLocation extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {



    Intent intent;
    private static final String TAG = "Resp";

    private static Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 3000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 0;


    DatabaseReference mDatabase;
    FirebaseDatabase firebaseDatabase;
    SharedPreferences pending_job_pref;
    String user_id;
    private static Double previousLat = 0.0;
    private static Double previousLong = 0.0;

    final class Mythreadclass implements Runnable {


        @Override
        public void run() {

            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        buildGoogleApiClient();
        createLocationRequest();
        mGoogleApiClient.connect();
        intent = new Intent();

        pending_job_pref = getSharedPreferences(PreferenceClass.user, Context.MODE_PRIVATE);
        user_id = pending_job_pref.getString(PreferenceClass.pre_user_id, "");

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference().child(AllConstants.TRACKING).child(user_id);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        Thread thread = new Thread(new Mythreadclass());
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }



    private void displayLocation() {

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            mDatabase.keepSynced(true);
            mDatabase.setValue(new TrackingModelClass(""+latitude,""+longitude,""+previousLat,""+previousLong));

            previousLat = latitude;
            previousLong = longitude;

            Log.d("resp",previousLat+"---"+previousLong);

        } else {

            Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_SHORT).show();
        }
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }



    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private FusedLocationProviderClient mFusedLocationClient;
    LocationCallback locationCallback;
    protected void startLocationUpdates() {
        mGoogleApiClient.connect();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback= new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mLastLocation = location;
                        displayLocation();
                        saveRiderLocation(location);

                    }
                }
            }
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,locationCallback
                , Looper.myLooper());

    }


    protected void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }


    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
      startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    public void saveRiderLocation(Location location){

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("user_id",user_id);
            jsonObject.put("lat",location.getLatitude());
            jsonObject.put("long",location.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(AllConstants.tag,jsonObject.toString());
        Log.d(AllConstants.tag, Config.ADD_LOCATIONS);

        JsonObjectRequest updateLocationRequest = new JsonObjectRequest(Request.Method.POST, Config.ADD_LOCATIONS, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d(AllConstants.tag,response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("api-key", "2a5588cf-4cf3-4f1c-9548-cc1db4b54ae3");
                return headers;
            }
        };

        queue.add(updateLocationRequest);

    }




}
