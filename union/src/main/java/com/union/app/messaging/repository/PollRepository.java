package com.union.app.messaging.repository;

import com.union.app.messaging.model.Poll;
import com.union.app.messaging.model.Union;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    List<Poll> findByUnion(Union union);

    @Modifying
    @Query("DELETE FROM Poll p WHERE p.union.id = :unionId")
    void deleteByUnionId(@Param("unionId") Long unionId);
}
