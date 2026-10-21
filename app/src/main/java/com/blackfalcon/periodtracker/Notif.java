package com.blackfalcon.periodtracker;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import java.util.Calendar;

public class Notif {
    static final String CH = "cycle";
    static final int ID = 1;

    /** {title, subtitle} in the user's language. */
    public static String[] texts(Data d) {
        int today = D.today();
        Cycle c = d.cycleInfo(today);
        if (c.lastStart < 0) {
            return new String[] {
                d.tr("پیریڈ ٹریکر", "Period Tracker", "متتبع الدورة"),
                d.tr("پہلا پیریڈ درج کریں", "Add your first period", "أضف أول دورة")
            };
        }
        int cd = today - c.lastStart + 1;
        int pd = c.periodDayNo(today);
        String title = pd > 0
            ? d.tr("پیریڈ کا دن ", "Period day ", "يوم الحيض ") + pd
            : d.tr("سائیکل کا دن ", "Cycle day ", "يوم الدورة ") + cd;
        int diff = c.nextStart() - today;
        String sub;
        if (diff > 0) {
            sub = d.tr("اگلا پیریڈ " + diff + " دن بعد", "Next period in " + diff + " days",
                    "الدورة القادمة بعد " + diff + " يومًا");
        } else if (diff == 0) {
            sub = d.tr("آج پیریڈ متوقع ہے", "Period expected today", "الدورة متوقعة اليوم");
        } else {
            sub = d.tr("پیریڈ " + (-diff) + " دن لیٹ", "Period " + (-diff) + " days late",
                    "متأخرة " + (-diff) + " يومًا");
        }
        if (c.isOvu(today)) {
            sub += " \u2022 " + d.tr("بیضہ ریزی (اندازہ)", "Ovulation (est.)", "الإباضة (تقديري)");
        } else if (c.fertile(today)) {
            sub += " \u2022 " + d.tr("زرخیزی کی مدت (اندازہ)", "Fertile window (est.)",
                    "فترة الخصوبة (تقديري)");
        }
        return new String[] {title, sub};
    }

    public static void refresh(Context c) {
        Data d = new Data(c);
        updateWidgets(c, d);
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            if (d.notifOn()) post(c, d, nm);
            else nm.cancel(ID);
        }
        schedule(c);
    }

    static PendingIntent openApp(Context c) {
        Intent i = new Intent(c, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(c, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void post(Context c, Data d, NotificationManager nm) {
        ensureChannel(c, nm);
        String[] t = texts(d);
        Notification.Builder b = builder(c);
        b.setSmallIcon(R.drawable.ic_notif);
        b.setContentTitle(t[0]);
        b.setContentText(t[1]);
        b.setOngoing(true);
        b.setOnlyAlertOnce(true);
        b.setShowWhen(false);
        b.setContentIntent(openApp(c));
        if (d.hide()) {
            // lock screen shows only the app name, not health details
            Notification.Builder pb = builder(c);
            pb.setSmallIcon(R.drawable.ic_notif);
            pb.setContentTitle(c.getString(R.string.app_name));
            b.setVisibility(Notification.VISIBILITY_PRIVATE);
            b.setPublicVersion(pb.build());
        } else {
            b.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        nm.notify(ID, b.build());
    }

    static void ensureChannel(Context c, NotificationManager nm) {
        if (Build.VERSION.SDK_INT >= 26) channelO(c, nm);
    }

    @TargetApi(26)
    static void channelO(Context c, NotificationManager nm) {
        NotificationChannel ch = new NotificationChannel(CH, c.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW);
        ch.setShowBadge(false);
        nm.createNotificationChannel(ch);
    }

    @TargetApi(26)
    static Notification.Builder builderO(Context c) {
        return new Notification.Builder(c, CH);
    }

    @SuppressWarnings("deprecation")
    static Notification.Builder builderLegacy(Context c) {
        return new Notification.Builder(c).setPriority(Notification.PRIORITY_LOW);
    }

    static Notification.Builder builder(Context c) {
        if (Build.VERSION.SDK_INT >= 26) return builderO(c);
        return builderLegacy(c);
    }

    static void updateWidgets(Context c, Data d) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, PeriodWidget.class));
        if (ids == null || ids.length == 0) return;
        String[] t = texts(d);
        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget);
        rv.setTextViewText(R.id.w_title, t[0]);
        rv.setTextViewText(R.id.w_sub, t[1]);
        rv.setOnClickPendingIntent(R.id.w_root, openApp(c));
        m.updateAppWidget(ids, rv);
    }

    /** Daily refresh at 00:05 so the day counter stays right. */
    static void schedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Calendar t = Calendar.getInstance();
        t.add(Calendar.DAY_OF_YEAR, 1);
        t.set(Calendar.HOUR_OF_DAY, 0);
        t.set(Calendar.MINUTE, 5);
        t.set(Calendar.SECOND, 0);
        Intent i = new Intent(c, BootReceiver.class);
        i.setAction("com.blackfalcon.periodtracker.TICK");
        PendingIntent pi = PendingIntent.getBroadcast(c, 7, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.set(AlarmManager.RTC_WAKEUP, t.getTimeInMillis(), pi);
    }
}
