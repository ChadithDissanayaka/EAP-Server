package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.ROLE_TYPES;
import com.automobileproject.eap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByPasswordResetToken(String passwordResetToken);

    List<User> findByRole(ROLE_TYPES role);

    List<User> findByShopIdAndRole(UUID shopId, ROLE_TYPES role);

    List<User> findByShopId(UUID shopId);
}

