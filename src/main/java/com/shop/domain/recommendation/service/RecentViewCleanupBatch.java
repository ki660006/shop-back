package com.shop.domain.recommendation.service;

import com.shop.domain.recommendation.repository.RecentViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentViewCleanupBatch {

    private final RecentViewRepository recentViewRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    @SchedulerLock(name = "recentViewCleanup", lockAtMostFor = "PT10M")
    @Transactional
    public void cleanupOldRecentViews() {
        log.info("Starting scheduled cleanup of old recent views...");
        int deletedCount = recentViewRepository.deleteOldRecentViews(50);
        log.info("Finished cleanup. Deleted {} old recent view records.", deletedCount);
    }
}
