package com.example.map.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.map.Constants.Functions;
import com.example.map.R;

import android.content.Intent;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.View;


import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

public class MainMenuActivity extends AppCompatActivity {

    private MainMenuFragment mainMenuFragment;
    long mBackPressed;

    public static String actionType ="none";

    DatabaseReference rootref;


    public static MainMenuActivity mainMenuActivity;

    Snackbar snackbar;


    Integer todayDay = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mainMenuActivity = this;


        try {
            if (savedInstanceState == null) {
                initScreen();
            } else {
                mainMenuFragment = (MainMenuFragment) getSupportFragmentManager().getFragments().get(0);
            }
        } catch (Exception e) {
        }
        Functions.registerConnectivity(this, response -> {
            if (response.equalsIgnoreCase("disconnected")) {
                snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.no_internet, Snackbar.LENGTH_INDEFINITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.colorRed));
                snackbar.show();
            } else {
                if (snackbar != null)
                    snackbar.dismiss();
            }
        });


    }


    private void initScreen() {
        mainMenuFragment = new MainMenuFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, mainMenuFragment).commit();

        findViewById(R.id.container).setOnClickListener(v ->
                Functions.hideSoftKeyboard(MainMenuActivity.this)
        );
    }


    // on start we will save the latest token into the firebase
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        }
        //checkVersion();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (!mainMenuFragment.onBackPressed()) {
            int count = this.getSupportFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                if (mBackPressed + 2000 > System.currentTimeMillis()) {
                    super.onBackPressed();
                    return;
                } else {
                    Functions.showToast(getApplicationContext(), "Tap Again To Exit");

                    mBackPressed = System.currentTimeMillis();
                }
            } else {
                super.onBackPressed();
            }
        }
    }


    @Override
    protected void onDestroy() {
        Functions.unRegisterConnectivity(this);
        super.onDestroy();
    }


}