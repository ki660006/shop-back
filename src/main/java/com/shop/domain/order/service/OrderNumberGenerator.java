package com.shop.domain.order.service;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String uuidPart = UuidCreator.getTimeOrderedEpoch().toString().substring(0, 8);
        return datePart + "-" + uuidPart;
    }
}
