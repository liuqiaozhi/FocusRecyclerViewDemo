package com.example.focusrecyclerviewdemo;

import android.content.Context;

/**
 * @author liuqz
 */
public class Utils {
    public static int getDimension(Context context, int resId){
        return (int)context.getResources().getDimension(resId);
    }

}
