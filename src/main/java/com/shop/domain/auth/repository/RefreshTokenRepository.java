package com.shop.domain.auth.repository;

import com.shop.domain.auth.entity.RefreshToken;
import com.shop.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
    int deleteByToken(String token);
}
