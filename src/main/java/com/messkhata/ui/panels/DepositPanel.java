package com.messkhata.ui.panels;

import com.messkhata.model.Deposit;
import com.messkhata.model.MemberSummary;
import com.messkhata.service.MessException;
import com.messkhata.ui.MainFrame;
import com.messkhata.ui.components.FlatButton;
import com.messkhata.ui.components.RoundedPanel;
import com.messkhata.ui.theme.Theme;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Money members give to the manager.
 */
public class DepositPanel extends BasePanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final JSpinner day = daySpinner();
    private final JComboBox<Choice> member = new JComboBox<>();
    private final JTextField amount = Theme.textField();
    private final JTextField note = Theme.textField();
    private final JLabel total = Theme.label(" ", Font.BOLD, 13, Theme.LEAF);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Date", "Member", "Note", "Amount"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 4 ? Double.class : String.class;
        }
    };
    private final JTable table = new JTable(model);

    private final DefaultTableModel byMemberModel = new DefaultTableModel(new Object[]{"Member", "Given (Tk)"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == 1 ? Double.class : String.class;
        }
    };
    private final JTable byMember = new JTable(byMemberModel);

    public DepositPanel(MainFrame app) {
        super(app);
        setLayout(new GridBagLayout());

        JPanel leftCol = new JPanel(new BorderLayout(0, 16));
        leftCol.setOpaque(false);
        leftCol.add(buildForm(), BorderLayout.NORTH);
        leftCol.add(buildByMember(), BorderLayout.CENTER);
        leftCol.setPreferredSize(new Dimension(320, 400));
        GridBagConstraints fc = gbc(0, 0, 0, 1);
        fc.insets = new java.awt.Insets(0, 0, 0, 16);
        add(leftCol, fc);

        GridBagConstraints tc = gbc(1, 0, 1, 1);
        tc.insets = new java.awt.Insets(0, 0, 0, 0);
        add(buildTable(), tc);
    }

    @Override
    public String getSubtitle() {
        return "Advance money members hand over to the manager";
    }

    private RoundedPanel buildForm() {
        RoundedPanel card = card("Add a deposit");
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        Theme.styleCombo(member);
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(day, BorderLayout.WEST);
        row.add(member, BorderLayout.CENTER);
        int r = addField(fields, 0, "Day  /  Member", row);
        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 0));
        row2.setOpaque(false);
        row2.add(amount);
        row2.add(note);
        r = addField(fields, r, "Amount (Tk)  /  Note", row2);
        card.add(fields, BorderLayout.CENTER);

        FlatButton add = new FlatButton("Add deposit", FlatButton.Style.PRIMARY);
        add.addActionListener(e -> addDeposit());
        amount.addActionListener(e -> addDeposit());
        note.addActionListener(e -> addDeposit());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        buttons.add(add);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private RoundedPanel buildByMember() {
        RoundedPanel card = card("Given by each member");
        Theme.styleTable(byMember);
        byMember.setRowHeight(30);
        byMember.setRowSelectionAllowed(false);
        card.add(Theme.scroll(byMember), BorderLayout.CENTER);
        return card;
    }

    private RoundedPanel buildTable() {
        RoundedPanel card = card(null);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(Theme.label("All deposits this month", Font.BOLD, 16, Theme.TEXT));
        left.add(total);
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
        table.removeColumn(table.getColumnModel().getColumn(0));
        int[] widths = {70, 150, 240, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        card.add(Theme.scroll(table), BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refresh() {
        resetDay(day);
        fillMembers(member, false, false);
        model.setRowCount(0);
        double sum = 0;
        for (Deposit d : service().getDeposits(month())) {
            model.addRow(new Object[]{d.getId(), d.getDate().format(DATE_FMT),
                service().memberName(d.getMemberId()), d.getDescription(), d.getAmount()});
            sum += d.getAmount();
        }
        total.setText("Total received  " + Theme.tk(sum));

        byMemberModel.setRowCount(0);
        for (MemberSummary s : service().getSummary(month()).getMembers()) {
            byMemberModel.addRow(new Object[]{s.getMember().getName(), s.getDeposit()});
        }
    }

    private void addDeposit() {
        try {
            Choice who = (Choice) member.getSelectedItem();
            if (who == null) {
                throw new MessException("Add a member first.");
            }
            Deposit d = service().addDeposit(dateOf(day), who.id, parseAmount(amount.getText()), note.getText());
            amount.setText("");
            note.setText("");
            app.dataChanged();
            showInfo(Theme.tk(d.getAmount()) + " received from " + service().memberName(d.getMemberId()) + ".");
        } catch (MessException ex) {
            showError(ex);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError(new MessException("Select a deposit in the table first."));
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        if (!confirm("Delete deposit " + id + "?")) {
            return;
        }
        try {
            service().deleteDeposit(id);
            app.dataChanged();
        } catch (MessException e) {
            showError(e);
        }
    }
}
