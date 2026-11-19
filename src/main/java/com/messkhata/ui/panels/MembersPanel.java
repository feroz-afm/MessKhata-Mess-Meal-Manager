package com.messkhata.ui.panels;

import com.messkhata.model.Member;
import com.messkhata.service.MessException;
import com.messkhata.ui.MainFrame;
import com.messkhata.ui.components.FlatButton;
import com.messkhata.ui.components.Pill;
import com.messkhata.ui.components.RoundedPanel;
import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
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
import java.util.regex.Pattern;

/**
 * Add, edit and remove mess members.
 */
public class MembersPanel extends BasePanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final JTextField name = Theme.textField();
    private final JTextField phone = Theme.textField();
    private final JTextField room = Theme.textField();
    private final JCheckBox active = new JCheckBox("Currently living in the mess");
    private final JLabel formTitle = Theme.label("Add a member", Font.BOLD, 16, Theme.TEXT);
    private final JLabel formNote = Theme.label("New members start as active.", Font.PLAIN, 12, Theme.MUTED);
    private final FlatButton save = new FlatButton("Add member", FlatButton.Style.PRIMARY);
    private final FlatButton delete = new FlatButton("Delete", FlatButton.Style.DANGER);
    private final JTextField search = Theme.textField();
    private final JLabel count = Theme.label(" ", Font.PLAIN, 12, Theme.MUTED);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Phone", "Room", "Joined", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private String editingId = null;

    public MembersPanel(MainFrame app) {
        super(app);
        setLayout(new GridBagLayout());

        RoundedPanel form = buildForm();
        form.setPreferredSize(new Dimension(330, 400));
        GridBagConstraints fc = gbc(0, 0, 0, 1);
        fc.insets = new java.awt.Insets(0, 0, 0, 16);
        add(form, fc);

        GridBagConstraints tc = gbc(1, 0, 1, 1);
        tc.insets = new java.awt.Insets(0, 0, 0, 0);
        add(buildTable(), tc);
    }

    @Override
    public String getSubtitle() {
        return "Everyone who eats in the mess";
    }

    private RoundedPanel buildForm() {
        RoundedPanel card = card(null);
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 2));
        head.setOpaque(false);
        head.add(formTitle);
        head.add(formNote);
        card.add(head, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        int r = addField(fields, 0, "Full name", name);
        r = addField(fields, r, "Phone (11 digits)", phone);
        r = addField(fields, r, "Room", room);
        active.setOpaque(false);
        active.setFont(Theme.font(Font.PLAIN, 13));
        active.setForeground(Theme.TEXT);
        active.setSelected(true);
        active.setVisible(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = r;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(12, 0, 0, 0);
        fields.add(active, c);
        c.gridy = r + 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new java.awt.Insets(22, 0, 0, 0);
        fields.add(tipBox(), c);
        card.add(fields, BorderLayout.CENTER);

        FlatButton clear = new FlatButton("Clear", FlatButton.Style.OUTLINE);
        save.addActionListener(e -> saveMember());
        clear.addActionListener(e -> clearForm());
        delete.addActionListener(e -> deleteMember());
        delete.setVisible(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        buttons.add(save);
        buttons.add(javax.swing.Box.createHorizontalStrut(8));
        buttons.add(clear);
        buttons.add(javax.swing.Box.createHorizontalStrut(8));
        buttons.add(delete);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JPanel tipBox() {
        RoundedPanel tip = new RoundedPanel(new BorderLayout(), 10, Theme.PAPER, null);
        tip.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        tip.add(Theme.label("<html><b>Tip</b><br>Click a member in the table to edit. "
                + "When someone leaves the mess, untick <i>Currently living</i> instead of deleting, "
                + "so old months stay correct.</html>", Font.PLAIN, 12, Theme.MUTED));
        return tip;
    }

    private RoundedPanel buildTable() {
        RoundedPanel card = card(null);
        JPanel head = new JPanel(new BorderLayout(12, 0));
        head.setOpaque(false);
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(Theme.label("All members", Font.BOLD, 16, Theme.TEXT));
        left.add(count);
        search.setPreferredSize(new Dimension(240, 38));
        search.setToolTipText("Search by name, phone or room");
        head.add(left, BorderLayout.WEST);
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        searchBox.add(Theme.label("Search", Font.BOLD, 11, Theme.MUTED), BorderLayout.WEST);
        searchBox.add(search, BorderLayout.CENTER);
        head.add(searchBox, BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = {64, 170, 120, 60, 110, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelected();
            }
        });
        card.add(Theme.scroll(table), BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        return card;
    }

    private void filter() {
        String text = search.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text), 1, 2, 3));
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        int activeCount = 0;
        for (Member m : service().getMembers()) {
            model.addRow(new Object[]{m.getId(), m.getName(), m.getPhone(), m.getRoom(),
                m.getJoinDate().format(DATE_FMT), m.isActive() ? "Active" : "Left"});
            if (m.isActive()) {
                activeCount++;
            }
        }
        count.setText(model.getRowCount() + " members, " + activeCount + " active");
    }

    private void loadSelected() {
        int view = table.getSelectedRow();
        if (view < 0) {
            return;
        }
        String id = (String) model.getValueAt(table.convertRowIndexToModel(view), 0);
        Member m = service().findMember(id);
        if (m == null) {
            return;
        }
        editingId = id;
        name.setText(m.getName());
        phone.setText(m.getPhone());
        room.setText(m.getRoom());
        active.setSelected(m.isActive());
        active.setVisible(true);
        delete.setVisible(true);
        save.setText("Save changes");
        formTitle.setText("Edit " + m.getName().split(" ")[0]);
        formNote.setText("Member ID " + m.getId());
    }

    private void clearForm() {
        editingId = null;
        name.setText("");
        phone.setText("");
        room.setText("");
        active.setSelected(true);
        active.setVisible(false);
        delete.setVisible(false);
        save.setText("Add member");
        formTitle.setText("Add a member");
        formNote.setText("New members start as active.");
        table.clearSelection();
        name.requestFocusInWindow();
    }

    private void saveMember() {
        try {
            if (editingId == null) {
                Member m = service().addMember(name.getText(), phone.getText(), room.getText());
                showInfo(m.getName() + " was added with ID " + m.getId() + ".");
            } else {
                service().updateMember(editingId, name.getText(), phone.getText(), room.getText(),
                        active.isSelected());
            }
            clearForm();
            app.dataChanged();
        } catch (MessException e) {
            showError(e);
        }
    }

    private void deleteMember() {
        if (editingId == null) {
            return;
        }
        Member m = service().findMember(editingId);
        if (m == null || !confirm("Delete " + m.getName() + "? This cannot be undone.")) {
            return;
        }
        try {
            service().deleteMember(editingId);
            clearForm();
            app.dataChanged();
        } catch (MessException e) {
            showError(e);
        }
    }

    /** Shows the status column as a coloured pill. */
    private static class StatusRenderer implements TableCellRenderer {
        private final Pill pill = new Pill();
        private final JPanel cell = new JPanel(new BorderLayout());

        StatusRenderer() {
            cell.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            pill.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            cell.add(pill);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
            boolean isActive = "Active".equals(v);
            pill.set(String.valueOf(v), isActive ? Theme.LEAF_PALE : Theme.PAPER_DARK,
                    isActive ? Theme.LEAF : Theme.MUTED);
            cell.setBackground(sel ? Theme.TURMERIC_PALE : Color.WHITE);
            return cell;
        }
    }
}
