package com.ecommerce.repository;

import com.ecommerce.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<Users> findAllByRole(String role);    // ✅ Fixed — String not Users
    void deleteByUserId(Long userId);
}