package edu.ptit.vn.appda2020.util;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import edu.ptit.vn.appda2020.model.dto.GeoPoint;

public class CommonUtils {
    public static double haversineFomular(GeoPoint from, GeoPoint to) {
        double R = 6372.8;

        double dLat = Math.toRadians(to.getLat() - from.getLat());
        double dLon = Math.toRadians(to.getLng() - from.getLng());
        double lat1 = Math.toRadians(from.getLat());
        double lat2 = Math.toRadians(to.getLat());

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void setTranslucentStatus(Activity context, boolean flag) {
        //Determine whether the current device version number is more than 4.4. If so, call setTranslucentStatus to make the status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = context.getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (flag) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
    }

    /**
     * Need MIUIV6 or above
     *
     * @param dark Set the status bar text and icon color to dark
     * @return boolean Successful execution returns true
     */
    public static boolean MIUISetStatusBarLightMode(Activity activity, boolean dark) {
        boolean result = false;
        Window window = activity.getWindow();
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//Status bar transparent and black font
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//Clear black font
                }
                result = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //Development version 7.7.13 and later uses system API, the old method is invalid but will not report errors, so both methods should be added
                    if (dark) {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    } else {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean isStraight(GeoPoint A, GeoPoint B, GeoPoint C) {
        return Math.abs(haversineFomular(A, B) + haversineFomular(B, C) - haversineFomular(A, C)) < 0.01;
    }
}
