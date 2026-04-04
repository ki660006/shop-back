package com.shop.domain.payment.service;

import com.shop.domain.order.entity.Order;
import com.shop.domain.payment.entity.Payment;
import com.shop.domain.payment.entity.PaymentMethod;

public interface PaymentService {
    Payment processPayment(Order order, PaymentMethod paymentMethod);
}
