package com.example.map.Utils;

import android.app.ProgressDialog;
import android.content.Context;

public class CustPrograssbar {

    ProgressDialog progressDialog;

    public void prograsscreate(Context context) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                return;
            } else {
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Progress...");
                progressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeprograssbar() {
        if (progressDialog != null) {
            try {
                progressDialog.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
