package com.shop.domain.payment.service;

import com.shop.domain.order.entity.Order;
import com.shop.domain.payment.entity.Payment;
import com.shop.domain.payment.entity.PaymentMethod;
import com.shop.domain.payment.entity.PaymentStatus;
import com.shop.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MockPaymentService implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Payment processPayment(Order order, PaymentMethod paymentMethod) {
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.COMPLETED)
                .amount(order.getTotalAmount())
                .transactionId("MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paidAt(OffsetDateTime.now())
                .build();
        
        return paymentRepository.save(payment);
    }

    private void afaweac() {
        
    }

    private void testtest() {
        
    }
}
