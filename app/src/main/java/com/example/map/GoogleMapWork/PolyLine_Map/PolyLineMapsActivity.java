package com.example.map.GoogleMapWork.PolyLine_Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.map.Constants.PreferenceClass;
import com.example.map.GoogleMapWork.MapStateManager;
import com.example.map.GoogleMapWork.PlaceModel;
import com.example.map.GoogleMapWork.SearchPlaces;
import com.example.map.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class PolyLineMapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    public static boolean SAVE_LOCATION, SAVE_LOCATION_ADDRESS;

    private static final String TAG = "Current Location";

    GoogleMap mGoogleMap;

    ArrayList<PlaceModel> placeModelArrayList = new ArrayList<>();
    ArrayList<PlaceModel> placeModelArrayListCopy = new ArrayList<>();

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

        sPredMap = getSharedPreferences(PreferenceClass.user, MODE_PRIVATE);
        lat = sPredMap.getString(PreferenceClass.LATITUDE, "");
        long_ = sPredMap.getString(PreferenceClass.LONGITUDE, "");
        if (lat.isEmpty() && long_.isEmpty()) {
            lat = "" + Double.parseDouble("31.4904023");
            long_ = "" + Double.parseDouble("74.2906989");
        }


        mDefaultLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(long_));
        setContentView(R.layout.activity_poly_line_maps);
        current_text_tv = findViewById(R.id.current_text_tv);
        close_country = findViewById(R.id.close_country);
        current_address_div = findViewById(R.id.current_address_div);
        save_loc_div = findViewById(R.id.save_loc_div);
        handleSSLHandshake();
        save_loc_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SAVE_LOCATION = true;
                SAVE_LOCATION_ADDRESS = true;

                SharedPreferences.Editor editor = sPredMap.edit();
                editor.putString(PreferenceClass.LATITUDE, lat);
                editor.putString(PreferenceClass.LONGITUDE, long_)
                        .commit();

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
                Intent i = new Intent(PolyLineMapsActivity.this, SearchPlaces.class);
                startActivityForResult(i, PERMISSION_DATA_ACCESS_CODE);
                //  startActivity(new Intent(PolyLineMapsActivity.this,SearchPlaces.class));

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


        setupMapIfNeeded();

        configureCameraIdle();

        mapFragment.setRetainInstance(true);

    }

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // put your code here...
        GetDataForMap(); //Load from server
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
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showDialogProject(marker.getSnippet(), marker.getTitle());
                return false;
            }
        });
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(getApplicationContext(), "onInfoWindowClick:", Toast.LENGTH_SHORT).show();

            }
        });
        On_Data_Map();
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
                Geocoder geocoder = new Geocoder(PolyLineMapsActivity.this, Locale.getDefault());

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
        AlertDialog.Builder builder = new AlertDialog.Builder(PolyLineMapsActivity.this);
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
                ActivityCompat.requestPermissions(PolyLineMapsActivity.this,
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


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("polyline_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void GetDataForMap() {
        final ProgressDialog progressDialog = new ProgressDialog(PolyLineMapsActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading..");
        progressDialog.show();
        String FatchPlaceByRoute = "https://pixeldev.in/webservices/json_file/polyline_data.json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, FatchPlaceByRoute, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("StringRequestList", "onResponse: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message = jsonObject.getString("message");
                    String code = jsonObject.getString("code");
                    if (code.equals("200")) {
                        progressDialog.dismiss();

                        JSONArray jsonArrayvideo = jsonObject.optJSONArray("data");

                        if (jsonArrayvideo != null && jsonArrayvideo.length() > 0) {
                            placeModelArrayList = new ArrayList<>();
                            placeModelArrayListCopy = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                PlaceModel placeModel = new PlaceModel();
                                JSONObject jsonArrayJSONObject = jsonArray.getJSONObject(i);

                                placeModel.id = jsonArrayJSONObject.getString("id");
                                placeModel.name = jsonArrayJSONObject.getString("name");
                                placeModel.latitude = jsonArrayJSONObject.getString("latitude");
                                placeModel.longitude = jsonArrayJSONObject.getString("longitude");
                                placeModel.city = jsonArrayJSONObject.getString("city");
                                placeModelArrayList.add(placeModel);
                                placeModelArrayListCopy.add(placeModel);
                            }
                            On_Data_Map();
                        }

                    } else {

                        progressDialog.dismiss();
                    }
                } catch (JSONException e) {
                   ;
                    Log.d("JSONException", "onResponse: " +e.toString());
                    progressDialog.dismiss();
                }

                progressDialog.dismiss();

                if (placeModelArrayListCopy.size() > 0) {
                    placeModelArrayListCopy.add(placeModelArrayListCopy.size(), placeModelArrayListCopy.get(0));
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onErrorResponse", "onResponse: " +error.toString());

                error.printStackTrace();
                progressDialog.dismiss();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        RetryPolicy retryPolicy = new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(stringRequest);

    }

    private void On_Data_Map() {
        if (mGoogleMap != null) {

            if (ActivityCompat.checkSelfPermission(PolyLineMapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PolyLineMapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
//            mGoogleMap.setTrafficEnabled(true);
//            mGoogleMap.setIndoorEnabled(true);

            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);//setMyLocationButtonEnabled
            mGoogleMap.getUiSettings().setCompassEnabled(true);
            mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);
            mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);
            mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
            /*Start =>this is for Complete Polyline circle */
            if (placeModelArrayListCopy.size() > 0) {
                placeModelArrayListCopy.add(placeModelArrayListCopy.size(), placeModelArrayListCopy.get(0));
            }
            /* End =>this is for Complete Polyline circle */

            if (placeModelArrayListCopy != null && placeModelArrayListCopy.size() > 0) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.width(10);
                List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dot(), new Gap(5f));
                polylineOptions.pattern(pattern);

                polylineOptions.color(getResources().getColor(R.color.colorRed));
                polylineOptions.geodesic(true);

                LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
                for (int i = 0; i < placeModelArrayListCopy.size(); i++) {
                    double placeLat = Double.parseDouble(placeModelArrayListCopy.get(i).latitude);
                    double placeLng = Double.parseDouble(placeModelArrayListCopy.get(i).longitude);
                    Log.d(TAG, "onMapReady: " + placeLat);
                    Log.d(TAG, "onMapReady: " + placeLng);

                    LatLng latLng = new LatLng(placeLat, placeLng);
                    MarkerOptions dmarkerOptions = new MarkerOptions();
                    dmarkerOptions.position(latLng);
                    dmarkerOptions.title(placeModelArrayListCopy.get(i).city);
                    dmarkerOptions.snippet(placeModelArrayListCopy.get(i).name);
                    dmarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker_location, placeModelArrayListCopy.get(i).city)));

                    mGoogleMap.addMarker(dmarkerOptions);
                    latLngBounds.include(latLng);
                    polylineOptions.add(latLng);
                    if (i == 0) {
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9));
                    }
                }

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 9));
                mGoogleMap.addPolyline(polylineOptions);
            }

        }

    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId, String tittle) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_marker, null);
        ImageView markerImageView = customMarkerView.findViewById(R.id.profile_imageaa);
        TextView txtTittle = customMarkerView.findViewById(R.id.txtTittle);
        txtTittle.setText(tittle);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    Dialog dialog_details;

    private void showDialogProject(String someid, String subtitle) {
        dialog_details = new Dialog(this);
        dialog_details.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog_details.setContentView(R.layout.dialog_info);
        dialog_details.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog_details.setCancelable(true);
        ((TextView) dialog_details.findViewById(R.id.name)).setText(someid);
        ((TextView) dialog_details.findViewById(R.id.address)).setText(subtitle);

        (dialog_details.findViewById(R.id.fab)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_details.dismiss();
            }
        });


        dialog_details.show();
    }
}
