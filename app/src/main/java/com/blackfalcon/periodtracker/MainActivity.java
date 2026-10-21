package com.blackfalcon.periodtracker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity {

    static final int RED = 0xFFD32F2F;
    static final int GREEN = 0xFF2E7D32;

    Data d;
    int tab = 0;
    int viewY, viewM;
    int cBg, cCard, cText, cSub;
    TextView[] tabViews;
    ScrollView[] scrolls;
    LinearLayout calBox, histBox, setBox;
    RadioGroup rgLang, rgTheme;
    int[] langIds = new int[3];
    int[] themeIds = new int[2];
    EditText etCycle, etPlen;
    CheckBox cbNotif, cbHide;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        d = new Data(this);
        int[] t = D.ymd(D.today());
        viewY = t[0];
        viewM = t[1];
        buildUI();
        showTab(0);
        askNotifPerm();
        Notif.refresh(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (calBox != null) refreshAll();
    }

    // ---------- permission (API 33+) ----------
    void askNotifPerm() {
        if (Build.VERSION.SDK_INT >= 33) askNotifPerm33();
    }

    @TargetApi(33)
    void askNotifPerm33() {
        if (checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {"android.permission.POST_NOTIFICATIONS"}, 11);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        Notif.refresh(this);
    }

    // ---------- helpers ----------
    String tr(String ur, String en, String ar) { return d.tr(ur, en, ar); }

    int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    GradientDrawable round(int color, int r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(r));
        return g;
    }

    TextView tv(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    LinearLayout cardBox() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(round(cCard, 14));
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(10), dp(6), dp(10), dp(6));
        c.setLayoutParams(lp);
        return c;
    }

    Button btn(String s, int color, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setBackground(round(color, 10));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    EditText edit(String val, boolean number) {
        EditText e = new EditText(this);
        e.setText(val);
        e.setTextColor(Color.BLACK);
        e.setHintTextColor(0xFF777777);
        e.setTextSize(15);
        e.setBackground(round(0xFFFFFFFF, 8));
        e.setPadding(dp(10), dp(8), dp(10), dp(8));
        if (number) {
            e.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            e.setMinLines(2);
            e.setGravity(Gravity.TOP);
        }
        return e;
    }

    String[] monthNames() {
        String l = d.lang();
        if (l.equals("en")) return new String[] {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
        if (l.equals("ar")) return new String[] {"يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
            "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"};
        return new String[] {"جنوری", "فروری", "مارچ", "اپریل", "مئی", "جون",
            "جولائی", "اگست", "ستمبر", "اکتوبر", "نومبر", "دسمبر"};
    }

    String[] dayNames() {
        String l = d.lang();
        if (l.equals("en")) return new String[] {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        if (l.equals("ar")) return new String[] {"أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت"};
        return new String[] {"اتوار", "پیر", "منگل", "بدھ", "جمعرات", "جمعہ", "ہفتہ"};
    }

    String[] flowNames() {
        return new String[] {
            tr("کوئی نہیں", "None", "لا شيء"), tr("ہلکا", "Light", "خفيف"),
            tr("درمیانہ", "Medium", "متوسط"), tr("زیادہ", "Heavy", "غزير")};
    }

    String[] symNames() {
        String l = d.lang();
        if (l.equals("en")) return new String[] {"Cramps", "Headache", "Bloating", "Backache",
            "Fatigue", "Mood swings", "Nausea", "Acne"};
        if (l.equals("ar")) return new String[] {"تشنجات", "صداع", "انتفاخ", "ألم الظهر",
            "إرهاق", "تقلب المزاج", "غثيان", "حب الشباب"};
        return new String[] {"درد/اینٹھن", "سر درد", "پیٹ پھولنا", "کمر درد",
            "تھکن", "مزاج میں تبدیلی", "متلی", "دانے"};
    }

    // ---------- main UI ----------
    void buildUI() {
        boolean dark = d.dark();
        cBg = dark ? 0xFF000000 : 0xFFF2F2F2;
        cCard = dark ? 0xFF1A1A1A : 0xFFFFFFFF;
        cText = dark ? 0xFFFFFFFF : 0xFF111111;
        cSub = dark ? 0xFFAAAAAA : 0xFF555555;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(cBg);
        root.setLayoutDirection(d.rtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        TextView title = tv("Period Tracker", 22, cText, true);
        title.setPadding(dp(14), dp(14), dp(14), dp(6));
        root.addView(title);

        String[] names = {tr("تقویم", "Calendar", "التقويم"), tr("ریکارڈ", "History", "السجل"),
            tr("سیٹنگز", "Settings", "الإعدادات")};
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(8), dp(4), dp(8), dp(8));
        tabViews = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView t = tv(names[i], 15, cText, false);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(8), dp(9), dp(8), dp(9));
            t.setOnClickListener(v -> showTab(idx));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(dp(4), 0, dp(4), 0);
            tabs.addView(t, lp);
            tabViews[i] = t;
        }
        root.addView(tabs);

        FrameLayout content = new FrameLayout(this);
        scrolls = new ScrollView[3];
        calBox = new LinearLayout(this);
        histBox = new LinearLayout(this);
        setBox = new LinearLayout(this);
        LinearLayout[] boxes = {calBox, histBox, setBox};
        for (int i = 0; i < 3; i++) {
            boxes[i].setOrientation(LinearLayout.VERTICAL);
            boxes[i].setPadding(0, dp(4), 0, dp(12));
            scrolls[i] = new ScrollView(this);
            scrolls[i].addView(boxes[i]);
            content.addView(scrolls[i], new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView credit = tv("By: Black Falcon \uD83E\uDD85", 13, cSub, false);
        credit.setGravity(Gravity.CENTER);
        credit.setPadding(0, dp(8), 0, dp(10));
        root.addView(credit);

        setContentView(root);
        buildSettings();
        renderCal();
        renderHist();
    }

    void showTab(int i) {
        tab = i;
        for (int k = 0; k < 3; k++) {
            boolean sel = k == i;
            tabViews[k].setTextColor(sel ? Color.WHITE : cText);
            tabViews[k].setBackground(round(sel ? RED : cCard, 20));
            scrolls[k].setVisibility(sel ? View.VISIBLE : View.GONE);
        }
    }

    void refreshAll() {
        renderCal();
        renderHist();
        Notif.refresh(this);
    }

    // ---------- calendar tab ----------
    void renderCal() {
        calBox.removeAllViews();
        final int today = D.today();
        Cycle cy = d.cycleInfo(today);

        LinearLayout card = cardBox();
        String[] tx = Notif.texts(d);
        card.addView(tv(tx[0], 22, cText, true));
        card.addView(tv(tx[1], 14, cSub, false));
        final boolean open = cy.ongoing();
        card.addView(btn(open ? tr("آج پیریڈ ختم ہوا", "Period ended today", "انتهت الدورة اليوم")
                : tr("آج پیریڈ شروع ہوا", "Period started today", "بدأت الدورة اليوم"), RED, v -> {
            if (open) d.setEnd(today);
            else d.addStart(today);
            refreshAll();
        }));
        card.addView(btn(tr("آج کی علامات / نوٹ", "Log today's symptoms / note", "تسجيل أعراض اليوم"),
                0xFF455A64, v -> logDialog(today)));
        calBox.addView(card);

        // month navigation
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(14), dp(8), dp(14), dp(4));
        boolean rtl = d.rtl();
        TextView prev = tv(rtl ? "\u203A" : "\u2039", 30, cText, true);
        TextView next = tv(rtl ? "\u2039" : "\u203A", 30, cText, true);
        prev.setPadding(dp(18), 0, dp(18), 0);
        next.setPadding(dp(18), 0, dp(18), 0);
        prev.setOnClickListener(v -> { shiftMonth(-1); });
        next.setOnClickListener(v -> { shiftMonth(1); });
        TextView mt = tv(monthNames()[viewM] + " " + viewY, 18, cText, true);
        mt.setGravity(Gravity.CENTER);
        nav.addView(prev);
        nav.addView(mt, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        nav.addView(next);
        calBox.addView(nav);

        // weekday header
        LinearLayout hdr = new LinearLayout(this);
        hdr.setOrientation(LinearLayout.HORIZONTAL);
        hdr.setPadding(dp(6), 0, dp(6), dp(2));
        String[] dn = dayNames();
        for (int i = 0; i < 7; i++) {
            TextView t = tv(dn[i], 10, cSub, false);
            t.setGravity(Gravity.CENTER);
            hdr.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        calBox.addView(hdr);

        // grid
        int first = D.make(viewY, viewM, 1);
        int offset = D.dow(first);
        int n = D.dim(viewY, viewM);
        int rows = (offset + n + 6) / 7;
        JSONObject logs = d.logs();
        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dp(6), dp(2), dp(6), dp(2));
            for (int c = 0; c < 7; c++) {
                int num = r * 7 + c - offset + 1;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(46), 1f);
                lp.setMargins(dp(2), 0, dp(2), 0);
                if (num < 1 || num > n) {
                    row.addView(new View(this), lp);
                } else {
                    row.addView(cell(first + num - 1, num, cy, today, logs), lp);
                }
            }
            calBox.addView(row);
        }

        // legend
        LinearLayout lg = cardBox();
        lg.addView(legend(RED, tr("پیریڈ (درج شدہ)", "Period (logged)", "الدورة (مسجلة)")));
        lg.addView(legend(0x88D32F2F, tr("متوقع پیریڈ", "Predicted period", "الدورة المتوقعة")));
        lg.addView(legend(GREEN, tr("بیضہ ریزی (اندازہ)", "Ovulation (estimate)", "الإباضة (تقديري)")));
        lg.addView(legend(0x882E7D32, tr("زرخیزی کی مدت (اندازہ)", "Fertile window (estimate)",
                "فترة الخصوبة (تقديري)")));
        lg.addView(tv(tr("دن پر ٹیپ کریں: پیریڈ شروع/ختم یا علامات درج کریں۔",
                "Tap a day to mark period start/end or log symptoms.",
                "اضغط على اليوم لتسجيل بداية/نهاية الدورة أو الأعراض."), 12, cSub, false));
        calBox.addView(lg);

        TextView note = tv(tr(
            "یہ اندازے صرف آپ کے درج کردہ ریکارڈ پر مبنی ہیں۔ یہ ایپ مانع حمل یا طبی مشورے کا متبادل نہیں؛ کوئی تشویش ہو تو ڈاکٹر سے رجوع کریں۔",
            "Predictions are estimates based only on what you log. This app is not a contraceptive or medical advice; see a doctor if you are concerned.",
            "التوقعات تقديرية وتعتمد على ما تسجله فقط. التطبيق ليس وسيلة منع حمل ولا بديلًا عن الاستشارة الطبية."),
            11, cSub, false);
        note.setPadding(dp(14), dp(4), dp(14), dp(8));
        calBox.addView(note);
    }

    void shiftMonth(int delta) {
        viewM += delta;
        if (viewM < 0) { viewM = 11; viewY--; }
        if (viewM > 11) { viewM = 0; viewY++; }
        renderCal();
    }

    TextView cell(final int day, int num, Cycle cy, int today, JSONObject logs) {
        boolean hasLog = logs.optJSONObject(String.valueOf(day)) != null;
        TextView t = new TextView(this);
        t.setText(hasLog ? num + "\n\u2022" : String.valueOf(num));
        t.setTextSize(hasLog ? 12 : 14);
        t.setGravity(Gravity.CENTER);
        int fill = 0;
        boolean light = false;
        if (cy.logged(day)) { fill = RED; light = true; }
        else if (cy.isOvu(day)) { fill = GREEN; light = true; }
        else if (cy.pred(day)) fill = 0x88D32F2F;
        else if (cy.fertile(day)) fill = 0x882E7D32;
        t.setTextColor(light ? Color.WHITE : cText);
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(10));
        g.setColor(fill);
        if (day == today) {
            g.setStroke(dp(2), cText);
            t.setTypeface(Typeface.DEFAULT_BOLD);
        }
        t.setBackground(g);
        t.setOnClickListener(v -> dayDialog(day));
        return t;
    }

    LinearLayout legend(int color, String label) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(3), 0, dp(3));
        TextView dot = tv("\u25CF", 18, color, false);
        dot.setPadding(0, 0, dp(8), 0);
        r.addView(dot);
        r.addView(tv(label, 13, cText, false));
        return r;
    }

    // ---------- dialogs ----------
    void dayDialog(final int day) {
        Cycle cy = d.cycleInfo(D.today());
        final ArrayList<String> labels = new ArrayList<String>();
        final ArrayList<Integer> acts = new ArrayList<Integer>();
        boolean in = cy.logged(day);
        if (!in) {
            labels.add(tr("اس دن پیریڈ شروع", "Period started this day", "بدأت الدورة هذا اليوم"));
            acts.add(0);
        }
        int[] best = null;
        for (int[] p : cy.per) if (p[0] <= day) best = p;
        if (best != null && day - best[0] <= 20) {
            labels.add(tr("اس دن پیریڈ ختم", "Period ended this day", "انتهت الدورة هذا اليوم"));
            acts.add(1);
        }
        labels.add(tr("علامات / نوٹ درج کریں", "Log symptoms / note", "تسجيل الأعراض / ملاحظة"));
        acts.add(2);
        if (in) {
            labels.add(tr("یہ پیریڈ حذف کریں", "Delete this period", "حذف هذه الدورة"));
            acts.add(3);
        }
        new AlertDialog.Builder(this)
            .setTitle(D.fmt(day))
            .setItems(labels.toArray(new CharSequence[0]), (dlg, which) -> {
                int a = acts.get(which);
                if (a == 0) { d.addStart(day); refreshAll(); }
                else if (a == 1) { d.setEnd(day); refreshAll(); }
                else if (a == 2) logDialog(day);
                else { d.removeAt(day); refreshAll(); }
            })
            .setNegativeButton(tr("بند", "Close", "إغلاق"), null)
            .show();
    }

    void logDialog(final int day) {
        JSONObject cur = d.log(day);
        int flow0 = cur == null ? 0 : cur.optInt("f", 0);
        int mask0 = cur == null ? 0 : cur.optInt("s", 0);
        String note0 = cur == null ? "" : cur.optString("n", "");

        ScrollView sv = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(0xFF222222);
        box.setPadding(dp(16), dp(10), dp(16), dp(10));
        box.setLayoutDirection(d.rtl() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        sv.addView(box);

        box.addView(tv(tr("بہاؤ", "Flow", "التدفق"), 15, Color.WHITE, true));
        final RadioGroup rg = new RadioGroup(this);
        final int[] ids = new int[4];
        String[] fn = flowNames();
        for (int i = 0; i < 4; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(fn[i]);
            rb.setTextColor(Color.WHITE);
            rb.setId(View.generateViewId());
            ids[i] = rb.getId();
            rg.addView(rb);
        }
        rg.check(ids[Math.max(0, Math.min(3, flow0))]);
        box.addView(rg);

        box.addView(tv(tr("علامات", "Symptoms", "الأعراض"), 15, Color.WHITE, true));
        String[] sn = symNames();
        final CheckBox[] cbs = new CheckBox[sn.length];
        for (int i = 0; i < sn.length; i++) {
            cbs[i] = new CheckBox(this);
            cbs[i].setText(sn[i]);
            cbs[i].setTextColor(Color.WHITE);
            cbs[i].setChecked((mask0 & (1 << i)) != 0);
            box.addView(cbs[i]);
        }
        box.addView(tv(tr("نوٹ", "Note", "ملاحظة"), 15, Color.WHITE, true));
        final EditText en = edit(note0, false);
        box.addView(en);

        new AlertDialog.Builder(this)
            .setTitle(D.fmt(day))
            .setView(sv)
            .setPositiveButton(tr("محفوظ", "Save", "حفظ"), (dlg, w) -> {
                int flow = 0;
                for (int i = 0; i < 4; i++) if (ids[i] == rg.getCheckedRadioButtonId()) flow = i;
                int mask = 0;
                for (int i = 0; i < cbs.length; i++) if (cbs[i].isChecked()) mask |= (1 << i);
                String nt = en.getText().toString().trim().replace("\n", " ");
                d.putLog(day, flow, mask, nt);
                refreshAll();
            })
            .setNegativeButton(tr("منسوخ", "Cancel", "إلغاء"), null)
            .show();
    }

    // ---------- history tab ----------
    void renderHist() {
        histBox.removeAllViews();
        int today = D.today();
        Cycle cy = d.cycleInfo(today);
        ArrayList<int[]> per = d.periods();

        LinearLayout st = cardBox();
        st.addView(tv(tr("اعداد و شمار", "Statistics", "الإحصاءات"), 18, cText, true));
        if (per.size() == 0) {
            st.addView(tv(tr("ابھی کوئی ریکارڈ نہیں۔", "No records yet.", "لا توجد سجلات بعد."), 14, cSub, false));
        } else {
            st.addView(tv(tr("درج شدہ پیریڈ: ", "Periods logged: ", "عدد الدورات: ") + per.size(), 14, cText, false));
            st.addView(tv(tr("اوسط سائیکل: ", "Average cycle: ", "متوسط الدورة: ") + cy.cycleLen
                    + tr(" دن", " days", " يومًا"), 14, cText, false));
            st.addView(tv(tr("اوسط پیریڈ کی لمبائی: ", "Average period length: ", "متوسط مدة الحيض: ")
                    + cy.periodLen + tr(" دن", " days", " يومًا"), 14, cText, false));
            st.addView(tv(tr("آخری آغاز: ", "Last start: ", "آخر بداية: ") + D.fmt(cy.lastStart), 14, cText, false));
            st.addView(tv(tr("اگلا متوقع: ", "Next expected: ", "المتوقعة القادمة: ") + D.fmt(cy.nextStart()), 14, cText, false));
        }
        histBox.addView(st);

        LinearLayout pl = cardBox();
        pl.addView(tv(tr("پیریڈ کی فہرست", "Period list", "قائمة الدورات"), 18, cText, true));
        for (int i = per.size() - 1; i >= 0; i--) {
            final int idx = i;
            int[] p = per.get(i);
            String line = D.fmt(p[0]) + " \u2192 " + (p[1] >= 0 ? D.fmt(p[1]) : tr("جاری", "ongoing", "جارية"));
            if (p[1] >= 0) line += "  (" + (p[1] - p[0] + 1) + tr(" دن", " d", " ي") + ")";
            if (i > 0) line += "  \u2022 " + tr("سائیکل ", "cycle ", "دورة ") + (p[0] - per.get(i - 1)[0]);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));
            row.addView(tv(line, 14, cText, false),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            TextView del = tv("\u2715", 18, RED, true);
            del.setPadding(dp(12), dp(4), dp(12), dp(4));
            del.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage(tr("یہ ریکارڈ حذف کریں؟", "Delete this record?", "حذف هذا السجل؟"))
                .setPositiveButton(tr("ہاں", "Yes", "نعم"), (dlg, w) -> { d.removeIndex(idx); refreshAll(); })
                .setNegativeButton(tr("نہیں", "No", "لا"), null)
                .show());
            row.addView(del);
            pl.addView(row);
        }
        histBox.addView(pl);

        // recent symptom logs
        LinearLayout lg = cardBox();
        lg.addView(tv(tr("علامات کا ریکارڈ", "Symptom log", "سجل الأعراض"), 18, cText, true));
        JSONObject logs = d.logs();
        ArrayList<Integer> days = new ArrayList<Integer>();
        java.util.Iterator<String> it = logs.keys();
        while (it.hasNext()) {
            try { days.add(Integer.parseInt(it.next())); } catch (NumberFormatException e) { }
        }
        java.util.Collections.sort(days, java.util.Collections.reverseOrder());
        String[] sn = symNames();
        String[] fn = flowNames();
        int shown = 0;
        for (int dayv : days) {
            JSONObject e = logs.optJSONObject(String.valueOf(dayv));
            if (e == null) continue;
            StringBuilder sb = new StringBuilder();
            int f = e.optInt("f", 0);
            if (f > 0) sb.append(tr("بہاؤ: ", "Flow: ", "التدفق: ")).append(fn[Math.min(3, f)]).append("  ");
            int m = e.optInt("s", 0);
            for (int i = 0; i < sn.length; i++) if ((m & (1 << i)) != 0) sb.append(sn[i]).append(", ");
            String nt = e.optString("n", "");
            if (nt.length() > 0) sb.append("\u2014 ").append(nt);
            LinearLayout one = new LinearLayout(this);
            one.setOrientation(LinearLayout.VERTICAL);
            one.setPadding(0, dp(6), 0, dp(6));
            one.setOnClickListener(v -> logDialog(dayv));
            one.addView(tv(D.fmt(dayv), 13, RED, true));
            one.addView(tv(sb.toString(), 13, cText, false));
            lg.addView(one);
            if (++shown >= 30) break;
        }
        if (shown == 0) lg.addView(tv(tr("کوئی اندراج نہیں۔", "No entries.", "لا توجد إدخالات."), 14, cSub, false));
        histBox.addView(lg);
    }

    // ---------- settings tab ----------
    TextView label(String s) {
        TextView t = tv(s, 16, cText, true);
        t.setPadding(dp(14), dp(14), dp(14), dp(6));
        return t;
    }

    RadioGroup radios(String[] labels, int checked, int[] ids) {
        RadioGroup g = new RadioGroup(this);
        g.setPadding(dp(14), 0, dp(14), 0);
        for (int i = 0; i < labels.length; i++) {
            RadioButton r = new RadioButton(this);
            r.setText(labels[i]);
            r.setTextColor(cText);
            r.setTextSize(15);
            r.setId(View.generateViewId());
            ids[i] = r.getId();
            g.addView(r);
        }
        g.check(ids[checked]);
        return g;
    }

    void buildSettings() {
        setBox.removeAllViews();
        setBox.addView(label(tr("زبان", "Language", "اللغة")));
        String l = d.lang();
        rgLang = radios(new String[] {"اردو", "English", "العربية"},
                l.equals("en") ? 1 : (l.equals("ar") ? 2 : 0), langIds);
        setBox.addView(rgLang);

        setBox.addView(label(tr("تھیم", "Theme", "المظهر")));
        rgTheme = radios(new String[] {tr("ڈارک موڈ", "Dark mode", "الوضع الداكن"),
            tr("لائٹ موڈ", "Light mode", "الوضع الفاتح")}, d.dark() ? 0 : 1, themeIds);
        setBox.addView(rgTheme);

        setBox.addView(label(tr("عام سائیکل کی لمبائی (دن)", "Default cycle length (days)",
                "طول الدورة الافتراضي (أيام)")));
        etCycle = edit(String.valueOf(d.cycle()), true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(14), 0, dp(14), 0);
        setBox.addView(etCycle, lp);

        setBox.addView(label(tr("عام پیریڈ کی لمبائی (دن)", "Default period length (days)",
                "مدة الحيض الافتراضية (أيام)")));
        etPlen = edit(String.valueOf(d.periodLen()), true);
        setBox.addView(etPlen, lp);

        cbNotif = new CheckBox(this);
        cbNotif.setText(tr("نوٹیفکیشن بار میں دکھائیں", "Show in notification bar", "عرض في شريط الإشعارات"));
        cbNotif.setTextColor(cText);
        cbNotif.setChecked(d.notifOn());
        cbNotif.setPadding(dp(14), dp(10), dp(14), 0);
        setBox.addView(cbNotif);

        cbHide = new CheckBox(this);
        cbHide.setText(tr("لاک اسکرین پر تفصیل چھپائیں", "Hide details on lock screen",
                "إخفاء التفاصيل في شاشة القفل"));
        cbHide.setTextColor(cText);
        cbHide.setChecked(d.hide());
        cbHide.setPadding(dp(14), dp(4), dp(14), 0);
        setBox.addView(cbHide);

        LinearLayout holder = new LinearLayout(this);
        holder.setOrientation(LinearLayout.VERTICAL);
        holder.setPadding(dp(14), 0, dp(14), 0);
        holder.addView(btn(tr("محفوظ کریں", "Save", "حفظ"), RED, v -> saveSettings()));
        holder.addView(btn(tr("نوٹیفکیشن کی اجازت", "Notification permission", "إذن الإشعارات"),
                0xFF455A64, v -> askNotifPerm()));
        setBox.addView(holder);

        TextView info = tv(tr(
            "ویجٹ: ہوم اسکرین پر دیر تک دبائیں ← ویجٹ ← Period Tracker۔\nتمام ڈیٹا صرف آپ کے فون میں رہتا ہے؛ ایپ انٹرنیٹ استعمال نہیں کرتی۔ فون ری اسٹارٹ ہونے پر نوٹیفکیشن خود واپس آ جاتا ہے۔",
            "Widget: long-press the home screen \u2192 Widgets \u2192 Period Tracker.\nAll data stays on your phone; the app uses no internet. The notification returns automatically after a reboot.",
            "الودجت: اضغط مطولًا على الشاشة الرئيسية \u2190 الودجات \u2190 Period Tracker.\nتبقى كل البيانات على هاتفك والتطبيق لا يستخدم الإنترنت. يعود الإشعار تلقائيًا بعد إعادة التشغيل."),
            12, cSub, false);
        info.setPadding(dp(14), dp(16), dp(14), dp(8));
        setBox.addView(info);
    }

    int num(EditText e, int def, int lo, int hi) {
        try {
            int v = Integer.parseInt(e.getText().toString().trim());
            return Math.max(lo, Math.min(hi, v));
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    void saveSettings() {
        String[] langs = {"ur", "en", "ar"};
        int cur = rgLang.getCheckedRadioButtonId();
        for (int i = 0; i < 3; i++) if (langIds[i] == cur) d.setLang(langs[i]);
        d.setDark(rgTheme.getCheckedRadioButtonId() == themeIds[0]);
        d.setCycle(num(etCycle, 28, 15, 60));
        d.setPeriodLen(num(etPlen, 5, 1, 14));
        d.setNotifOn(cbNotif.isChecked());
        d.setHide(cbHide.isChecked());
        buildUI();
        showTab(2);
        Notif.refresh(this);
        Toast.makeText(this, tr("محفوظ ہو گیا", "Saved", "تم الحفظ"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle(tr("ایپ بند کریں؟", "Exit app?", "إغلاق التطبيق؟"))
            .setMessage(tr("کیا آپ واقعی باہر نکلنا چاہتے ہیں؟", "Do you really want to exit?",
                    "هل تريد الخروج فعلًا؟"))
            .setPositiveButton(tr("ہاں", "Yes", "نعم"), (dlg, w) -> finish())
            .setNegativeButton(tr("نہیں", "No", "لا"), null)
            .show();
    }
}
