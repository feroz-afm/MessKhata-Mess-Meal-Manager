package com.messkhata.ui.panels;

import com.messkhata.model.MealSheet;
import com.messkhata.model.Member;
import com.messkhata.service.MessException;
import com.messkhata.ui.MainFrame;
import com.messkhata.ui.components.FlatButton;
import com.messkhata.ui.components.RoundedPanel;
import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * The meal chart: one row per member, one column per day, like the paper chart on a mess wall.
 */
public class MealChartPanel extends BasePanel {

    private static final Color TODAY_BG = new Color(0xFDF3E1);
    private static final Color FRIDAY_BG = new Color(0xFAF7F1);

    private final MealModel mealModel = new MealModel();
    private final NameModel nameModel = new NameModel();
    private final JTable table = new JTable(mealModel);
    private final JTable names = new JTable(nameModel);
    private final JSpinner day = daySpinner();
    private final JComboBox<String> count = new JComboBox<>(new String[]{"0", "1", "1.5", "2", "2.5", "3"});
    private final JLabel status = Theme.label(" ", Font.PLAIN, 12, Theme.MUTED);

    private List<Member> members = new ArrayList<>();
    private MealSheet sheet;

    public MealChartPanel(MainFrame app) {
        super(app);
        setLayout(new BorderLayout(0, 16));
        add(buildQuickFill(), BorderLayout.NORTH);
        add(buildChart(), BorderLayout.CENTER);
    }

    @Override
    public String getSubtitle() {
        return "Double-click a cell to change meals. Half meals (0.5) are allowed.";
    }

    private RoundedPanel buildQuickFill() {
        RoundedPanel card = new RoundedPanel(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(Theme.label("Quick fill", Font.BOLD, 15, Theme.TEXT));
        left.add(javax.swing.Box.createHorizontalStrut(10));
        left.add(Theme.fieldLabel("Day"));
        left.add(day);
        left.add(Theme.fieldLabel("Meals each"));
        Theme.styleCombo(count);
        count.setSelectedItem("2");
        count.setPreferredSize(new Dimension(80, 38));
        left.add(count);
        FlatButton apply = new FlatButton("Set for all active members", FlatButton.Style.PRIMARY);
        apply.addActionListener(e -> quickFill());
        left.add(apply);
        card.add(left, BorderLayout.WEST);
        status.setHorizontalAlignment(SwingConstants.RIGHT);
        card.add(status, BorderLayout.EAST);
        return card;
    }

    private RoundedPanel buildChart() {
        RoundedPanel card = card(null);

        Theme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSurrendersFocusOnKeystroke(true);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setDefaultRenderer(Double.class, new MealRenderer());
        table.getTableHeader().setDefaultRenderer(new DayHeaderRenderer());
        table.getTableHeader().setPreferredSize(new Dimension(0, 46));

        Theme.styleTable(names);
        names.setSelectionModel(table.getSelectionModel());
        names.setDefaultRenderer(Object.class, new NameRenderer());
        names.setFocusable(false);
        names.getTableHeader().setPreferredSize(new Dimension(0, 46));

        JScrollPane sp = Theme.scroll(table);
        sp.setRowHeaderView(names);
        sp.getRowHeader().setBackground(Color.WHITE);
        sp.setCorner(JScrollPane.UPPER_LEFT_CORNER, names.getTableHeader());
        card.add(sp, BorderLayout.CENTER);

        JLabel legend = Theme.label("Turmeric column = today      Shaded columns = Friday      "
                + "Last row and column show totals", Font.PLAIN, 12, Theme.MUTED);
        card.add(legend, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refresh() {
        YearMonth m = month();
        members = service().getMealChartMembers(m);
        sheet = service().getMealSheet(m);
        resetDay(day);
        mealModel.fireTableStructureChanged();
        nameModel.fireTableDataChanged();
        for (int c = 0; c < table.getColumnCount(); c++) {
            TableColumn col = table.getColumnModel().getColumn(c);
            col.setPreferredWidth(c < sheet.getDays() ? 46 : 78);
        }
        names.getColumnModel().getColumn(0).setPreferredWidth(190);
        names.setPreferredScrollableViewportSize(new Dimension(190, 0));
        status.setText(Theme.MEALS.format(sheet.getGrandTotal()) + " meals so far");
        scrollToToday();
    }

    private void scrollToToday() {
        if (YearMonth.now().equals(month())) {
            int col = LocalDate.now().getDayOfMonth() - 1;
            table.scrollRectToVisible(table.getCellRect(0, Math.min(col + 4, table.getColumnCount() - 1), true));
        }
    }

    private void quickFill() {
        try {
            int d = (Integer) day.getValue();
            double value = Double.parseDouble((String) count.getSelectedItem());
            int n = service().setMealForAll(month(), d, value);
            refresh();
            status.setText("Day " + d + ": " + Theme.MEALS.format(value) + " meals set for " + n + " members");
            app.dataChanged();
        } catch (MessException e) {
            showError(e);
        }
    }

    private boolean isFriday(int dayOfMonth) {
        return month().atDay(dayOfMonth).getDayOfWeek() == DayOfWeek.FRIDAY;
    }

    private boolean isToday(int dayOfMonth) {
        return YearMonth.now().equals(month()) && LocalDate.now().getDayOfMonth() == dayOfMonth;
    }

    // ---------------------------------------------------------------- models

    private class MealModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return sheet == null ? 0 : members.size() + 1;
        }

        @Override
        public int getColumnCount() {
            return sheet == null ? 0 : sheet.getDays() + 1;
        }

        @Override
        public String getColumnName(int c) {
            return c < sheet.getDays() ? String.valueOf(c + 1) : "Total";
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return Double.class;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return r < members.size() && c < sheet.getDays();
        }

        @Override
        public Object getValueAt(int r, int c) {
            boolean totalRow = r == members.size();
            boolean totalCol = c == sheet.getDays();
            if (totalRow) {
                return totalCol ? sheet.getGrandTotal() : sheet.getDayTotal(c + 1);
            }
            String id = members.get(r).getId();
            return totalCol ? sheet.getMemberTotal(id) : sheet.getMeal(id, c + 1);
        }

        @Override
        public void setValueAt(Object value, int r, int c) {
            if (value == null) {
                return;
            }
            try {
                service().setMeal(month(), members.get(r).getId(), c + 1, ((Number) value).doubleValue());
                fireTableRowsUpdated(r, r);
                fireTableRowsUpdated(members.size(), members.size());
                status.setText(Theme.MEALS.format(sheet.getGrandTotal()) + " meals so far");
                app.dataChanged();
            } catch (MessException e) {
                showError(e);
            }
        }
    }

    private class NameModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return mealModel.getRowCount();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int c) {
            return "Member";
        }

        @Override
        public Object getValueAt(int r, int c) {
            return r == members.size() ? "Day total" : members.get(r).getName();
        }
    }

    // ------------------------------------------------------------- renderers

    private class MealRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, r, c);
            double value = v == null ? 0 : (Double) v;
            boolean totalRow = r == members.size();
            boolean totalCol = c == sheet.getDays();
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBorder(null);
            l.setFont(Theme.font(totalRow || totalCol ? Font.BOLD : Font.PLAIN, 13));
            l.setText(value == 0 && !totalCol ? "." : Theme.MEALS.format(value));
            l.setForeground(value == 0 ? new Color(0xC9C0AE) : Theme.TEXT);

            Color bg = Color.WHITE;
            if (totalRow || totalCol) {
                bg = Theme.PAPER;
            } else if (isToday(c + 1)) {
                bg = TODAY_BG;
            } else if (isFriday(c + 1)) {
                bg = FRIDAY_BG;
            }
            if (sel && !totalRow && !totalCol) {
                bg = Theme.TURMERIC_PALE;
            }
            l.setBackground(bg);
            return l;
        }
    }

    private class NameRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
            boolean totalRow = r == members.size();
            l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.LINE),
                    BorderFactory.createEmptyBorder(0, 14, 0, 10)));
            l.setFont(Theme.font(Font.BOLD, 13));
            l.setForeground(totalRow ? Theme.MUTED : Theme.TEXT);
            l.setBackground(totalRow ? Theme.PAPER : Color.WHITE);
            return l;
        }
    }

    private class DayHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, r, c);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.LINE));
            l.setOpaque(true);
            if (c >= sheet.getDays()) {
                l.setText("<html><center><b>TOTAL</b></center></html>");
                l.setFont(Theme.font(Font.BOLD, 11));
                l.setForeground(Theme.MUTED);
                l.setBackground(Theme.PAPER);
                return l;
            }
            int d = c + 1;
            String weekday = month().atDay(d).getDayOfWeek().name().substring(0, 2);
            weekday = weekday.charAt(0) + weekday.substring(1).toLowerCase();
            l.setText("<html><center><b>" + d + "</b><br><span style='font-size:8px'>" + weekday
                    + "</span></center></html>");
            l.setFont(Theme.font(Font.PLAIN, 12));
            if (isToday(d)) {
                l.setBackground(Theme.TURMERIC);
                l.setForeground(Theme.INK);
            } else {
                l.setBackground(isFriday(d) ? Theme.PAPER_DARK : Theme.PAPER);
                l.setForeground(isFriday(d) ? Theme.TURMERIC_DARK : Theme.MUTED);
            }
            return l;
        }
    }
}
