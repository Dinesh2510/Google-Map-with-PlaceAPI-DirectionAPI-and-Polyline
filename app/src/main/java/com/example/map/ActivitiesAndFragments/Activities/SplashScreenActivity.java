package com.example.map.ActivitiesAndFragments.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.map.BuildConfig;
import com.example.map.Constants.Functions;
import com.example.map.Constants.PreferenceClass;
import com.example.map.GoogleMapWork.MapsActivity;
import com.example.map.R;
import com.example.map.Utils.ContextWrapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;



public class SplashScreenActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {

    private Location mLastLocation;


    public static String VERSION_CODE;

    private static int SPLASH_TIME_OUT = 3000;

    TextView welcome_location_txt;
    private RelativeLayout main_welcome_screen_layout, main_splash_layout, welcome_search_div;

    String getCurrentLocationAddress;
    private GoogleApiClient mGoogleApiClient;
    SharedPreferences sharedPreferences;
    double latitude, longitude;

    private Button welcome_show_restaurants_btn;
    AlertDialog.Builder builder;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSION_DATA_ACCESS_CODE = 2;
    private boolean mLocationPermissionGranted;


    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    @Override
    protected void attachBaseContext(Context newBase) {

        String[] language_array = newBase.getResources().getStringArray(R.array.language_code);
        List<String> language_code = Arrays.asList(language_array);
        sharedPreferences = newBase.getSharedPreferences(PreferenceClass.user, MODE_PRIVATE);
        String language = sharedPreferences.getString(PreferenceClass.selected_language, "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && language_code.contains(language)) {
            Locale newLocale = new Locale(language);
            Context context = ContextWrapper.wrap(newBase, newLocale);
            super.attachBaseContext(context);
        }
        else {
            super.attachBaseContext(newBase);
        }

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            set_language_local();
        }

        setContentView(R.layout.splash);
        sharedPreferences = getSharedPreferences(PreferenceClass.user, MODE_PRIVATE);
        getCurrentLocationAddress = sharedPreferences.getString(PreferenceClass.CURRENT_LOCATION_ADDRESS, "");

        VERSION_CODE = BuildConfig.VERSION_NAME;


        buildGoogleApiClient();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);


        welcome_location_txt = findViewById(R.id.welcome_location_txt);
        main_welcome_screen_layout = findViewById(R.id.main_welcome_screen_layout);
        main_splash_layout = findViewById(R.id.main_splash_layout);
        welcome_search_div = findViewById(R.id.welcome_search_div);
        welcome_show_restaurants_btn = findViewById(R.id.welcome_show_restaurants_btn);
        welcome_show_restaurants_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Move_next();

            }
        });



        welcome_search_div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(SplashScreenActivity.this, MapsActivity.class);
                startActivityForResult(i, PERMISSION_DATA_ACCESS_CODE);

            }
        });



        if (!getCurrentLocationAddress.isEmpty()) {

            main_welcome_screen_layout.setVisibility(View.GONE);
            main_splash_layout.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    Move_next();

                }
            }, SPLASH_TIME_OUT);

        } else {
            displayLocation();
        }


        printKeyHash();


        set_language_local();

    }


    public void set_language_local(){
        String [] language_array=getResources().getStringArray(R.array.language_code);
        List <String> language_code= Arrays.asList(language_array);

        String language=sharedPreferences.getString(PreferenceClass.selected_language,"");


        if(language_code.contains(language)) {
            Locale myLocale = new Locale(language);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = new Configuration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            onConfigurationChanged(conf);

        }



    }




    public void Move_next(){

        final String android_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        SharedPreferences.Editor editor2 = sharedPreferences.edit();
        editor2.putString(PreferenceClass.UDID, android_id).commit();
        Intent i = new Intent(SplashScreenActivity.this, MapsActivity.class);//MainMenuActivity
        startActivity(i);
        finish();

    }


    private void displayLocation() {
        getLocationPermission();
        Log.e("", "displayLocation: ");
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

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            Address locationAddress;

            locationAddress=getAddress(latitude,longitude);
            if(locationAddress!=null)
            {

                String city = locationAddress.getLocality();

                String country = locationAddress.getCountryName();

                if(!getCurrentLocationAddress.isEmpty()){
                    welcome_location_txt.setText(getCurrentLocationAddress);
                }
                else {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PreferenceClass.CURRENT_LOCATION_LAT_LONG, latitude + "," + longitude);
                    editor.putString(PreferenceClass.CURRENT_LOCATION_ADDRESS, city + " " + country);
                    editor.putString(PreferenceClass.LATITUDE, String.valueOf(latitude));
                    editor.putString(PreferenceClass.LONGITUDE, String.valueOf(longitude));
                    editor.commit();

                    welcome_location_txt.setText(getCurrentLocationAddress);
                    welcome_location_txt.setText(city + " " + country);
                }

            }

        } else {

            welcome_location_txt
                    .setText("Tata Group, Mumbai");
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_DATA_ACCESS_CODE) {
            if(resultCode == RESULT_OK) {
                latitude = Double.parseDouble(data.getStringExtra("lat"));
                longitude = Double.parseDouble(data.getStringExtra("lng"));


                //  Geocoder gcd = new Geocoder(this, Locale.getDefault());
                Address locationAddress;

                locationAddress=getAddress(latitude,longitude);
                if(locationAddress!=null)
                {

                    String city = locationAddress.getLocality();

                    String country = locationAddress.getCountryName();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PreferenceClass.CURRENT_LOCATION_LAT_LONG,latitude+","+longitude);
                    editor.putString(PreferenceClass.CURRENT_LOCATION_ADDRESS,city+" " +country);
                    editor.putString(PreferenceClass.LATITUDE,String.valueOf(latitude));
                    editor.putString(PreferenceClass.LONGITUDE,String.valueOf(longitude));
                    editor.commit();

                    welcome_location_txt.setText(getCurrentLocationAddress);
                    welcome_location_txt.setText(city+" " +country);

                }


            }


        }


    }

    public Address getAddress(double latitude,double longitude)
    {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude,longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    private void buildAlertMessageNoGps() {
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        //pd.dismiss();
                    }
                });
        final AlertDialog alert = builder.create();
        if(!getCurrentLocationAddress.isEmpty()){
            alert.cancel();
        }
        else {
            alert.show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
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
            Log.e("", "getLocationPermission: mLocationPermissionGranted");
            Functions.Bind_Location_service(SplashScreenActivity.this);

        } else {
            try {
                ActivityCompat.requestPermissions(SplashScreenActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
            catch (Exception e){
                e.getMessage();
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
                    displayLocation();
                }
            }
        }

    }



    public void printKeyHash()  {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName() , PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("keyhash" , Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


}
