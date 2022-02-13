package com.example.map.ActivitiesAndFragments.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.map.R;
import com.example.map.Utils.RelateToFragment_OnBack.RootFragment;

public class Profile_F extends RootFragment {
    public Profile_F() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users_, container, false);
    }

}
