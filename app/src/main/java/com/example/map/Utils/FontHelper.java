package com.example.map.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Helper class to apply custom font from assets to all text views in the specified root
 * view.
 * 
 * @author Alexander Naberezhnov
 */
public class FontHelper {
	private static final String TAG = FontHelper.class.getSimpleName();


	public static void applyFont(final Context context, final View root, final String fontPath) {
		try {
			if (root instanceof ViewGroup) {
				ViewGroup viewGroup = (ViewGroup) root;
				int childCount = viewGroup.getChildCount();
				for (int i = 0; i < childCount; i++)
					applyFont(context, viewGroup.getChildAt(i), fontPath);
			} else if (root instanceof TextView)
				((TextView) root).setTypeface(Typeface.createFromAsset(context.getAssets(), fontPath));
		} catch (Exception e) {
			Log.e(TAG, String.format("Error occured when trying to apply %s font for %s view", fontPath, root));
			e.printStackTrace();
		}
	}
}
