package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.PaymentRepository;
import com.example.GymManagementSystem.repository.TrainerRepository;
import com.example.GymManagementSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public Map<String, Object> getDashboard() {

        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("totalUsers", userRepository.count());
        dashboard.put("totalMembers", memberRepository.count());
        dashboard.put("totalTrainers", trainerRepository.count());
        dashboard.put("totalPayments", paymentRepository.count());

        Double revenue = paymentRepository.getTotalRevenue();

        if (revenue == null) {
            revenue = 0.0;
        }

        dashboard.put("totalRevenue", revenue);

        return dashboard;
    }
}