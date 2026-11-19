package com.messkhata.ui.components;

import com.messkhata.ui.theme.Theme;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

/**
 * Bar chart of total meals per day, painted by hand.
 */
public class DailyMealChart extends JComponent {

    private double[] values = new double[0];
    private int highlightDay = -1;

    private static final int LEFT = 36;
    private static final int BOTTOM = 26;
    private static final int TOP = 12;

    public DailyMealChart() {
        setPreferredSize(new Dimension(500, 240));
        setToolTipText("");
    }

    public void setData(double[] values, int highlightDay) {
        this.values = values;
        this.highlightDay = highlightDay;
        repaint();
    }

    private double step() {
        double max = 0;
        for (double v : values) {
            max = Math.max(max, v);
        }
        if (max <= 8) {
            return 2;
        }
        if (max <= 20) {
            return 4;
        }
        return max <= 40 ? 10 : 20;
    }

    private double niceMax() {
        double max = 0;
        for (double v : values) {
            max = Math.max(max, v);
        }
        double step = step();
        return Math.max(step * 2, Math.ceil(max / step) * step);
    }

    private double slot() {
        return values.length == 0 ? 0 : (getWidth() - LEFT - 8) / (double) values.length;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int i = (int) ((e.getX() - LEFT) / Math.max(slot(), 1));
        if (i < 0 || i >= values.length) {
            return null;
        }
        return "Day " + (i + 1) + ": " + Theme.MEALS.format(values[i]) + " meals";
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int plotH = h - TOP - BOTTOM;
        double max = niceMax();

        g2.setFont(Theme.font(Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        // horizontal grid lines with labels
        int lines = (int) Math.round(max / step());
        for (int i = 0; i <= lines; i++) {
            int y = TOP + plotH - (int) Math.round(plotH * i / (double) lines);
            g2.setColor(i == 0 ? Theme.LINE : Theme.PAPER_DARK);
            g2.drawLine(LEFT, y, w - 8, y);
            String label = Theme.MEALS.format(step() * i);
            g2.setColor(Theme.MUTED);
            g2.drawString(label, LEFT - 8 - fm.stringWidth(label), y + 4);
        }

        double slot = slot();
        int barW = (int) Math.max(3, Math.min(18, slot * 0.62));
        for (int i = 0; i < values.length; i++) {
            int x = LEFT + (int) Math.round(i * slot + (slot - barW) / 2);
            int barH = (int) Math.round(plotH * values[i] / max);
            int y = TOP + plotH - barH;
            boolean today = (i + 1) == highlightDay;
            g2.setColor(today ? Theme.TURMERIC : new Color(0x22314A));
            if (!today) {
                g2.setColor(new Color(34, 49, 74, 200));
            }
            if (barH > 0) {
                g2.fillRoundRect(x, y, barW, barH + 3, 4, 4);
                g2.setColor(Theme.CARD);
                g2.fillRect(x, TOP + plotH + 1, barW, 3);
            }
            int day = i + 1;
            boolean nextToToday = highlightDay > 0 && Math.abs(day - highlightDay) == 1;
            if (today || ((day == 1 || day % 5 == 0) && !nextToToday)) {
                String d = String.valueOf(day);
                g2.setColor(today ? Theme.TURMERIC_DARK : Theme.MUTED);
                g2.setFont(Theme.font(today ? Font.BOLD : Font.PLAIN, 11));
                g2.drawString(d, x + (barW - g2.getFontMetrics().stringWidth(d)) / 2, h - 8);
                g2.setFont(Theme.font(Font.PLAIN, 11));
            }
        }
        g2.dispose();
    }
}
