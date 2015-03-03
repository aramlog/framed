package com.frames.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AndroidUtils {

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(Double dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static int getScreenWidth(Activity screen) {
        Display display = screen.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static int getScreenHeight(Activity screen) {
        Display display = screen.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static void setFont(TextView textView) {
        Typeface tf = Typeface.createFromAsset(textView.getContext().getAssets(), "font/PapaBear.ttf");
        textView.setTypeface(tf);
    }

    public static void overrideFont(Context context, String defaultFontNameToOverride, String customFontFileNameInAssets) {
        try {
            final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), customFontFileNameInAssets);
            final Field defaultFontTypefaceField = Typeface.class.getDeclaredField(defaultFontNameToOverride);
            defaultFontTypefaceField.setAccessible(true);
            defaultFontTypefaceField.set(null, customFontTypeface);
        } catch (Exception e) {
        }
    }

    public static void changeChildrenFont(Context context, ViewGroup v){
        Typeface font = Typeface.createFromAsset(context.getAssets(), "font/PapaBear.ttf");
        for(int i = 0; i < v.getChildCount(); i++){
            if(v.getChildAt(i) instanceof ViewGroup){
                changeChildrenFont(context,(ViewGroup) v.getChildAt(i));
            }
            else{
                try {
                    Object[] nullArgs = null;
                    Method methodTypeFace = v.getChildAt(i).getClass().getMethod("setTypeface", new Class[] {Typeface.class, Integer.TYPE});
                    Method methodGetTypeFace = v.getChildAt(i).getClass().getMethod("getTypeface", new Class[] {});
                    Typeface typeFace = ((Typeface)methodGetTypeFace.invoke(v.getChildAt(i), nullArgs));
                    methodTypeFace.invoke(v.getChildAt(i), new Object[] {font, typeFace == null ? 0 : typeFace.getStyle()});
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
