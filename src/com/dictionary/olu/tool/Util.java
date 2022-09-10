package com.dictionary.olu.tool;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Util {

	public static void translucentStatusbar(Activity activity) {
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			activity.getWindow().requestFeature(
					Window.FEATURE_NO_TITLE);
			if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
				Window window = activity.getWindow();
				window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				window.getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						// | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
				window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				 window.setStatusBarColor(Color.TRANSPARENT);
			} else {
				activity.getWindow().addFlags(
						WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			}

		}

	}
	
	public static void setDarkStatusIcon(Activity activity, boolean bDark) {
	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
	        View decorView = activity.getWindow().getDecorView();
	        if(decorView != null){
	            int vis = decorView.getSystemUiVisibility();
	            if(bDark){
	                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
	            } else{
	                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
	            }
	            decorView.setSystemUiVisibility(vis);
	        }
	    }
	}
	
}
