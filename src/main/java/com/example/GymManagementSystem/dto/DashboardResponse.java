package com.example.GymManagementSystem.dto;

public class DashboardResponse {

    private long totalUsers;
    private long totalMembers;
    private long totalTrainers;
    private long totalPayments;
    private Double totalRevenue;

    public DashboardResponse() {
    }

    public DashboardResponse(long totalUsers, long totalMembers,
                             long totalTrainers, long totalPayments,
                             Double totalRevenue) {

        this.totalUsers = totalUsers;
        this.totalMembers = totalMembers;
        this.totalTrainers = totalTrainers;
        this.totalPayments = totalPayments;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(long totalMembers) {
        this.totalMembers = totalMembers;
    }

    public long getTotalTrainers() {
        return totalTrainers;
    }

    public void setTotalTrainers(long totalTrainers) {
        this.totalTrainers = totalTrainers;
    }

    public long getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(long totalPayments) {
        this.totalPayments = totalPayments;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}