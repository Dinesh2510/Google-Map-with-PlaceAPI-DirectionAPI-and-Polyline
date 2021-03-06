package com.example.map.Activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.map.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class Userlikes_F extends BottomSheetDialogFragment {

    public static Userlikes_F newInstance() {
        Userlikes_F fragment = new Userlikes_F();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.fragment_bottomsheet, null);
        dialog.setContentView(contentView);
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

}
