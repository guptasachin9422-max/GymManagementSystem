package com.example.GymManagementSystem.repository;

import com.example.GymManagementSystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    

    List<Payment> findByMemberUserId(Integer userId);
@Query("SELECT SUM(p.amount) FROM Payment p")
Double getTotalRevenue();

}