package com.careerconnect.repository;

import com.careerconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring magically writes the SQL for this based on the method name!
    // We will need this to check if a user exists during login.
    Optional<User> findByEmail(String email);

    // We will use this during registration to ensure emails are unique.
    boolean existsByEmail(String email);
}