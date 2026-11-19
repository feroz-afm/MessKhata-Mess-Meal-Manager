package com.messkhata.ui.panels;

import com.messkhata.model.MemberSummary;
import com.messkhata.model.MonthSummary;
import com.messkhata.service.MessException;
import com.messkhata.ui.MainFrame;
import com.messkhata.ui.components.FlatButton;
import com.messkhata.ui.components.Pill;
import com.messkhata.ui.components.RoundedPanel;
import com.messkhata.ui.components.StatCard;
import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.nio.file.Path;

/**
 * Month-end settlement: who gets money back and who has to pay.
 */
public class ReportPanel extends BasePanel {

    private final StatCard meals = new StatCard("Total meals", Theme.INK);
    private final StatCard rate = new StatCard("Meal rate", Theme.TURMERIC);
    private final StatCard shared = new StatCard("Shared bill each", Theme.CHILLI);
    private final StatCard cash = new StatCard("Cash in hand", Theme.LEAF);

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
        "Member", "Meals", "Meal cost", "Shared", "Total cost", "Deposit", "Balance", "Settlement"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c >= 1 && c <= 6 ? Double.class : String.class;
        }
    };
    private final JTable table = new JTable(model);

    public ReportPanel(MainFrame app) {
        super(app);
        setLayout(new BorderLayout(0, 16));

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(meals);
        stats.add(rate);
        stats.add(shared);
        stats.add(cash);
        add(stats, BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
    }

    @Override
    public String getSubtitle() {
        return "Month-end settlement for every member";
    }

    private RoundedPanel buildTable() {
        RoundedPanel card = card(null);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(Theme.label("Settlement", Font.BOLD, 16, Theme.TEXT));
        left.add(Theme.label("Meal cost = meals x meal rate     Balance = deposit - (meal cost + shared bill)",
                Font.PLAIN, 12, Theme.MUTED));
        head.add(left, BorderLayout.WEST);
        FlatButton export = new FlatButton("Export report (.txt)", FlatButton.Style.DARK);
        export.addActionListener(e -> export());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        right.setOpaque(false);
        right.add(export);
        head.add(right, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setRowHeight(40);
        table.setRowSelectionAllowed(false);
        int[] widths = {160, 64, 100, 90, 100, 100, 105, 160};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(1).setCellRenderer(new MealsRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new BalanceRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new SettlementRenderer());
        card.add(Theme.scroll(table), BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refresh() {
        MonthSummary s = service().getSummary(month());
        meals.setValue(Theme.MEALS.format(s.getTotalMeals()));
        meals.setNote(s.getMembers().size() + " members this month");
        rate.setValue(Theme.tk(s.getMealRate()));
        rate.setNote(Theme.tkShort(s.getTotalBazar()) + " bazar / " + Theme.MEALS.format(s.getTotalMeals()) + " meals");
        shared.setValue(Theme.tk(s.getSharedPerHead()));
        shared.setNote(Theme.tkShort(s.getTotalShared()) + " split equally");
        cash.setValue(Theme.tkShort(s.getCashInHand()));
        cash.setValueColor(s.getCashInHand() >= 0 ? Theme.LEAF : Theme.CHILLI);
        cash.setNote(Theme.tkShort(s.getTotalDeposit()) + " received in total");

        model.setRowCount(0);
        for (MemberSummary m : s.getMembers()) {
            String settle;
            if ("Settled".equals(m.getStatus())) {
                settle = "Settled";
            } else {
                settle = m.getStatus() + "  " + Theme.tkShort(Math.abs(m.getBalance()));
            }
            model.addRow(new Object[]{m.getMember().getName(), m.getMeals(), m.getMealCost(), m.getSharedCost(),
                m.getTotalCost(), m.getDeposit(), m.getBalance(), settle});
        }
    }

    private void export() {
        try {
            Path file = service().exportReport(month());
            showInfo("Report saved to:\n" + file);
        } catch (MessException e) {
            showError(e);
        }
    }

    private static class MealsRenderer extends Theme.PaddedRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            l.setText(Theme.MEALS.format(v));
            return l;
        }
    }

    private static class BalanceRenderer extends Theme.PaddedRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            double b = (Double) v;
            l.setText((b > 0 ? "+" : "") + Theme.MONEY.format(b));
            l.setFont(Theme.font(Font.BOLD, 13));
            l.setForeground(Math.abs(b) < 0.5 ? Theme.MUTED : b > 0 ? Theme.LEAF : Theme.CHILLI);
            return l;
        }
    }

    private static class SettlementRenderer implements TableCellRenderer {
        private final Pill pill = new Pill();
        private final JPanel cell = new JPanel(new BorderLayout());

        SettlementRenderer() {
            cell.setBackground(Color.WHITE);
            cell.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 10));
            pill.setHorizontalAlignment(SwingConstants.LEFT);
            cell.add(pill);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            String text = String.valueOf(v);
            if (text.startsWith("Will get")) {
                pill.set(text, Theme.LEAF_PALE, Theme.LEAF);
            } else if (text.startsWith("Will pay")) {
                pill.set(text, Theme.CHILLI_PALE, Theme.CHILLI);
            } else {
                pill.set(text, Theme.PAPER_DARK, Theme.MUTED);
            }
            return cell;
        }
    }
}
