package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByAuthSub(String authSub);

    Optional<User> findByEmail(String email);

    boolean existsByAuthSub(String authSub);
}