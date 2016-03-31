package com.example.catalyst.androidtodo.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by dsloane on 3/31/2016.
 */
public class BootReceiver extends BroadcastReceiver {
    NotificationReceiver alarm = new NotificationReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.turnOnNotifications(context);
        }
    }
}