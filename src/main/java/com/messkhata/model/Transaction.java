package com.messkhata.model;

import java.time.LocalDate;

/**
 * Money that moves in or out of the mess fund.
 * Deposit and Expense both extend this class.
 */
public abstract class Transaction implements Storable {

    private final String id;
    private final LocalDate date;
    private final String memberId;
    private final double amount;

    protected Transaction(String id, LocalDate date, String memberId, double amount) {
        this.id = id;
        this.date = date;
        this.memberId = memberId;
        this.amount = amount;
    }

    /** Short label shown in lists, e.g. "Deposit" or "Bazar". */
    public abstract String getType();

    /** One-line explanation of the transaction. */
    public abstract String getDescription();

    /** true if money comes into the fund, false if it goes out. */
    public abstract boolean isIncome();

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getMemberId() {
        return memberId;
    }

    public double getAmount() {
        return amount;
    }
}
