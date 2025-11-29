package com.carwash.dto;

public class StatsSummary {
    private int usersCount;
    private int completedBookings;
    private int satisfactionPercent;
    private int avgPaymentResponseMinutes;

    public int getUsersCount() { return usersCount; }
    public void setUsersCount(int usersCount) { this.usersCount = usersCount; }
    public int getCompletedBookings() { return completedBookings; }
    public void setCompletedBookings(int completedBookings) { this.completedBookings = completedBookings; }
    public int getSatisfactionPercent() { return satisfactionPercent; }
    public void setSatisfactionPercent(int satisfactionPercent) { this.satisfactionPercent = satisfactionPercent; }
    public int getAvgPaymentResponseMinutes() { return avgPaymentResponseMinutes; }
    public void setAvgPaymentResponseMinutes(int avgPaymentResponseMinutes) { this.avgPaymentResponseMinutes = avgPaymentResponseMinutes; }
}
