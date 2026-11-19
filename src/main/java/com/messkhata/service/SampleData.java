package com.messkhata.service;

import com.messkhata.model.ExpenseCategory;
import com.messkhata.model.Member;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;

/**
 * Fills the app with a realistic month of data on the very first run,
 * so the screens are not empty when the project is shown.
 */
final class SampleData {

    private SampleData() {
    }

    static void fill(MessService s) {
        try {
            String[][] people = {
                {"Rakibul Hasan", "01711234567", "201"},
                {"Tanvir Ahmed", "01822345678", "201"},
                {"Shuvo Roy", "01933456789", "202"},
                {"Arif Hossain", "01644567890", "202"},
                {"Nayeem Islam", "01555678901", "203"},
            };
            for (String[] p : people) {
                s.addMember(p[0], p[1], p[2]);
            }
            List<Member> m = s.getMembers();
            YearMonth month = YearMonth.now();
            int today = LocalDate.now().getDayOfMonth();
            Random rnd = new Random(7);
            double[] choices = {2, 2, 2, 3, 1.5, 2, 1, 2.5, 2, 0};

            for (int day = 1; day <= today; day++) {
                for (Member member : m) {
                    s.setMeal(month, member.getId(), day, choices[rnd.nextInt(choices.length)]);
                }
            }

            String[] items = {"Rice, dal, oil", "Fish (rui), vegetables", "Chicken 3 kg, potato",
                "Eggs 4 dozen, onion", "Beef 2 kg, spices", "Vegetables, green chilli", "Ilish, tomato",
                "Lentils, salt, sugar"};
            int i = 0;
            for (int day = 1; day <= today; day += 2) {
                Member buyer = m.get(i % m.size());
                double amount = 650 + rnd.nextInt(10) * 70;
                s.addExpense(month.atDay(day), buyer.getId(), ExpenseCategory.BAZAR,
                        items[i % items.length], amount, i % 3 == 0);
                i++;
            }

            s.addExpense(month.atDay(1), "", ExpenseCategory.SHARED, "Cook (khala) salary", 4000, false);
            s.addExpense(month.atDay(Math.min(5, today)), "", ExpenseCategory.SHARED, "Gas bill", 1080, false);
            s.addExpense(month.atDay(Math.min(7, today)), "", ExpenseCategory.SHARED, "Wi-Fi bill", 1000, false);

            int[] amounts = {3000, 3500, 2500, 3000, 2000};
            for (int k = 0; k < m.size(); k++) {
                s.addDeposit(month.atDay(1), m.get(k).getId(), amounts[k], "Monthly advance");
            }
            if (today >= 10) {
                s.addDeposit(month.atDay(10), m.get(4).getId(), 1500, "Second installment");
            }
        } catch (MessException e) {
            System.err.println("Sample data skipped: " + e.getMessage());
        }
    }
}
