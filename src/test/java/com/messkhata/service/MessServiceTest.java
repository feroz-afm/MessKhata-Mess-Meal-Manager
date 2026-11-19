package com.messkhata.service;

import com.messkhata.model.ExpenseCategory;
import com.messkhata.model.Member;
import com.messkhata.model.MemberSummary;
import com.messkhata.model.MonthSummary;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Dependency-free checks for the money calculations in {@link MessService}.
 * Run: javac -d out (all sources + this file) and then: java -cp out com.messkhata.service.MessServiceTest
 */
public class MessServiceTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        mealRateAndBalances();
        paidFromOwnPocketCreditsDeposit();
        invalidMealCountsAreRejected();
        loginChecksPassword();
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** README example: bazar 1000 / 10 meals = 100; a 600 bill shared by 2 = 300 each. */
    private static void mealRateAndBalances() throws Exception {
        MessService s = freshService();
        YearMonth m = YearMonth.of(2020, 1);
        Member a = s.addMember("Rahim", "01711111111", "101");
        Member b = s.addMember("Karim", "01722222222", "102");
        s.setMeal(m, a.getId(), 1, 3);
        s.setMeal(m, a.getId(), 2, 1);
        s.setMeal(m, b.getId(), 1, 3);
        s.setMeal(m, b.getId(), 2, 3);
        s.addExpense(LocalDate.of(2020, 1, 2), "", ExpenseCategory.BAZAR, "Rice and fish", 1000, false);
        s.addExpense(LocalDate.of(2020, 1, 3), "", ExpenseCategory.SHARED, "Gas bill", 600, false);
        s.addDeposit(LocalDate.of(2020, 1, 1), a.getId(), 1000, "advance");
        s.addDeposit(LocalDate.of(2020, 1, 1), b.getId(), 500, "advance");

        MonthSummary sum = s.getSummary(m);
        check("total meals = 10", sum.getTotalMeals(), 10);
        check("meal rate = 100", sum.getMealRate(), 100);
        check("shared bill each = 300", sum.getSharedPerHead(), 300);
        check("Rahim balance = +300", row(sum, a).getBalance(), 300);
        check("Karim balance = -400", row(sum, b).getBalance(), -400);
        check("cash in hand = 1500 - 1600", sum.getCashInHand(), -100);
    }

    private static void paidFromOwnPocketCreditsDeposit() throws Exception {
        MessService s = freshService();
        YearMonth m = YearMonth.of(2020, 2);
        Member a = s.addMember("Hasan", "01733333333", "201");
        s.addExpense(LocalDate.of(2020, 2, 5), a.getId(), ExpenseCategory.BAZAR, "Vegetables", 250, true);
        check("own-pocket bazar becomes a deposit", s.getSummary(m).getTotalDeposit(), 250);
    }

    private static void invalidMealCountsAreRejected() throws Exception {
        MessService s = freshService();
        Member a = s.addMember("Jamal", "01744444444", "301");
        expectError("quarter meal rejected", () -> s.setMeal(YearMonth.of(2020, 3), a.getId(), 1, 1.25));
        expectError("more than 6 meals rejected", () -> s.setMeal(YearMonth.of(2020, 3), a.getId(), 1, 7));
        expectError("bad phone rejected", () -> s.addMember("Test", "12345", "1"));
    }

    private static void loginChecksPassword() throws Exception {
        MessService s = freshService();
        s.login("admin", "1234");
        pass("default admin can log in");
        expectError("wrong password rejected", () -> s.login("admin", "0000"));
    }

    // ------------------------------------------------------------------ helpers

    /** Empty data folder; an empty members file skips the first-run sample data. */
    private static MessService freshService() throws Exception {
        Path dir = Files.createTempDirectory("messkhata-test");
        Files.createFile(dir.resolve("members.txt"));
        return new MessService(dir.toString());
    }

    private static MemberSummary row(MonthSummary sum, Member m) {
        for (MemberSummary r : sum.getMembers()) {
            if (r.getMember().getId().equals(m.getId())) {
                return r;
            }
        }
        throw new IllegalStateException("No summary row for " + m.getName());
    }

    private interface Action {
        void run() throws Exception;
    }

    private static void expectError(String name, Action action) {
        try {
            action.run();
            fail(name + " (no error thrown)");
        } catch (MessException e) {
            pass(name);
        } catch (Exception e) {
            fail(name + " (unexpected " + e + ")");
        }
    }

    private static void check(String name, double actual, double expected) {
        if (Math.abs(actual - expected) < 1e-9) {
            pass(name);
        } else {
            fail(name + " (expected " + expected + ", got " + actual + ")");
        }
    }

    private static void pass(String name) {
        passed++;
        System.out.println("PASS  " + name);
    }

    private static void fail(String name) {
        failed++;
        System.out.println("FAIL  " + name);
    }
}
