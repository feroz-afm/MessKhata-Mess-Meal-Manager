package com.messkhata.ui.panels;

import com.messkhata.model.Expense;
import com.messkhata.model.ExpenseCategory;
import com.messkhata.service.MessException;
import com.messkhata.ui.MainFrame;
import com.messkhata.ui.components.FlatButton;
import com.messkhata.ui.components.Pill;
import com.messkhata.ui.components.RoundedPanel;
import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Daily bazar and shared bills (cook, gas, internet...).
 */
public class ExpensePanel extends BasePanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final JSpinner day = daySpinner();
    private final JComboBox<ExpenseCategory> category = new JComboBox<>(ExpenseCategory.values());
    private final JComboBox<Choice> paidBy = new JComboBox<>();
    private final JTextField details = Theme.textField();
    private final JTextField amount = Theme.textField();
    private final JCheckBox ownPocket = new JCheckBox("<html>Paid from own pocket<br>"
            + "<span style='color:#837B6D'>also adds this amount to the member's deposit</span></html>");
    private final JLabel bazarTotal = Theme.label(" ", Font.BOLD, 13, Theme.TURMERIC_DARK);
    private final JLabel sharedTotal = Theme.label(" ", Font.BOLD, 13, Theme.INK);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Date", "Type", "Paid by", "Details", "Amount"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 5 ? Double.class : String.class;
        }
    };
    private final JTable table = new JTable(model);

    public ExpensePanel(MainFrame app) {
        super(app);
        setLayout(new GridBagLayout());
        RoundedPanel form = buildForm();
        form.setPreferredSize(new Dimension(320, 400));
        GridBagConstraints fc = gbc(0, 0, 0, 1);
        fc.insets = new java.awt.Insets(0, 0, 0, 16);
        add(form, fc);
        GridBagConstraints tc = gbc(1, 0, 1, 1);
        tc.insets = new java.awt.Insets(0, 0, 0, 0);
        add(buildTable(), tc);
    }

    @Override
    public String getSubtitle() {
        return "Bazar is divided by meals, shared bills are divided equally";
    }

    private RoundedPanel buildForm() {
        RoundedPanel card = card("Add an expense");
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);

        Theme.styleCombo(category);
        Theme.styleCombo(paidBy);
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(day, BorderLayout.WEST);
        row.add(category, BorderLayout.CENTER);

        int r = addField(fields, 0, "Day  /  Type", row);
        r = addField(fields, r, "Paid by", paidBy);
        r = addField(fields, r, "What was bought / which bill", details);
        r = addField(fields, r, "Amount (Tk)", amount);

        ownPocket.setOpaque(false);
        ownPocket.setFont(Theme.font(Font.PLAIN, 12));
        ownPocket.setForeground(Theme.TEXT);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = r;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new java.awt.Insets(12, 0, 0, 0);
        fields.add(ownPocket, c);
        c.gridy = r + 1;
        c.weighty = 1;
        fields.add(new JLabel(), c);
        card.add(fields, BorderLayout.CENTER);

        paidBy.addActionListener(e -> updateOwnPocket());

        FlatButton add = new FlatButton("Add expense", FlatButton.Style.PRIMARY);
        add.addActionListener(e -> addExpense());
        amount.addActionListener(e -> addExpense());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        buttons.add(add);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private void updateOwnPocket() {
        Choice c = (Choice) paidBy.getSelectedItem();
        boolean member = c != null && !c.id.isEmpty();
        ownPocket.setEnabled(member);
        if (!member) {
            ownPocket.setSelected(false);
        }
    }

    private RoundedPanel buildTable() {
        RoundedPanel card = card(null);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(Theme.label("This month's expenses", Font.BOLD, 16, Theme.TEXT));
        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        totals.setOpaque(false);
        totals.add(bazarTotal);
        totals.add(javax.swing.Box.createHorizontalStrut(18));
        totals.add(sharedTotal);
        left.add(totals);
        head.add(left, BorderLayout.WEST);
        FlatButton delete = new FlatButton("Delete selected", FlatButton.Style.OUTLINE);
        delete.addActionListener(e -> deleteSelected());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        right.setOpaque(false);
        right.add(delete);
        head.add(right, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(2).setCellRenderer(new TypeRenderer());
        // the ID stays in the model (used for delete) but is not shown
        table.removeColumn(table.getColumnModel().getColumn(0));
        int[] widths = {70, 110, 140, 190, 95};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        card.add(Theme.scroll(table), BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refresh() {
        resetDay(day);
        fillMembers(paidBy, true, true);
        updateOwnPocket();
        model.setRowCount(0);
        double bazar = 0;
        double shared = 0;
        for (Expense e : service().getExpenses(month())) {
            model.addRow(new Object[]{e.getId(), e.getDate().format(DATE_FMT), e.getType(),
                e.getMemberId().isEmpty() ? "Mess fund" : service().memberName(e.getMemberId()),
                e.getDescription(), e.getAmount()});
            if (e.getCategory() == ExpenseCategory.BAZAR) {
                bazar += e.getAmount();
            } else {
                shared += e.getAmount();
            }
        }
        bazarTotal.setText("Bazar  " + Theme.tk(bazar));
        sharedTotal.setText("Shared bills  " + Theme.tk(shared));
    }

    private void addExpense() {
        try {
            Choice who = (Choice) paidBy.getSelectedItem();
            Expense e = service().addExpense(dateOf(day), who == null ? "" : who.id,
                    (ExpenseCategory) category.getSelectedItem(), details.getText(),
                    parseAmount(amount.getText()), ownPocket.isSelected());
            details.setText("");
            amount.setText("");
            ownPocket.setSelected(false);
            app.dataChanged();
            showInfo(e.getType() + " of " + Theme.tk(e.getAmount()) + " saved.");
        } catch (MessException ex) {
            showError(ex);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError(new MessException("Select an expense in the table first."));
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        if (!confirm("Delete expense " + id + "?")) {
            return;
        }
        try {
            service().deleteExpense(id);
            app.dataChanged();
        } catch (MessException e) {
            showError(e);
        }
    }

    private static class TypeRenderer implements TableCellRenderer {
        private final Pill pill = new Pill();
        private final JPanel cell = new JPanel(new BorderLayout());

        TypeRenderer() {
            cell.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
            pill.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            cell.add(pill);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            boolean bazar = "Bazar".equals(v);
            pill.set(String.valueOf(v), bazar ? Theme.TURMERIC_PALE : new Color(0xE3E7EF),
                    bazar ? Theme.TURMERIC_DARK : Theme.INK);
            cell.setBackground(sel ? Theme.TURMERIC_PALE : Color.WHITE);
            return cell;
        }
    }
}
