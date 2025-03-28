package com.example.pdrdemo;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtil {
    static Toast toast = null;
    public static void show(Context context, String text) {
        try {
            if(toast!=null){
                toast.setText(text);
            }else{
                toast= Toast.makeText(context, text, Toast.LENGTH_SHORT);
            }
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
