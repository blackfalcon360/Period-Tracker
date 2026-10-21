package com.blackfalcon.periodtracker;

import java.util.Calendar;
import java.util.TimeZone;

/** Date helpers: a day is "days since 1970-01-01" (local calendar day). */
public class D {
    static final String[] MON = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public static int today() {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        long local = now + c.getTimeZone().getOffset(now);
        return (int) Math.floorDiv(local, 86400000L);
    }

    public static int make(int y, int m, int d) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(y, m, d);
        return (int) Math.floorDiv(c.getTimeInMillis(), 86400000L);
    }

    public static int[] ymd(int day) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(day * 86400000L);
        return new int[] {c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)};
    }

    /** 0 = Sunday ... 6 = Saturday. 1970-01-01 was a Thursday. */
    public static int dow(int day) {
        return Math.floorMod(day + 4, 7);
    }

    public static int dim(int y, int m) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(y, m, 1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static String fmt(int day) {
        int[] a = ymd(day);
        return a[2] + " " + MON[a[1]] + " " + a[0];
    }
}
