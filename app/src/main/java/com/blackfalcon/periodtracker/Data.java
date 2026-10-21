package com.blackfalcon.periodtracker;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class Data {
    private final SharedPreferences p;

    public Data(Context c) {
        p = c.getSharedPreferences("period", Context.MODE_PRIVATE);
    }

    // ----- settings -----
    public String lang() { return p.getString("lang", "ur"); }
    public void setLang(String l) { p.edit().putString("lang", l).apply(); }
    public boolean dark() { return p.getBoolean("dark", true); }
    public void setDark(boolean v) { p.edit().putBoolean("dark", v).apply(); }
    public int cycle() { return p.getInt("cycle", 28); }
    public void setCycle(int v) { p.edit().putInt("cycle", v).apply(); }
    public int periodLen() { return p.getInt("plen", 5); }
    public void setPeriodLen(int v) { p.edit().putInt("plen", v).apply(); }
    public boolean notifOn() { return p.getBoolean("notif", true); }
    public void setNotifOn(boolean v) { p.edit().putBoolean("notif", v).apply(); }
    public boolean hide() { return p.getBoolean("hide", true); }
    public void setHide(boolean v) { p.edit().putBoolean("hide", v).apply(); }
    public boolean rtl() { return !lang().equals("en"); }

    public String tr(String ur, String en, String ar) {
        String l = lang();
        if (l.equals("en")) return en;
        if (l.equals("ar")) return ar;
        return ur;
    }

    // ----- periods: "start:end,start:end" (end = -1 while ongoing) -----
    public ArrayList<int[]> periods() {
        ArrayList<int[]> l = new ArrayList<int[]>();
        String s = p.getString("periods", "");
        if (s.length() > 0) {
            for (String t : s.split(",")) {
                String[] a = t.split(":");
                if (a.length == 2) {
                    try {
                        l.add(new int[] {Integer.parseInt(a[0]), Integer.parseInt(a[1])});
                    } catch (NumberFormatException e) { }
                }
            }
        }
        Collections.sort(l, (x, y) -> Integer.compare(x[0], y[0]));
        return l;
    }

    void savePeriods(ArrayList<int[]> l) {
        StringBuilder sb = new StringBuilder();
        for (int[] a : l) {
            if (sb.length() > 0) sb.append(',');
            sb.append(a[0]).append(':').append(a[1]);
        }
        p.edit().putString("periods", sb.toString()).apply();
    }

    public Cycle cycleInfo(int today) {
        return Cycle.of(periods(), cycle(), periodLen(), today);
    }

    public boolean addStart(int day) {
        int today = D.today();
        Cycle c = cycleInfo(today);
        if (c.logged(day)) return false;
        ArrayList<int[]> l = periods();
        for (int[] a : l) {
            if (a[1] < 0 && a[0] < day) a[1] = Math.min(a[0] + c.periodLen - 1, day - 1);
        }
        l.add(new int[] {day, -1});
        savePeriods(l);
        return true;
    }

    public boolean setEnd(int day) {
        ArrayList<int[]> l = periods();
        int[] best = null;
        for (int[] a : l) {
            if (a[0] <= day) best = a;
        }
        if (best == null || day - best[0] > 20) return false;
        best[1] = day;
        savePeriods(l);
        return true;
    }

    public void removeAt(int day) {
        Cycle c = cycleInfo(D.today());
        ArrayList<int[]> l = periods();
        for (int i = 0; i < l.size(); i++) {
            int[] a = l.get(i);
            if (day >= a[0] && day <= c.endEff(a)) {
                l.remove(i);
                break;
            }
        }
        savePeriods(l);
    }

    public void removeIndex(int idx) {
        ArrayList<int[]> l = periods();
        if (idx >= 0 && idx < l.size()) {
            l.remove(idx);
            savePeriods(l);
        }
    }

    // ----- daily logs: {"<day>":{"f":flow,"s":symptomMask,"n":"note"}} -----
    public JSONObject logs() {
        try {
            return new JSONObject(p.getString("logs", "{}"));
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public JSONObject log(int day) {
        return logs().optJSONObject(String.valueOf(day));
    }

    public void putLog(int day, int flow, int mask, String note) {
        JSONObject o = logs();
        try {
            if (flow == 0 && mask == 0 && note.length() == 0) {
                o.remove(String.valueOf(day));
            } else {
                JSONObject e = new JSONObject();
                e.put("f", flow);
                e.put("s", mask);
                e.put("n", note);
                o.put(String.valueOf(day), e);
            }
        } catch (Exception e) { }
        p.edit().putString("logs", o.toString()).apply();
    }
}
