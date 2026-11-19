package com.messkhata.ui.components;

import com.messkhata.ui.theme.Theme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * The app logo: a plate with a spoon, drawn in code (no image files needed).
 */
public class LogoMark extends JComponent {

    private final int size;

    public LogoMark(int size) {
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.TURMERIC);
        g2.fillRoundRect(0, 0, size, size, size / 3, size / 3);

        float s = size;
        g2.setColor(Theme.INK);
        g2.setStroke(new BasicStroke(s * 0.07f));
        int plate = Math.round(s * 0.56f);
        int px = Math.round(s * 0.16f);
        int py = Math.round((s - plate) / 2f);
        g2.drawOval(px, py, plate, plate);
        int inner = Math.round(plate * 0.5f);
        g2.drawOval(px + (plate - inner) / 2, py + (plate - inner) / 2, inner, inner);

        // spoon on the right
        int sx = Math.round(s * 0.82f);
        g2.setStroke(new BasicStroke(s * 0.06f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(sx, Math.round(s * 0.42f), sx, Math.round(s * 0.80f));
        int bowlW = Math.round(s * 0.12f);
        int bowlH = Math.round(s * 0.2f);
        g2.fillOval(sx - bowlW / 2, Math.round(s * 0.2f), bowlW, bowlH);
        g2.dispose();
    }
}
