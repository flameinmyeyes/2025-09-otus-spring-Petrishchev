package com.union.app.messaging.repository;

import com.union.app.messaging.model.Message;
import com.union.app.messaging.model.Union;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByUnionOrderByTimestampAsc(Union union);

    @Query("SELECT m FROM Message m WHERE "
            + "(m.sender.id = :userId1 AND m.receiver.id = :userId2) "
            + "OR (m.sender.id = :userId2 AND m.receiver.id = :userId1) "
            + "ORDER BY m.timestamp ASC")
    List<Message> findPrivateMessages(@Param("userId1") Long userId1,
                                      @Param("userId2") Long userId2);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.union.id = :unionId")
    void deleteByUnionId(@Param("unionId") Long unionId);
}