package com.example.map.GoogleMapWork;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.map.Constants.PreferenceClass;
import com.example.map.GoogleMapWork.DirectionApi.Direction_A;
import com.example.map.GoogleMapWork.PolyLine_Map.PolyLineMapsActivity;
import com.example.map.R;
import com.example.map.Utils.GpsUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    public static boolean SAVE_LOCATION, SAVE_LOCATION_ADDRESS;

    private static final String TAG = "Current Location";

    GoogleMap mGoogleMap;

    ArrayList<PlaceModel> proPlaceListRoute = new ArrayList<>();
    ArrayList<PlaceModel> proPlaceListRoute_copy = new ArrayList<>();

    private LatLng mDefaultLocation;
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSION_DATA_ACCESS_CODE = 2;
    private boolean mLocationPermissionGranted;
    String lat, long_;


    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.


    private Location mLastKnownLocation;
    private SupportMapFragment mapFragment;


    private SharedPreferences sPredMap;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;


    ImageView close_country;
    TextView current_text_tv;
    RelativeLayout current_address_div, save_loc_div;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enable_permission();
        sPredMap = getSharedPreferences(PreferenceClass.user, MODE_PRIVATE);
        lat = sPredMap.getString(PreferenceClass.LATITUDE, "");
        long_ = sPredMap.getString(PreferenceClass.LONGITUDE, "");
        if (lat.isEmpty() && long_.isEmpty()) {
            lat = "" + Double.parseDouble("20.5937");
            long_ = "" + Double.parseDouble("78.9629");
        }


        mDefaultLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(long_));
        setContentView(R.layout.activity_main_maps);
        current_text_tv = findViewById(R.id.current_text_tv);
        close_country = findViewById(R.id.close_country);
        current_address_div = findViewById(R.id.current_address_div);
        save_loc_div = findViewById(R.id.save_loc_div);

        save_loc_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SAVE_LOCATION = true;
                SAVE_LOCATION_ADDRESS = true;

                SharedPreferences.Editor editor = sPredMap.edit();
                editor.putString(PreferenceClass.LATITUDE, lat);
                editor.putString(PreferenceClass.LONGITUDE, long_)
                        .apply();

                //Do the things here on Click.....
                Intent data = new Intent();
                data.putExtra("lat", String.valueOf(lat));
                data.putExtra("lng", String.valueOf(long_));
                setResult(RESULT_OK, data);
                finish();


            }
        });
        current_address_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MapsActivity.this, SearchPlaces.class);
                startActivityForResult(i, PERMISSION_DATA_ACCESS_CODE);
                //  startActivity(new Intent(MapsActivity.this,SearchPlaces.class));

            }
        });
        close_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        findViewById(R.id.polylineMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, PolyLineMapsActivity.class));
            }
        });  findViewById(R.id.direction_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, Direction_A.class));
            }
        });

        setupMapIfNeeded();

        configureCameraIdle();

        mapFragment.setRetainInstance(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        // put your code here...
        setupMapIfNeeded();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
      /*  if (mGoogleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mGoogleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);

        }*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        updateLocationUI();
        getDeviceLocation();

        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mGoogleMap.moveCamera(update);

            mGoogleMap.setMapType(mgr.getSavedMapType());
        }

        if (mGoogleMap != null) {
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
            if (ActivityCompat.checkSelfPermission(getApplicationContext()
                    , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext()
                    , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

        }
        mGoogleMap.setOnCameraIdleListener(onCameraIdleListener);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        //Getprofiledata();

    }


    private void updateLocationUI() {
        if (mGoogleMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mGoogleMap.setMyLocationEnabled(false);
                //  mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(Double.parseDouble(lat), Double.parseDouble(long_)), DEFAULT_ZOOM));
                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

                            SharedPreferences.Editor editor = sPredMap.edit();
                            editor.putString(PreferenceClass.LATITUDE, String.valueOf(lat));
                            editor.putString(PreferenceClass.LONGITUDE, String.valueOf(long_));
                            editor.apply();

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                            // mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void configureCameraIdle() {
        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                LatLng latLng = mGoogleMap.getCameraPosition().target;
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        String locality = addressList.get(0).getAddressLine(0);
                        String country = addressList.get(0).getCountryName();

                        String local = "";
                        if (locality != null && !locality.equals("null"))
                            local = "" + locality;

                        else if (country != null && !country.equals("null"))
                            local = "" + country;

                        lat = "" + latLng.latitude;
                        long_ = "" + latLng.longitude;
                        current_text_tv.setText(local);

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {

                }


            }
        };
    }


    private void setupMapIfNeeded() {
        // Build the map.
        if (mGoogleMap == null) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        MapStateManager mgr = new MapStateManager(this);
        mgr.saveMapState(mGoogleMap);
       /* //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }*/
    }


    @SuppressWarnings("deprecation")
    private void showDialogGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setCancelable(false);
        builder.setTitle("Enable GPS");
        builder.setMessage("Please enable GPS");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            try {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } catch (Exception e) {
                e.getMessage();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_DATA_ACCESS_CODE) {
            if (resultCode == RESULT_OK) {
                String latSearch = data.getStringExtra("lat");
                String longSearch = data.getStringExtra("lng");
                lat = latSearch;
                long_ = longSearch;
                mDefaultLocation = new LatLng(Double.parseDouble(latSearch), Double.parseDouble(longSearch));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    updateLocationUI();
                }
            }
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    private void enable_permission() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!GpsStatus) {

            new GpsUtils(this).turnGPSOn(isGPSEnable -> {

                enable_permission();

            });
        } else if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateLocationUI();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                        },
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }


    }

}
