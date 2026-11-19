package com.messkhata.ui.components;

import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Small card with a title, a big number and a note underneath.
 */
public class StatCard extends RoundedPanel {

    private final JLabel value;
    private final JLabel note;
    private final Color accent;

    public StatCard(String title, Color accent) {
        super(null);
        this.accent = accent;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 18));

        JLabel t = Theme.label(title.toUpperCase(), Font.BOLD, 11, Theme.MUTED);
        value = Theme.label("-", Font.BOLD, 26, Theme.TEXT);
        note = Theme.label(" ", Font.PLAIN, 12, Theme.MUTED);
        add(t);
        add(javax.swing.Box.createVerticalStrut(6));
        add(value);
        add(javax.swing.Box.createVerticalStrut(2));
        add(note);
    }

    public void setValue(String text) {
        value.setText(text);
    }

    public void setValueColor(Color c) {
        value.setForeground(c);
    }

    public void setNote(String text) {
        note.setText(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(accent);
        g2.fillRoundRect(0, 16, 5, getHeight() - 32, 5, 5);
        g2.dispose();
    }
}
