package com.blackfalcon.periodtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Runs after boot / app update / time change / daily alarm: re-posts the status-bar notification. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Notif.refresh(context);
    }
}
