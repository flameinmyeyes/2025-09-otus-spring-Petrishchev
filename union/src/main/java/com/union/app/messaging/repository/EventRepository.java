package com.union.app.messaging.repository;

import com.union.app.messaging.model.Event;
import com.union.app.messaging.model.Union;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUnion(Union union);

    @Modifying
    @Query("DELETE FROM Event e WHERE e.union.id = :unionId")
    void deleteByUnionId(@Param("unionId") Long unionId);
}
