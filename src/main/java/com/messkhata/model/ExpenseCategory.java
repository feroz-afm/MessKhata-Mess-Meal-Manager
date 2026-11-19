package com.messkhata.model;

/**
 * BAZAR is divided by meals eaten.
 * SHARED (rent, gas, cook, internet) is divided equally between active members.
 */
public enum ExpenseCategory {

    BAZAR("Bazar"),
    SHARED("Shared bill");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
