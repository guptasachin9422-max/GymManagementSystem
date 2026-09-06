package com.example.GymManagementSystem.repository;

import com.example.GymManagementSystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    

    List<Payment> findByMemberUserId(Integer userId);
@Query("SELECT SUM(p.amount) FROM Payment p")
Double getTotalRevenue();

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findFirstByMemberIdOrderByPaymentIdDesc(Integer memberId);

    Optional<Payment> findFirstByMemberIdAndPaymentStatusInOrderByPaymentIdDesc(
            Integer memberId, List<String> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Payment p where p.member.id = :memberId")
    int deleteByMemberId(@Param("memberId") Integer memberId);
}

