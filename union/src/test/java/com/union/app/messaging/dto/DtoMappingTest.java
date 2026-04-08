package com.union.app.messaging.dto;

import com.union.app.testsupport.TestFixtures;
import com.union.app.user.dto.UserDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DtoMappingTest {

    @Test
    void mapsUserUnionMessageEventAndPollDtos() {
        var creator = TestFixtures.user(1L, "79990001111", "Creator", "Active");
        var member = TestFixtures.user(2L, "79990001122", "Member", "Busy");
        var union = TestFixtures.union(10L, "Union", creator, creator, member);
        var sender = creator;
        var receiver = member;
        var message = TestFixtures.message(100L, "Hello", sender, union, receiver, LocalDateTime.now());
        var event = TestFixtures.event(200L, "Meetup", "Desc", LocalDateTime.now().plusDays(1), "Online", union, creator, LocalDateTime.now());
        var poll = TestFixtures.poll(300L, "Question?", union, creator, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                TestFixtures.option(1L, "A", TestFixtures.vote(1L, creator)), TestFixtures.option(2L, "B"));

        UserDto userDto = UserDto.fromEntity(creator);
        UnionDto unionDto = UnionDto.fromEntity(union);
        MessageDto messageDto = MessageDto.fromEntity(message);
        EventDto eventDto = EventDto.fromEntity(event);
        PollResponseDto pollResponseDto = PollResponseDto.fromEntity(poll, creator);

        assertAll(
                () -> assertEquals(1L, userDto.getId()),
                () -> assertEquals(10L, unionDto.getId()),
                () -> assertEquals(2L, unionDto.getMembers().size()),
                () -> assertEquals(100L, messageDto.getId()),
                () -> assertEquals(10L, messageDto.getUnionId()),
                () -> assertEquals(2L, messageDto.getReceiverId()),
                () -> assertEquals(200L, eventDto.getId()),
                () -> assertEquals(300L, pollResponseDto.getId()),
                () -> assertTrue(pollResponseDto.isHasUserVoted())
        );
    }

    @Test
    void pollResponseDtoHandlesExpiredPollAndNoVotes() {
        var creator = TestFixtures.user(1L, "79990001111");
        var union = TestFixtures.union(10L, "Union", creator, creator);
        var poll = TestFixtures.poll(300L, "Question?", union, creator, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusHours(1),
                TestFixtures.option(1L, null));

        PollResponseDto dto = PollResponseDto.fromEntity(poll, null);

        assertAll(
                () -> assertFalse(dto.isHasUserVoted()),
                () -> assertEquals("", dto.getOptions().get(0).getText()),
                () -> assertEquals(0.0, dto.getOptions().get(0).getVotePercentage())
        );
    }

    @Test
    void staticMappersReturnNullForNullEntities() {
        assertAll(
                () -> assertNull(UserDto.fromEntity(null)),
                () -> assertNull(UnionDto.fromEntity(null)),
                () -> assertNull(MessageDto.fromEntity(null)),
                () -> assertNull(EventDto.fromEntity(null)),
                () -> assertNull(PollResponseDto.fromEntity(null, null))
        );
    }

    @Test
    void pollDeleteResponseDtoConstructorWorks() {
        PollDeleteResponseDto dto = new PollDeleteResponseDto("ok", 5L, "success");
        assertEquals("ok", dto.getMessage());
        assertEquals(5L, dto.getPollId());
        assertEquals("success", dto.getStatus());
    }
}
