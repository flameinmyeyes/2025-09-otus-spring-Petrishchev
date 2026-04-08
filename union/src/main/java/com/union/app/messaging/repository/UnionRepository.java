package com.union.app.messaging.repository;

import com.union.app.user.model.User;
import com.union.app.messaging.model.Union;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UnionRepository extends JpaRepository<Union, Long> {

    @Query("SELECT u FROM Union u WHERE :user MEMBER OF u.members")
    List<Union> findByMembersContaining(@Param("user") User user);
}