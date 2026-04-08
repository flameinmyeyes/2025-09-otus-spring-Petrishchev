package com.union.app.user.repository;

import com.union.app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.friends WHERE u.id = :id")
    Optional<User> findByIdWithFriends(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE "
            + "LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR "
            + "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@Param("query") String query);
}