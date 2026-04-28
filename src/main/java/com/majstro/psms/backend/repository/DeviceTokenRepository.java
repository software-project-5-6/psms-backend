package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    
    List<DeviceToken> findByUser_IdAndActiveTrue(String userId);
    
    Optional<DeviceToken> findByTokenAndActiveTrue(String token);
    
    void deleteByToken(String token);
}
