package com.majstro.psms.backend.repository;

import com.majstro.psms.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by their unique Cognito "sub" ID
    Optional<User> findByCognitoSub(String cognitoSub);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check if user exists by Cognito sub
    boolean existsByCognitoSub(String cognitoSub);
}