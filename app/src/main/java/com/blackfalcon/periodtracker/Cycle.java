package com.blackfalcon.periodtracker;

import java.util.ArrayList;

/** Pure prediction logic (no Android classes). Estimates only. */
public class Cycle {
    public int cycleLen, periodLen, lastStart = -1, today;
    public ArrayList<int[]> per = new ArrayList<int[]>();

    public static Cycle of(ArrayList<int[]> periods, int defCycle, int defPeriod, int today) {
        Cycle c = new Cycle();
        c.today = today;
        c.per = periods;
        c.cycleLen = defCycle;
        c.periodLen = defPeriod;
        int n = periods.size();
        if (n > 0) c.lastStart = periods.get(n - 1)[0];
        if (n >= 2) {
            int from = Math.max(1, n - 6);
            int sum = 0, cnt = 0;
            for (int i = from; i < n; i++) {
                int g = periods.get(i)[0] - periods.get(i - 1)[0];
                if (g >= 15 && g <= 60) { sum += g; cnt++; }
            }
            if (cnt > 0) c.cycleLen = Math.round((float) sum / cnt);
        }
        int s2 = 0, c2 = 0;
        for (int[] p : periods) {
            if (p[1] >= p[0]) { s2 += p[1] - p[0] + 1; c2++; }
        }
        if (c2 > 0) c.periodLen = Math.round((float) s2 / c2);
        if (c.periodLen < 1) c.periodLen = 1;
        if (c.periodLen > 14) c.periodLen = 14;
        if (c.cycleLen < 15) c.cycleLen = 15;
        if (c.cycleLen > 60) c.cycleLen = 60;
        return c;
    }

    public int endEff(int[] p) {
        if (p[1] >= 0) return p[1];
        return Math.min(today, p[0] + Math.max(periodLen, 5) + 2);
    }

    public boolean logged(int day) {
        for (int[] p : per) {
            if (day >= p[0] && day <= endEff(p)) return true;
        }
        return false;
    }

    /** 1-based day number inside a logged period, or 0. */
    public int periodDayNo(int day) {
        for (int[] p : per) {
            if (day >= p[0] && day <= endEff(p)) return day - p[0] + 1;
        }
        return 0;
    }

    public boolean ongoing() {
        return per.size() > 0 && per.get(per.size() - 1)[1] < 0;
    }

    public int nextStart() {
        return lastStart < 0 ? -1 : lastStart + cycleLen;
    }

    public boolean pred(int day) {
        if (lastStart < 0) return false;
        int[] l = per.get(per.size() - 1);
        if (l[1] < 0 && day > today && day <= l[0] + periodLen - 1) return true;
        for (int k = 1; k <= 3; k++) {
            int s = lastStart + k * cycleLen;
            if (day >= s && day <= s + periodLen - 1 && !logged(day)) return true;
        }
        return false;
    }

    public int ovu(int k) { return lastStart + k * cycleLen - 14; }

    public boolean isOvu(int day) {
        if (lastStart < 0) return false;
        for (int k = 1; k <= 3; k++) if (day == ovu(k)) return true;
        return false;
    }

    public boolean fertile(int day) {
        if (lastStart < 0) return false;
        for (int k = 1; k <= 3; k++) {
            int o = ovu(k);
            if (day >= o - 5 && day <= o + 1) return true;
        }
        return false;
    }
}
