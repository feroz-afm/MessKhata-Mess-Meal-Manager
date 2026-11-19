package com.messkhata.model;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The meal chart of one month: how many meals each member ate on each day.
 */
public class MealSheet {

    private final YearMonth month;
    private final Map<String, double[]> meals = new LinkedHashMap<>();

    public MealSheet(YearMonth month) {
        this.month = month;
    }

    public YearMonth getMonth() {
        return month;
    }

    public int getDays() {
        return month.lengthOfMonth();
    }

    private double[] row(String memberId) {
        return meals.computeIfAbsent(memberId, k -> new double[getDays()]);
    }

    public double getMeal(String memberId, int day) {
        double[] r = meals.get(memberId);
        return r == null ? 0 : r[day - 1];
    }

    public void setMeal(String memberId, int day, double count) {
        row(memberId)[day - 1] = count;
    }

    public boolean hasMember(String memberId) {
        return meals.containsKey(memberId);
    }

    public double getMemberTotal(String memberId) {
        double total = 0;
        double[] r = meals.get(memberId);
        if (r != null) {
            for (double v : r) {
                total += v;
            }
        }
        return total;
    }

    public double getDayTotal(int day) {
        double total = 0;
        for (double[] r : meals.values()) {
            total += r[day - 1];
        }
        return total;
    }

    public double getGrandTotal() {
        double total = 0;
        for (String id : meals.keySet()) {
            total += getMemberTotal(id);
        }
        return total;
    }

    /** One line per member: id|day1|day2|... */
    public List<String> toLines() {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, double[]> e : meals.entrySet()) {
            StringBuilder sb = new StringBuilder(e.getKey());
            for (double v : e.getValue()) {
                sb.append(Storable.SEPARATOR).append(v);
            }
            lines.add(sb.toString());
        }
        return lines;
    }

    public static MealSheet fromParts(YearMonth month, List<String[]> rows) {
        MealSheet sheet = new MealSheet(month);
        for (String[] p : rows) {
            for (int day = 1; day < p.length && day <= sheet.getDays(); day++) {
                sheet.setMeal(p[0], day, Double.parseDouble(p[day]));
            }
        }
        return sheet;
    }
}
