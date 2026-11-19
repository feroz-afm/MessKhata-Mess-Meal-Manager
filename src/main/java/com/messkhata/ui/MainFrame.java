package com.messkhata.ui;

import com.messkhata.model.Manager;
import com.messkhata.service.MessService;
import com.messkhata.ui.components.FlatButton;
import com.messkhata.ui.components.LogoMark;
import com.messkhata.ui.components.RoundedPanel;
import com.messkhata.ui.components.SidebarButton;
import com.messkhata.ui.panels.BasePanel;
import com.messkhata.ui.panels.DashboardPanel;
import com.messkhata.ui.panels.DepositPanel;
import com.messkhata.ui.panels.ExpensePanel;
import com.messkhata.ui.panels.MealChartPanel;
import com.messkhata.ui.panels.MembersPanel;
import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Main window: sidebar on the left, the selected page on the right.
 */
public class MainFrame extends JFrame {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final MessService service;
    private final Manager manager;
    private YearMonth month = YearMonth.now();

    private final CardLayout cards = new CardLayout();
    private final JPanel pageHolder = new JPanel(cards);
    private final List<BasePanel> pages = new ArrayList<>();
    private final List<SidebarButton> menu = new ArrayList<>();
    private final JLabel pageTitle = Theme.label("", Font.BOLD, 24, Theme.TEXT);
    private final JLabel pageSubtitle = Theme.label("", Font.PLAIN, 13, Theme.MUTED);
    private final JLabel monthLabel = Theme.label("", Font.BOLD, 14, Theme.TEXT);
    private int current = 0;

    public MainFrame(MessService service, Manager manager) {
        this.service = service;
        this.manager = manager;
        setTitle("MessKhata - Mess Meal Manager");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 660));
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        pageHolder.setOpaque(false);
        addPage("Dashboard", "DB", new DashboardPanel(this));
        addPage("Members", "MB", new MembersPanel(this));
        addPage("Meal chart", "ML", new MealChartPanel(this));
        addPage("Bazar & bills", "BZ", new ExpensePanel(this));
        addPage("Deposits", "DP", new DepositPanel(this));

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);
        setContentPane(root);

        updateMonthLabel();
        showPage(0);
    }

    public MessService getService() {
        return service;
    }

    public YearMonth getMonth() {
        return month;
    }

    /** Called by a page after it changes data, so the other pages show fresh numbers. */
    public void dataChanged() {
        for (BasePanel p : pages) {
            p.refresh();
        }
    }

    private void addPage(String title, String code, BasePanel page) {
        pages.add(page);
        pageHolder.add(page, title);
        SidebarButton b = new SidebarButton(title, code);
        int index = menu.size();
        b.addActionListener(e -> showPage(index));
        menu.add(b);
    }

    private void showPage(int index) {
        current = index;
        for (int i = 0; i < menu.size(); i++) {
            menu.get(i).setSelectedItem(i == index);
        }
        BasePanel p = pages.get(index);
        pageTitle.setText(menu.get(index).getText());
        pageSubtitle.setText(p.getSubtitle());
        p.refresh();
        cards.show(pageHolder, menu.get(index).getText());
    }

    private void changeMonth(YearMonth newMonth) {
        month = newMonth;
        updateMonthLabel();
        dataChanged();
        pageSubtitle.setText(pages.get(current).getSubtitle());
    }

    private void updateMonthLabel() {
        monthLabel.setText(month.format(MONTH_FMT));
    }

    // ---------------------------------------------------------------- layout

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(Theme.INK);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(236, 0));
        side.setBorder(BorderFactory.createEmptyBorder(26, 0, 22, 0));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        brand.add(new LogoMark(38));
        brand.add(Box.createHorizontalStrut(12));
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        names.add(Theme.label("MessKhata", Font.BOLD, 18, Color.WHITE));
        names.add(Theme.label("Mess meal manager", Font.PLAIN, 11, Theme.INK_TEXT));
        brand.add(names);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(brand);
        side.add(Box.createVerticalStrut(34));

        JLabel menuLabel = Theme.label("MENU", Font.BOLD, 11, new Color(0x7F8BA1));
        menuLabel.setBorder(BorderFactory.createEmptyBorder(0, 28, 8, 0));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(menuLabel);
        for (SidebarButton b : menu) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            side.add(b);
            side.add(Box.createVerticalStrut(2));
        }
        side.add(Box.createVerticalGlue());

        RoundedPanel who = new RoundedPanel(new BorderLayout(10, 10), 12, Theme.INK_SOFT, null);
        who.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel avatar = new JLabel(initials(manager.getName()), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.LEAF);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(Theme.font(Font.BOLD, 12));
        avatar.setForeground(Color.WHITE);
        avatar.setPreferredSize(new Dimension(34, 34));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Theme.label(manager.getName(), Font.BOLD, 13, Color.WHITE));
        text.add(Theme.label(manager.getRole(), Font.PLAIN, 11, Theme.INK_TEXT));
        FlatButton logout = new FlatButton("Log out", FlatButton.Style.PRIMARY);
        logout.setFont(Theme.font(Font.BOLD, 12));
        logout.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        logout.addActionListener(e -> logout());
        who.add(avatar, BorderLayout.WEST);
        who.add(text, BorderLayout.CENTER);
        who.add(logout, BorderLayout.SOUTH);

        JPanel whoWrap = new JPanel(new BorderLayout());
        whoWrap.setOpaque(false);
        whoWrap.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        whoWrap.add(who);
        whoWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        whoWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(whoWrap);
        return side;
    }

    private static String initials(String name) {
        StringBuilder sb = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (!part.isEmpty() && sb.length() < 2) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return sb.toString();
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.PAPER);
        content.setBorder(BorderFactory.createEmptyBorder(24, 30, 26, 30));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(pageTitle);
        titles.add(Box.createVerticalStrut(2));
        titles.add(pageSubtitle);
        top.add(titles, BorderLayout.WEST);
        top.add(buildMonthSwitcher(), BorderLayout.EAST);

        content.add(top, BorderLayout.NORTH);
        content.add(pageHolder, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildMonthSwitcher() {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        wrap.setOpaque(false);

        FlatButton thisMonth = new FlatButton("This month", FlatButton.Style.GHOST);
        thisMonth.addActionListener(e -> changeMonth(YearMonth.now()));

        RoundedPanel box = new RoundedPanel(new BorderLayout(), 12, Theme.CARD, Theme.LINE);
        box.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        FlatButton prev = new FlatButton("<", FlatButton.Style.GHOST);
        FlatButton next = new FlatButton(">", FlatButton.Style.GHOST);
        prev.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        next.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        prev.setToolTipText("Previous month");
        next.setToolTipText("Next month");
        prev.addActionListener(e -> changeMonth(month.minusMonths(1)));
        next.addActionListener(e -> changeMonth(month.plusMonths(1)));
        monthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        monthLabel.setPreferredSize(new Dimension(140, 30));
        box.add(prev, BorderLayout.WEST);
        box.add(monthLabel, BorderLayout.CENTER);
        box.add(next, BorderLayout.EAST);

        wrap.add(thisMonth);
        wrap.add(box);
        return wrap;
    }

    private void logout() {
        int ok = JOptionPane.showConfirmDialog(this, "Log out of MessKhata?", "Log out",
                JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            new LoginFrame(service).setVisible(true);
            dispose();
        }
    }
}
