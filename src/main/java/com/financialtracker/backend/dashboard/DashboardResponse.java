package com.financialtracker.backend.dashboard;

import java.math.BigDecimal;

public class DashboardResponse {

    private BigDecimal totalBalance;

    private BigDecimal totalIncome;

    private BigDecimal totalExpenses;

    private BigDecimal netCashFlow;

    private BigDecimal savingsGoalProgress;

    private BigDecimal budgetUsagePercentage;

    public BigDecimal getSavingsGoalProgress() {
        return savingsGoalProgress;
    }


    public BigDecimal getNetCashFlow() {
        return netCashFlow;
    }



    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }
    public BigDecimal getBudgetUsagePercentage() {
        return budgetUsagePercentage;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }
    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }
    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }
    public void setNetCashFlow(BigDecimal netCashFlow) {
        this.netCashFlow = netCashFlow;
    }
    public void setSavingsGoalProgress(BigDecimal savingsGoalProgress) {
        this.savingsGoalProgress = savingsGoalProgress;
    }
    public void setBudgetUsagePercentage(BigDecimal budgetUsagePercentage) {
        this.budgetUsagePercentage = budgetUsagePercentage;
    }

}
