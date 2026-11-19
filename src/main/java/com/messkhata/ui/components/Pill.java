package com.messkhata.ui.components;

import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A small coloured tag, e.g. "Active", "Deposit", "Will pay".
 */
public class Pill extends JLabel {

    private Color fill = Theme.PAPER;

    public Pill() {
        setFont(Theme.font(Font.BOLD, 11));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        setOpaque(false);
    }

    public Pill(String text, Color fill, Color textColor) {
        this();
        set(text, fill, textColor);
    }

    public final void set(String text, Color fill, Color textColor) {
        setText(text);
        this.fill = fill;
        setForeground(textColor);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int textW = getFontMetrics(getFont()).stringWidth(getText()) + 20;
        int h = Math.min(getHeight() - 4, 22);
        int w = Math.min(getWidth() - 4, textW);
        int x;
        if (getHorizontalAlignment() == SwingConstants.LEFT) {
            x = getInsets().left - 10;
        } else if (getHorizontalAlignment() == SwingConstants.RIGHT) {
            x = getWidth() - getInsets().right + 10 - w;
        } else {
            x = (getWidth() - w) / 2;
        }
        g2.setColor(fill);
        g2.fillRoundRect(Math.max(x, 0), (getHeight() - h) / 2, w, h, h, h);
        g2.dispose();
        super.paintComponent(g);
    }
}
