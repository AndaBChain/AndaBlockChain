package com.onets.wallet.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

/**
 * 在线更新，未完成
 */
public class UpdateAppUtils {
    private static String TAG = "UpdateAppUtils";

    /**
     * 读取AndroidManifest.xml中的versionCode
     * @param context
     * @return
     */
    public static int getVerCode(Context context){
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    0
            ).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getVerCode: " + e.getMessage());
        }
        return verCode;
    }

    /**
     * 读取AndroidManifest.xml中的versionName
     * @param context
     * @return
     */
    public static String getVerName(Context context){
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    0
            ).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getVerName: " + e.getMessage());
        }
        return verName;
    }

    /**
     * 检查是否存在SDCard
     * @return
     */
    public static boolean hasSdcard(){
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 对话框字体主题色
     */
    public static int RED_THEME = Color.parseColor("#eb2127");
}
