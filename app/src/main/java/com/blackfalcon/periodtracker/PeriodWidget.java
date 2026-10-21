package com.blackfalcon.periodtracker;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

public class PeriodWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] ids) {
        Notif.refresh(context);
    }

    @Override
    public void onEnabled(Context context) {
        Notif.refresh(context);
    }
}
