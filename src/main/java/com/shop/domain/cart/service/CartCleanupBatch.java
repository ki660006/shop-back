package com.shop.domain.cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import com.shop.domain.cart.entity.Cart;
import com.shop.domain.cart.repository.CartRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartCleanupBatch {

    private final CartRepository cartRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @SchedulerLock(name = "CartCleanup", lockAtLeastFor = "PT1M", lockAtMostFor = "PT10M")
    @Transactional
    public void cleanupCarts() {
        log.info("Starting scheduled cart cleanup");
        OffsetDateTime expiryDate = OffsetDateTime.now().minusDays(7);
        cartRepository.deleteByUpdatedAtBefore(expiryDate);
        log.info("Finished scheduled cart cleanup");
    }
}
