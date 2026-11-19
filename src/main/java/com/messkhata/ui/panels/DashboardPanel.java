package com.messkhata.ui.panels;

import com.messkhata.model.MealSheet;
import com.messkhata.model.MonthSummary;
import com.messkhata.model.Transaction;
import com.messkhata.ui.MainFrame;
import com.messkhata.ui.components.DailyMealChart;
import com.messkhata.ui.components.Pill;
import com.messkhata.ui.components.RoundedPanel;
import com.messkhata.ui.components.StatCard;
import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Overview of the selected month.
 */
public class DashboardPanel extends BasePanel {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final StatCard rate = new StatCard("Meal rate", Theme.TURMERIC);
    private final StatCard meals = new StatCard("Total meals", Theme.INK);
    private final StatCard bazar = new StatCard("Bazar cost", Theme.CHILLI);
    private final StatCard cash = new StatCard("Cash in hand", Theme.LEAF);
    private final DailyMealChart chart = new DailyMealChart();
    private final JLabel chartNote = Theme.label(" ", Font.PLAIN, 12, Theme.MUTED);
    private final JPanel activity = new JPanel();

    public DashboardPanel(MainFrame app) {
        super(app);
        setLayout(new GridBagLayout());

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(rate);
        stats.add(meals);
        stats.add(bazar);
        stats.add(cash);
        GridBagConstraints c = gbc(0, 0, 1, 0);
        c.gridwidth = 2;
        c.insets = new java.awt.Insets(0, 0, 16, 0);
        add(stats, c);

        RoundedPanel chartCard = card(null);
        JPanel chartHead = new JPanel(new BorderLayout());
        chartHead.setOpaque(false);
        chartHead.add(Theme.label("Meals per day", Font.BOLD, 16, Theme.TEXT), BorderLayout.WEST);
        chartHead.add(chartNote, BorderLayout.EAST);
        chartCard.add(chartHead, BorderLayout.NORTH);
        chartCard.add(chart, BorderLayout.CENTER);
        GridBagConstraints cc = gbc(0, 1, 0.62, 1);
        cc.insets = new java.awt.Insets(0, 0, 0, 16);
        add(chartCard, cc);

        RoundedPanel activityCard = card("Recent activity");
        activity.setOpaque(false);
        activity.setLayout(new BoxLayout(activity, BoxLayout.Y_AXIS));
        activityCard.add(activity, BorderLayout.CENTER);
        activityCard.setPreferredSize(new Dimension(380, 300));
        GridBagConstraints ac = gbc(1, 1, 0.38, 1);
        ac.insets = new java.awt.Insets(0, 0, 0, 0);
        add(activityCard, ac);
    }

    @Override
    public String getSubtitle() {
        return "How the mess is doing this month";
    }

    @Override
    public void refresh() {
        YearMonth m = month();
        MonthSummary s = service().getSummary(m);

        rate.setValue(Theme.tk(s.getMealRate()));
        rate.setNote("per meal  (bazar / meals)");
        meals.setValue(Theme.MEALS.format(s.getTotalMeals()));
        meals.setNote(service().getMealChartMembers(m).size() + " members eating");
        bazar.setValue(Theme.tkShort(s.getTotalBazar()));
        bazar.setNote("+ " + Theme.tkShort(s.getTotalShared()) + " shared bills");
        cash.setValue(Theme.tkShort(s.getCashInHand()));
        cash.setValueColor(s.getCashInHand() >= 0 ? Theme.LEAF : Theme.CHILLI);
        cash.setNote("deposits minus all costs");

        MealSheet sheet = service().getMealSheet(m);
        double[] perDay = new double[sheet.getDays()];
        for (int d = 1; d <= perDay.length; d++) {
            perDay[d - 1] = sheet.getDayTotal(d);
        }
        boolean thisMonth = YearMonth.now().equals(m);
        chart.setData(perDay, thisMonth ? LocalDate.now().getDayOfMonth() : -1);
        int days = thisMonth ? LocalDate.now().getDayOfMonth() : perDay.length;
        double avg = days == 0 ? 0 : s.getTotalMeals() / days;
        chartNote.setText("average " + Theme.MEALS.format(avg) + " meals a day");

        fillActivity(service().getRecentActivity(m, 7));
        revalidate();
        repaint();
    }

    private void fillActivity(List<Transaction> list) {
        activity.removeAll();
        if (list.isEmpty()) {
            JLabel none = Theme.label("No deposits or bazar yet this month.", Font.PLAIN, 13, Theme.MUTED);
            activity.add(none);
            return;
        }
        for (Transaction t : list) {
            activity.add(activityRow(t));
            activity.add(Box.createVerticalStrut(4));
        }
        activity.add(Box.createVerticalGlue());
    }

    /** Uses polymorphism: Deposit and Expense answer getType(), getDescription() and isIncome() in their own way. */
    private JPanel activityRow(Transaction t) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.PAPER_DARK),
                BorderFactory.createEmptyBorder(6, 0, 8, 0)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JLabel date = Theme.label(t.getDate().format(DAY_FMT), Font.BOLD, 11, Theme.MUTED);
        date.setPreferredSize(new Dimension(46, 20));

        JPanel middle = new JPanel();
        middle.setOpaque(false);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        String who = t.getMemberId().isEmpty() ? "Mess fund" : service().memberName(t.getMemberId());
        middle.add(Theme.label(who, Font.BOLD, 13, Theme.TEXT));
        middle.add(Theme.label(shorten(t.getDescription(), 30), Font.PLAIN, 12, Theme.MUTED));

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JLabel amount = Theme.label((t.isIncome() ? "+ " : "- ") + Theme.tkShort(t.getAmount()),
                Font.BOLD, 13, t.isIncome() ? Theme.LEAF : Theme.TEXT);
        amount.setHorizontalAlignment(SwingConstants.RIGHT);
        Pill type = typePill(t);
        type.setHorizontalAlignment(SwingConstants.RIGHT);
        right.add(amount, BorderLayout.NORTH);
        right.add(type, BorderLayout.SOUTH);

        row.add(date, BorderLayout.WEST);
        row.add(middle, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    static Pill typePill(Transaction t) {
        if (t.isIncome()) {
            return new Pill(t.getType(), Theme.LEAF_PALE, Theme.LEAF);
        }
        if ("Bazar".equals(t.getType())) {
            return new Pill(t.getType(), Theme.TURMERIC_PALE, Theme.TURMERIC_DARK);
        }
        return new Pill(t.getType(), new Color(0xE3E7EF), Theme.INK);
    }

    private static String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "...";
    }
}
