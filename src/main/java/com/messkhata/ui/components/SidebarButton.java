package com.messkhata.ui.components;

import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Menu item of the left sidebar. The selected item gets a turmeric bar on the left.
 */
public class SidebarButton extends JButton {

    private final String code;
    private boolean selected;
    private boolean hover;

    /**
     * @param code two letters drawn in the small badge, e.g. "DB" for Dashboard
     */
    public SidebarButton(String text, String code) {
        super(text);
        this.code = code;
        setFont(Theme.font(Font.PLAIN, 14));
        setForeground(Theme.INK_TEXT);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(0, 64, 0, 12));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setPreferredSize(new Dimension(220, 44));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public void setSelectedItem(boolean selected) {
        this.selected = selected;
        setForeground(selected ? Color.WHITE : Theme.INK_TEXT);
        setFont(Theme.font(selected ? Font.BOLD : Font.PLAIN, 14));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (selected || hover) {
            g2.setColor(Theme.INK_SOFT);
            g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 10, 10);
        }
        if (selected) {
            g2.setColor(Theme.TURMERIC);
            g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
        }
        // small badge with two letters
        int size = 26;
        int x = 26;
        int y = (getHeight() - size) / 2;
        g2.setColor(selected ? Theme.TURMERIC : new Color(255, 255, 255, 28));
        g2.fillRoundRect(x, y, size, size, 8, 8);
        g2.setFont(Theme.font(Font.BOLD, 10));
        g2.setColor(selected ? Theme.INK : Theme.INK_TEXT);
        int w = g2.getFontMetrics().stringWidth(code);
        g2.drawString(code, x + (size - w) / 2, y + size / 2 + 4);
        g2.dispose();
        super.paintComponent(g);
    }
}
