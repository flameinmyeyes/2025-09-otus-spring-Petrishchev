package com.union.app.messaging.repository;

import com.union.app.user.model.User;
import com.union.app.messaging.model.Poll;
import com.union.app.messaging.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByUserAndOptionPoll(User user, Poll poll);

    @Modifying
    @Query("DELETE FROM Vote v WHERE v.option.poll.id = :pollId")
    void deleteByOptionPollId(@Param("pollId") Long pollId);

    @Modifying
    @Query("DELETE FROM Vote v WHERE v.option.poll.union.id = :unionId")
    void deleteByPollUnionId(@Param("unionId") Long unionId);
}
