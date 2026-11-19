package com.messkhata.model;

/**
 * One member's account for one month.
 */
public class MemberSummary {

    private final Member member;
    private final double meals;
    private final double mealCost;
    private final double sharedCost;
    private final double deposit;

    public MemberSummary(Member member, double meals, double mealCost, double sharedCost, double deposit) {
        this.member = member;
        this.meals = meals;
        this.mealCost = mealCost;
        this.sharedCost = sharedCost;
        this.deposit = deposit;
    }

    public Member getMember() {
        return member;
    }

    public double getMeals() {
        return meals;
    }

    public double getMealCost() {
        return mealCost;
    }

    public double getSharedCost() {
        return sharedCost;
    }

    public double getDeposit() {
        return deposit;
    }

    public double getTotalCost() {
        return mealCost + sharedCost;
    }

    /** Positive: the mess owes the member. Negative: the member must pay. */
    public double getBalance() {
        return deposit - getTotalCost();
    }

    public String getStatus() {
        double b = getBalance();
        if (Math.abs(b) < 0.5) {
            return "Settled";
        }
        return b > 0 ? "Will get" : "Will pay";
    }
}
