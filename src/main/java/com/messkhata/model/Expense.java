package com.messkhata.model;

import java.time.LocalDate;

/**
 * Money spent from the mess fund: daily bazar or a shared bill.
 */
public class Expense extends Transaction {

    private final ExpenseCategory category;
    private final String details;

    public Expense(String id, LocalDate date, String memberId, ExpenseCategory category,
                   double amount, String details) {
        super(id, date, memberId, amount);
        this.category = category;
        this.details = details;
    }

    @Override
    public String getType() {
        return category.getLabel();
    }

    @Override
    public String getDescription() {
        return details;
    }

    @Override
    public boolean isIncome() {
        return false;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    @Override
    public String toLine() {
        return String.join(SEPARATOR, getId(), getDate().toString(), getMemberId(),
                category.name(), String.valueOf(getAmount()), details);
    }

    public static Expense fromParts(String[] p) {
        return new Expense(p[0], LocalDate.parse(p[1]), p[2], ExpenseCategory.valueOf(p[3]),
                Double.parseDouble(p[4]), p[5]);
    }
}
