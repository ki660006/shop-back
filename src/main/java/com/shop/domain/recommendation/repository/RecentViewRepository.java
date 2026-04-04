package com.shop.domain.recommendation.repository;

import com.shop.domain.recommendation.entity.RecentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecentViewRepository extends JpaRepository<RecentView, UUID>, RecentViewRepositoryCustom {
    
    @Modifying
    @Query(value = "DELETE FROM recent_views WHERE id IN (" +
            "  SELECT id FROM (" +
            "    SELECT id, ROW_NUMBER() OVER(PARTITION BY user_id ORDER BY viewed_at DESC) as rn " +
            "    FROM recent_views" +
            "  ) tmp WHERE tmp.rn > :keepCount" +
            ")", nativeQuery = true)
    int deleteOldRecentViews(@Param("keepCount") int keepCount);
}
