package com.supermax.base.common.permission.support;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.supermax.base.common.permission.inter.ISetting;
/**
 * @Author yinzh
 * @Date   2018/5/25 15:04
 * @Description
 */

public class ViVo implements ISetting {

    private Context context;

    public ViVo(Context context) {
        this.context = context;
    }

    @Override
    public Intent getSetting() {
        Intent appIntent = context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
        if (appIntent != null && Build.VERSION.SDK_INT < 23) {
            context.startActivity(appIntent);
            return null;
        }
        Intent vIntent = new Intent();
        vIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        vIntent.setAction(Settings.ACTION_SETTINGS);
        return vIntent;
    }
}
