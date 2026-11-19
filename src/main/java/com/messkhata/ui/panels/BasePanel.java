package com.messkhata.ui.panels;

import com.messkhata.model.Member;
import com.messkhata.service.MessException;
import com.messkhata.service.MessService;
import com.messkhata.ui.MainFrame;
import com.messkhata.ui.components.RoundedPanel;
import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Parent of every page. Gives each page access to the backend and the selected month.
 */
public abstract class BasePanel extends JPanel {

    protected final MainFrame app;

    protected BasePanel(MainFrame app) {
        this.app = app;
        setOpaque(false);
    }

    /** Re-reads data from the backend and redraws the page. */
    public abstract void refresh();

    /** Line shown under the page title. */
    public abstract String getSubtitle();

    protected MessService service() {
        return app.getService();
    }

    protected YearMonth month() {
        return app.getMonth();
    }

    // ------------------------------------------------------------- helpers

    protected RoundedPanel card(String title) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));
        if (title != null) {
            card.add(Theme.label(title, Font.BOLD, 16, Theme.TEXT), BorderLayout.NORTH);
        }
        return card;
    }

    protected static GridBagConstraints gbc(int x, int y, double wx, double wy) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.weightx = wx;
        c.weighty = wy;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 0, 16, 16);
        return c;
    }

    /** Adds a label and a field one below the other in a form that uses GridBagLayout. */
    protected static int addField(JPanel form, int row, String label, java.awt.Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(row == 0 ? 0 : 12, 0, 6, 0);
        JLabel l = Theme.fieldLabel(label);
        form.add(l, c);
        c.gridy = row + 1;
        c.insets = new Insets(0, 0, 0, 0);
        form.add(field, c);
        return row + 2;
    }

    protected void showError(MessException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Please check", JOptionPane.WARNING_MESSAGE);
    }

    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "MessKhata", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Day picker limited to the days of the selected month. Today is chosen if it is in that month. */
    protected JSpinner daySpinner() {
        JSpinner s = new JSpinner(new SpinnerNumberModel(1, 1, 31, 1));
        Theme.styleSpinner(s);
        resetDay(s);
        return s;
    }

    protected void resetDay(JSpinner s) {
        YearMonth m = month();
        int max = m.lengthOfMonth();
        int day = YearMonth.now().equals(m) ? LocalDate.now().getDayOfMonth() : 1;
        s.setModel(new SpinnerNumberModel(day, 1, max, 1));
        Theme.styleSpinner(s);
    }

    protected LocalDate dateOf(JSpinner s) {
        return month().atDay((Integer) s.getValue());
    }

    protected static double parseAmount(String text) throws MessException {
        try {
            return Double.parseDouble(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            throw new MessException("Amount must be a number, e.g. 850");
        }
    }

    /** Fills a combo box with members. If withFund is true, the first item is the mess fund. */
    protected void fillMembers(JComboBox<Choice> combo, boolean withFund, boolean activeOnly) {
        Object old = combo.getSelectedItem();
        combo.removeAllItems();
        if (withFund) {
            combo.addItem(new Choice("", "Mess fund (manager's cash)"));
        }
        for (Member m : activeOnly ? service().getActiveMembers() : service().getMembers()) {
            combo.addItem(new Choice(m.getId(), m.getName() + " (" + m.getRoom() + ")"));
        }
        if (old != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).id.equals(((Choice) old).id)) {
                    combo.setSelectedIndex(i);
                }
            }
        }
    }

    /** Item of a combo box: an id with a readable label. */
    protected static class Choice {
        final String id;
        final String label;

        Choice(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
