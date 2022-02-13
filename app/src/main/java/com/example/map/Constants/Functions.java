package com.example.map.Constants;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.map.R;
import com.example.map.Services.UpdateLocation;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.core.app.ActivityCompat;


public class Functions {
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    static BroadcastReceiver broadcastReceiver;
    static IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    public static void unRegisterConnectivity(Context mContext) {
        try {
            mContext.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void registerConnectivity(Context context, final Callback callback) {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isConnectedToInternet(context)) {
                    callback.Responce("connected");
                } else {
                    callback.Responce("disconnected");
                }
            }
        };

        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public static Boolean isConnectedToInternet(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            } else {
                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }

        Log.i("update_statut","Network is available : FALSE ");
        return false;

    }
    public static final String LOCATION_SERVICE = "com.example.map.Services.UpdateLocation";

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String[] LOCATION_PERMISSIONS;

    public static boolean check_location_permissions(Activity context) {

        LOCATION_PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        };

        if (!hasPermissions(context, LOCATION_PERMISSIONS)) {
            if (SDK_INT >= Build.VERSION_CODES.M) {
//                context.requestPermissions(PERMISSIONS, 123);
            }
        } else {

            return true;
        }

        return false;
    }

    public static boolean isServiceRunning(Context context, String service_name) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service_name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean Bind_Location_service(Activity context) {

        if (!isServiceRunning(context, Functions.LOCATION_SERVICE)) {
            Intent mServiceIntent = new Intent(context, UpdateLocation.class);
            context.startService(mServiceIntent);
            return false;
        } else {
            return true;
        }

    }

    public static void Hide_keyboard(Activity activity){

        InputMethodManager imm = (InputMethodManager)activity.getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static int dpToPx(final Activity context, final float dp) {
        return (int)(dp * context.getResources().getDisplayMetrics().density);
    }


    public static Dialog dialog;
    public static void Show_loader(Context context, boolean outside_touch, boolean cancleable) {

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_progress_dialog_layout);


        if(!outside_touch)
            dialog.setCanceledOnTouchOutside(false);

        if(!cancleable)
            dialog.setCancelable(false);

        dialog.show();

    }


    public static void cancel_loader(){
        if(dialog!=null){
            dialog.cancel();
        }
    }


    // this is the delete message diloge which will show after long press in chat message
    public static void Show_Options(Context context,CharSequence[] options,final  Callback callback) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context,R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                callback.Responce(""+options[item]);

            }

        });

        builder.show();

    }


    public static boolean showToast(Context context, String msg) {
        if (true) {
            Toast.makeText(context, "" + msg, Toast.LENGTH_LONG).show();
        }

        return false;
    }
}
