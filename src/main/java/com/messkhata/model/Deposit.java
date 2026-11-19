package com.messkhata.model;

import java.time.LocalDate;

/**
 * Money a member gives to the mess fund.
 */
public class Deposit extends Transaction {

    private final String note;

    public Deposit(String id, LocalDate date, String memberId, double amount, String note) {
        super(id, date, memberId, amount);
        this.note = note;
    }

    @Override
    public String getType() {
        return "Deposit";
    }

    @Override
    public String getDescription() {
        return note.isEmpty() ? "Cash deposit" : note;
    }

    @Override
    public boolean isIncome() {
        return true;
    }

    public String getNote() {
        return note;
    }

    @Override
    public String toLine() {
        return String.join(SEPARATOR, getId(), getDate().toString(), getMemberId(),
                String.valueOf(getAmount()), note);
    }

    public static Deposit fromParts(String[] p) {
        return new Deposit(p[0], LocalDate.parse(p[1]), p[2], Double.parseDouble(p[3]), p[4]);
    }
}
