package com.union.app.messaging.repository;

import com.union.app.messaging.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {

    @Modifying
    @Query("DELETE FROM PollOption po WHERE po.poll.id = :pollId")
    void deleteByPollId(@Param("pollId") Long pollId);

    @Modifying
    @Query("DELETE FROM PollOption po WHERE po.poll.union.id = :unionId")
    void deleteByPollUnionId(@Param("unionId") Long unionId);
}
