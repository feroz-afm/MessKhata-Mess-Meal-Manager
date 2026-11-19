package com.messkhata.model;

import java.time.YearMonth;
import java.util.List;

/**
 * Totals of one month and every member's account.
 */
public class MonthSummary {

    private final YearMonth month;
    private final double totalMeals;
    private final double totalBazar;
    private final double totalShared;
    private final double totalDeposit;
    private final int sharingMembers;
    private final List<MemberSummary> members;

    public MonthSummary(YearMonth month, double totalMeals, double totalBazar, double totalShared,
                        double totalDeposit, int sharingMembers, List<MemberSummary> members) {
        this.month = month;
        this.totalMeals = totalMeals;
        this.totalBazar = totalBazar;
        this.totalShared = totalShared;
        this.totalDeposit = totalDeposit;
        this.sharingMembers = sharingMembers;
        this.members = members;
    }

    public YearMonth getMonth() {
        return month;
    }

    public double getTotalMeals() {
        return totalMeals;
    }

    public double getTotalBazar() {
        return totalBazar;
    }

    public double getTotalShared() {
        return totalShared;
    }

    public double getTotalDeposit() {
        return totalDeposit;
    }

    /** Meal rate = total bazar / total meals. */
    public double getMealRate() {
        return totalMeals == 0 ? 0 : totalBazar / totalMeals;
    }

    public double getSharedPerHead() {
        return sharingMembers == 0 ? 0 : totalShared / sharingMembers;
    }

    public double getCashInHand() {
        return totalDeposit - totalBazar - totalShared;
    }

    public List<MemberSummary> getMembers() {
        return members;
    }
}
