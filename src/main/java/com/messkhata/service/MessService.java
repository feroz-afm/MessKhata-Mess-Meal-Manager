package com.messkhata.service;

import com.messkhata.data.FileDatabase;
import com.messkhata.model.Deposit;
import com.messkhata.model.Expense;
import com.messkhata.model.ExpenseCategory;
import com.messkhata.model.Manager;
import com.messkhata.model.MealSheet;
import com.messkhata.model.Member;
import com.messkhata.model.MemberSummary;
import com.messkhata.model.MonthSummary;
import com.messkhata.model.Transaction;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The backend of the app. All rules and calculations live here;
 * the screens only call these methods.
 */
public class MessService {

    private static final String MEMBERS = "members.txt";
    private static final String MANAGERS = "managers.txt";
    private static final String DEPOSITS = "deposits.txt";
    private static final String EXPENSES = "expenses.txt";

    public static final double MAX_MEALS_PER_DAY = 6;

    private final FileDatabase db;
    private final List<Member> members = new ArrayList<>();
    private final List<Manager> managers = new ArrayList<>();
    private final List<Deposit> deposits = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();
    private final Map<YearMonth, MealSheet> sheets = new HashMap<>();

    public MessService(String dataFolder) throws IOException {
        db = new FileDatabase(dataFolder);
        boolean firstRun = !db.exists(MEMBERS);
        load();
        if (managers.isEmpty()) {
            managers.add(new Manager("A001", "Mess Manager", "01700000000", "admin", "1234"));
            db.writeAll(MANAGERS, managers);
        }
        if (firstRun) {
            SampleData.fill(this);
        }
    }

    private void load() throws IOException {
        for (String[] p : db.read(MEMBERS)) {
            members.add(Member.fromParts(p));
        }
        for (String[] p : db.read(MANAGERS)) {
            managers.add(Manager.fromParts(p));
        }
        for (String[] p : db.read(DEPOSITS)) {
            deposits.add(Deposit.fromParts(p));
        }
        for (String[] p : db.read(EXPENSES)) {
            expenses.add(Expense.fromParts(p));
        }
    }

    private void save(String file, List<? extends com.messkhata.model.Storable> items) throws MessException {
        try {
            db.writeAll(file, items);
        } catch (IOException e) {
            throw new MessException("Could not save " + file + ": " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- login

    public Manager login(String username, String password) throws MessException {
        for (Manager m : managers) {
            if (m.checkLogin(username.trim(), password)) {
                return m;
            }
        }
        throw new MessException("Wrong username or password.");
    }

    // -------------------------------------------------------------- members

    public List<Member> getMembers() {
        return new ArrayList<>(members);
    }

    public List<Member> getActiveMembers() {
        List<Member> list = new ArrayList<>();
        for (Member m : members) {
            if (m.isActive()) {
                list.add(m);
            }
        }
        return list;
    }

    public Member findMember(String id) {
        for (Member m : members) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        return null;
    }

    public String memberName(String id) {
        Member m = findMember(id);
        return m == null ? "Mess fund" : m.getName();
    }

    private String nextMemberId() {
        int max = 0;
        for (Member m : members) {
            max = Math.max(max, Integer.parseInt(m.getId().substring(1)));
        }
        return String.format("M%03d", max + 1);
    }

    private void checkMemberInput(String name, String phone, String room) throws MessException {
        if (name.trim().length() < 2) {
            throw new MessException("Please enter the member's name.");
        }
        if (!phone.trim().matches("01[3-9]\\d{8}")) {
            throw new MessException("Phone number must be 11 digits and start with 01 (e.g. 01712345678).");
        }
        if (room.trim().isEmpty()) {
            throw new MessException("Please enter a room number.");
        }
    }

    public Member addMember(String name, String phone, String room) throws MessException {
        checkMemberInput(name, phone, room);
        for (Member m : members) {
            if (m.getPhone().equals(phone.trim())) {
                throw new MessException(m.getName() + " already uses this phone number.");
            }
        }
        Member m = new Member(nextMemberId(), FileDatabase.clean(name), phone.trim(),
                FileDatabase.clean(room), LocalDate.now(), true);
        members.add(m);
        save(MEMBERS, members);
        return m;
    }

    public void updateMember(String id, String name, String phone, String room, boolean active)
            throws MessException {
        Member m = findMember(id);
        if (m == null) {
            throw new MessException("Member not found.");
        }
        checkMemberInput(name, phone, room);
        for (Member other : members) {
            if (other != m && other.getPhone().equals(phone.trim())) {
                throw new MessException(other.getName() + " already uses this phone number.");
            }
        }
        m.setName(FileDatabase.clean(name));
        m.setPhone(phone.trim());
        m.setRoom(FileDatabase.clean(room));
        m.setActive(active);
        save(MEMBERS, members);
    }

    public void deleteMember(String id) throws MessException {
        Member m = findMember(id);
        if (m == null) {
            throw new MessException("Member not found.");
        }
        for (Transaction t : allTransactions()) {
            if (t.getMemberId().equals(id)) {
                throw new MessException(m.getName() + " has deposits or bazar records, so the history "
                        + "would break.\nMark the member as inactive instead.");
            }
        }
        members.remove(m);
        save(MEMBERS, members);
    }

    // ---------------------------------------------------------------- meals

    public MealSheet getMealSheet(YearMonth month) {
        MealSheet sheet = sheets.get(month);
        if (sheet == null) {
            try {
                sheet = MealSheet.fromParts(month, db.read(mealFile(month)));
            } catch (IOException e) {
                sheet = new MealSheet(month);
            }
            sheets.put(month, sheet);
        }
        return sheet;
    }

    private String mealFile(YearMonth month) {
        return "meals_" + month + ".txt";
    }

    /** Members shown in a month's meal chart: active ones plus anyone who ate that month. */
    public List<Member> getMealChartMembers(YearMonth month) {
        MealSheet sheet = getMealSheet(month);
        List<Member> list = new ArrayList<>();
        for (Member m : members) {
            if (m.isActive() || sheet.hasMember(m.getId())) {
                list.add(m);
            }
        }
        return list;
    }

    public void setMeal(YearMonth month, String memberId, int day, double count) throws MessException {
        if (count < 0 || count > MAX_MEALS_PER_DAY) {
            throw new MessException("Meals per day must be between 0 and " + (int) MAX_MEALS_PER_DAY + ".");
        }
        if (Math.round(count * 2) != count * 2) {
            throw new MessException("Use whole or half meals only (e.g. 1, 1.5, 2).");
        }
        MealSheet sheet = getMealSheet(month);
        sheet.setMeal(memberId, day, count);
        saveSheet(sheet);
    }

    public int setMealForAll(YearMonth month, int day, double count) throws MessException {
        List<Member> active = getActiveMembers();
        if (active.isEmpty()) {
            throw new MessException("There are no active members.");
        }
        for (Member m : active) {
            setMeal(month, m.getId(), day, count);
        }
        return active.size();
    }

    private void saveSheet(MealSheet sheet) throws MessException {
        try {
            db.write(mealFile(sheet.getMonth()), sheet.toLines());
        } catch (IOException e) {
            throw new MessException("Could not save the meal chart: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------- expenses

    public List<Expense> getExpenses(YearMonth month) {
        List<Expense> list = new ArrayList<>();
        for (Expense e : expenses) {
            if (YearMonth.from(e.getDate()).equals(month)) {
                list.add(e);
            }
        }
        list.sort(Comparator.comparing(Transaction::getDate).thenComparing(Transaction::getId));
        return list;
    }

    /**
     * Records an expense. If paidBySelf is true the member spent their own money,
     * so the same amount is also added to their deposits.
     */
    public Expense addExpense(LocalDate date, String memberId, ExpenseCategory category,
                              String details, double amount, boolean paidBySelf) throws MessException {
        checkAmount(amount);
        if (details.trim().isEmpty()) {
            throw new MessException("Please write what was bought or which bill was paid.");
        }
        if (paidBySelf && findMember(memberId) == null) {
            throw new MessException("Choose the member who paid.");
        }
        Expense e = new Expense(nextId("E", expenses), date, memberId, category, amount,
                FileDatabase.clean(details));
        expenses.add(e);
        save(EXPENSES, expenses);
        if (paidBySelf) {
            addDeposit(date, memberId, amount, "Paid for " + category.getLabel().toLowerCase(Locale.ROOT)
                    + " (" + e.getId() + ")");
        }
        return e;
    }

    public void deleteExpense(String id) throws MessException {
        Expense found = null;
        for (Expense e : expenses) {
            if (e.getId().equals(id)) {
                found = e;
            }
        }
        if (found == null) {
            throw new MessException("Expense not found.");
        }
        expenses.remove(found);
        save(EXPENSES, expenses);
        // remove the matching "paid by self" deposit, if any
        deposits.removeIf(d -> d.getNote().endsWith("(" + id + ")"));
        save(DEPOSITS, deposits);
    }

    // ------------------------------------------------------------- deposits

    public List<Deposit> getDeposits(YearMonth month) {
        List<Deposit> list = new ArrayList<>();
        for (Deposit d : deposits) {
            if (YearMonth.from(d.getDate()).equals(month)) {
                list.add(d);
            }
        }
        list.sort(Comparator.comparing(Transaction::getDate).thenComparing(Transaction::getId));
        return list;
    }

    public Deposit addDeposit(LocalDate date, String memberId, double amount, String note) throws MessException {
        checkAmount(amount);
        if (findMember(memberId) == null) {
            throw new MessException("Choose a member.");
        }
        Deposit d = new Deposit(nextId("D", deposits), date, memberId, amount, FileDatabase.clean(note));
        deposits.add(d);
        save(DEPOSITS, deposits);
        return d;
    }

    public void deleteDeposit(String id) throws MessException {
        boolean removed = deposits.removeIf(d -> d.getId().equals(id));
        if (!removed) {
            throw new MessException("Deposit not found.");
        }
        save(DEPOSITS, deposits);
    }

    private void checkAmount(double amount) throws MessException {
        if (amount <= 0) {
            throw new MessException("Amount must be more than 0.");
        }
        if (amount > 500000) {
            throw new MessException("That amount looks too large. Please check it.");
        }
    }

    private String nextId(String prefix, List<? extends Transaction> list) {
        int max = 0;
        for (Transaction t : list) {
            max = Math.max(max, Integer.parseInt(t.getId().substring(1)));
        }
        return String.format("%s%04d", prefix, max + 1);
    }

    // --------------------------------------------------------- calculations

    private List<Transaction> allTransactions() {
        List<Transaction> all = new ArrayList<>(deposits);
        all.addAll(expenses);
        return all;
    }

    /** Latest deposits and expenses together, newest first. */
    public List<Transaction> getRecentActivity(YearMonth month, int limit) {
        List<Transaction> list = new ArrayList<>();
        list.addAll(getDeposits(month));
        list.addAll(getExpenses(month));
        list.sort(Comparator.comparing(Transaction::getDate).thenComparing(Transaction::getId).reversed());
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    public MonthSummary getSummary(YearMonth month) {
        MealSheet sheet = getMealSheet(month);
        double totalBazar = 0;
        double totalShared = 0;
        for (Expense e : getExpenses(month)) {
            if (e.getCategory() == ExpenseCategory.BAZAR) {
                totalBazar += e.getAmount();
            } else {
                totalShared += e.getAmount();
            }
        }
        Map<String, Double> depositOf = new HashMap<>();
        double totalDeposit = 0;
        for (Deposit d : getDeposits(month)) {
            depositOf.merge(d.getMemberId(), d.getAmount(), Double::sum);
            totalDeposit += d.getAmount();
        }

        // who is part of this month: in the meal chart or gave money
        List<Member> included = new ArrayList<>();
        for (Member m : members) {
            if (m.isActive() || sheet.getMemberTotal(m.getId()) > 0 || depositOf.containsKey(m.getId())) {
                included.add(m);
            }
        }
        int sharing = 0;
        for (Member m : included) {
            if (m.isActive() || sheet.getMemberTotal(m.getId()) > 0) {
                sharing++;
            }
        }

        double totalMeals = sheet.getGrandTotal();
        double rate = totalMeals == 0 ? 0 : totalBazar / totalMeals;
        double sharedPerHead = sharing == 0 ? 0 : totalShared / sharing;

        List<MemberSummary> rows = new ArrayList<>();
        for (Member m : included) {
            double meals = sheet.getMemberTotal(m.getId());
            boolean shares = m.isActive() || meals > 0;
            rows.add(new MemberSummary(m, meals, meals * rate, shares ? sharedPerHead : 0,
                    depositOf.getOrDefault(m.getId(), 0.0)));
        }
        return new MonthSummary(month, totalMeals, totalBazar, totalShared, totalDeposit, sharing, rows);
    }

    /** Writes a plain text report that can be printed or shared on Messenger. */
    public Path exportReport(YearMonth month) throws MessException {
        MonthSummary s = getSummary(month);
        String title = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        List<String> out = new ArrayList<>();
        out.add("MESS KHATA - MONTHLY REPORT - " + title.toUpperCase(Locale.ROOT));
        out.add("Generated on " + LocalDate.now());
        out.add("");
        out.add(String.format("Total meals      : %.1f", s.getTotalMeals()));
        out.add(String.format("Total bazar      : Tk %,.2f", s.getTotalBazar()));
        out.add(String.format("Meal rate        : Tk %.2f per meal", s.getMealRate()));
        out.add(String.format("Shared bills     : Tk %,.2f  (Tk %.2f each)", s.getTotalShared(), s.getSharedPerHead()));
        out.add(String.format("Total deposit    : Tk %,.2f", s.getTotalDeposit()));
        out.add(String.format("Cash in hand     : Tk %,.2f", s.getCashInHand()));
        out.add("");
        out.add(String.format("%-18s %7s %11s %10s %11s %11s %13s", "Member", "Meals", "Meal cost",
                "Shared", "Total cost", "Deposit", "Balance"));
        out.add(repeat('-', 87));
        for (MemberSummary m : s.getMembers()) {
            out.add(String.format("%-18s %7.1f %11.2f %10.2f %11.2f %11.2f %+13.2f", m.getMember().getName(),
                    m.getMeals(), m.getMealCost(), m.getSharedCost(), m.getTotalCost(), m.getDeposit(),
                    m.getBalance()));
        }
        out.add(repeat('-', 87));
        out.add("Balance: + means the member will get money back, - means the member has to pay.");
        String file = "report_" + month + ".txt";
        try {
            db.write(file, out);
        } catch (IOException e) {
            throw new MessException("Could not write the report: " + e.getMessage());
        }
        return db.getFolder().resolve(file).toAbsolutePath();
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
