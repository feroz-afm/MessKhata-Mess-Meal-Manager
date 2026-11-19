package com.messkhata.ui.theme;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Colours, fonts and small helpers shared by every screen.
 * Palette idea: an old ledger book (khata) - cream paper, navy ink, turmeric highlight.
 */
public final class Theme {

    public static final Color INK = new Color(0x22314A);
    public static final Color INK_SOFT = new Color(0x2E4062);
    public static final Color INK_TEXT = new Color(0xC9D1DE);
    public static final Color PAPER = new Color(0xF6F1E7);
    public static final Color PAPER_DARK = new Color(0xEDE5D6);
    public static final Color CARD = Color.WHITE;
    public static final Color LINE = new Color(0xE4DCCB);
    public static final Color TURMERIC = new Color(0xE3A03A);
    public static final Color TURMERIC_DARK = new Color(0xC7831D);
    public static final Color TURMERIC_PALE = new Color(0xFBEBCF);
    public static final Color LEAF = new Color(0x3C8A5F);
    public static final Color LEAF_PALE = new Color(0xE2F1E7);
    public static final Color CHILLI = new Color(0xBF4B37);
    public static final Color CHILLI_PALE = new Color(0xF8E1DB);
    public static final Color TEXT = new Color(0x2A2824);
    public static final Color MUTED = new Color(0x837B6D);

    public static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    public static final DecimalFormat MONEY_SHORT = new DecimalFormat("#,##0");
    public static final DecimalFormat MEALS = new DecimalFormat("0.#");

    private static String family = "SansSerif";

    private Theme() {
    }

    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // the default look and feel is fine too
        }
        List<String> fonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames());
        for (String f : new String[]{"Segoe UI", "Noto Sans", "DejaVu Sans"}) {
            if (fonts.contains(f)) {
                family = f;
                break;
            }
        }
        UIManager.put("OptionPane.messageFont", font(Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", font(Font.PLAIN, 13));
        UIManager.put("ToolTip.font", font(Font.PLAIN, 12));
    }

    public static Font font(int style, float size) {
        return new Font(family, style, Math.round(size));
    }

    public static String tk(double amount) {
        return "Tk " + MONEY.format(amount);
    }

    public static String tkShort(double amount) {
        return "Tk " + MONEY_SHORT.format(amount);
    }

    // ------------------------------------------------------------- labels

    public static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    public static JLabel fieldLabel(String text) {
        return label(text.toUpperCase(), Font.BOLD, 11, MUTED);
    }

    // ------------------------------------------------------------- inputs

    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10));
    }

    public static JTextField textField() {
        JTextField f = new JTextField();
        styleInput(f);
        return f;
    }

    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        styleInput(f);
        return f;
    }

    private static void styleInput(JTextField f) {
        f.setFont(font(Font.PLAIN, 14));
        f.setForeground(TEXT);
        f.setBackground(Color.WHITE);
        f.setCaretColor(INK);
        f.setBorder(fieldBorder());
        f.setPreferredSize(new Dimension(200, 38));
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TURMERIC, 1, true),
                        BorderFactory.createEmptyBorder(7, 10, 7, 10)));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(fieldBorder());
            }
        });
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(font(Font.PLAIN, 14));
        c.setBackground(Color.WHITE);
        c.setForeground(TEXT);
        c.setPreferredSize(new Dimension(200, 38));
    }

    public static void styleSpinner(JSpinner s) {
        s.setFont(font(Font.PLAIN, 14));
        s.setPreferredSize(new Dimension(90, 38));
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setFont(font(Font.PLAIN, 14));
            tf.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    // ------------------------------------------------------------- tables

    public static void styleTable(JTable table) {
        table.setFont(font(Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(PAPER_DARK);
        table.setSelectionBackground(TURMERIC_PALE);
        table.setSelectionForeground(TEXT);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new PaddedRenderer());
        table.setDefaultRenderer(Double.class, new PaddedRenderer());
        table.setDefaultRenderer(Integer.class, new PaddedRenderer());
        styleHeader(table.getTableHeader());
    }

    public static void styleHeader(JTableHeader header) {
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
                l.setFont(font(Font.BOLD, 11));
                l.setForeground(MUTED);
                l.setBackground(PAPER);
                l.setText(v == null ? "" : v.toString().toUpperCase());
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                return l;
            }
        });
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(LINE));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    /** Default cell renderer with padding and right-aligned numbers. */
    public static class PaddedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
            l.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            l.setFont(t.getFont());
            if (!sel) {
                l.setBackground(Color.WHITE);
                l.setForeground(TEXT);
            }
            l.setHorizontalAlignment(v instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            if (v instanceof Double) {
                l.setText(MONEY.format(v));
            }
            return l;
        }
    }
}
