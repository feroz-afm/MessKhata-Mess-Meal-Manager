package com.messkhata.ui.components;

import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A flat rounded button that changes colour on hover.
 */
public class FlatButton extends JButton {

    public enum Style { PRIMARY, DARK, OUTLINE, DANGER, GHOST }

    private final Style style;
    private boolean hover;

    public FlatButton(String text, Style style) {
        super(text);
        this.style = style;
        setFont(Theme.font(Font.BOLD, 13));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        setForeground(textColor());
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

    private Color textColor() {
        switch (style) {
            case PRIMARY:
                return Theme.INK;
            case OUTLINE:
            case GHOST:
                return Theme.INK;
            default:
                return Color.WHITE;
        }
    }

    private Color fillColor() {
        switch (style) {
            case PRIMARY:
                return hover ? Theme.TURMERIC_DARK : Theme.TURMERIC;
            case DARK:
                return hover ? Theme.INK_SOFT : Theme.INK;
            case DANGER:
                return hover ? Theme.CHILLI.darker() : Theme.CHILLI;
            case OUTLINE:
                return hover ? Theme.PAPER : Color.WHITE;
            default:
                return hover ? Theme.PAPER_DARK : new Color(0, 0, 0, 0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = isEnabled() ? fillColor() : Theme.PAPER_DARK;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        if (style == Style.OUTLINE) {
            g2.setColor(Theme.LINE);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
