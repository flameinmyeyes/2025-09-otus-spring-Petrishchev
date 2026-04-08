package com.union.app.messaging.service.impl;

import com.union.app.messaging.dto.UnionCreateDto;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.repository.EventRepository;
import com.union.app.messaging.repository.MessageRepository;
import com.union.app.messaging.repository.PollOptionRepository;
import com.union.app.messaging.repository.PollRepository;
import com.union.app.messaging.repository.UnionRepository;
import com.union.app.messaging.repository.VoteRepository;
import com.union.app.testsupport.TestFixtures;
import com.union.app.user.model.User;
import com.union.app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnionServiceImplTest {
    @Test
    void managesUnionsAndMembers() {
        UnionRepository unionRepository = mock(UnionRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PollRepository pollRepository = mock(PollRepository.class);
        PollOptionRepository pollOptionRepository = mock(PollOptionRepository.class);
        VoteRepository voteRepository = mock(VoteRepository.class);
        EventRepository eventRepository = mock(EventRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);

        User creator = TestFixtures.user(1L, "79990001111");
        User member = TestFixtures.user(2L, "79990001122");

        Union union = TestFixtures.union(10L, "Union", creator, creator);
        union.getMembers().add(creator);

        when(unionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unionRepository.findById(10L)).thenReturn(Optional.of(union));
        when(unionRepository.findByMembersContaining(creator)).thenReturn(List.of(union));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));

        UnionServiceImpl service = new UnionServiceImpl(
                unionRepository,
                userRepository,
                pollRepository,
                pollOptionRepository,
                voteRepository,
                eventRepository,
                messageRepository
        );

        UnionCreateDto createDto = new UnionCreateDto();
        createDto.setName("New Union");
        createDto.setDescription("desc");

        Union created = service.createUnion(createDto, creator);

        assertEquals("New Union", created.getName());
        assertTrue(created.getMembers().contains(creator));

        assertEquals(List.of(union), service.getUserUnions(creator));
        assertEquals(union, service.getUnionById(10L));

        service.addMember(10L, 2L);
        assertTrue(union.getMembers().contains(member));

        service.removeMember(10L, 2L);
        assertFalse(union.getMembers().contains(member));

        UnionCreateDto updateDto = new UnionCreateDto();
        updateDto.setName("Updated");
        updateDto.setDescription(null);

        Union updated = service.updateUnion(10L, updateDto);
        assertEquals("Updated", updated.getName());

        assertTrue(service.isMember(10L, creator));
        assertDoesNotThrow(() -> service.checkUserIsCreator(10L, creator));

        User other = TestFixtures.user(3L, "79990001133");
        assertThrows(SecurityException.class, () -> service.checkUserIsCreator(10L, other));

        service.deleteUnion(10L);

        verify(messageRepository).deleteByUnionId(10L);
        verify(voteRepository).deleteByPollUnionId(10L);
        verify(pollOptionRepository).deleteByPollUnionId(10L);
        verify(pollRepository).deleteByUnionId(10L);
        verify(eventRepository).deleteByUnionId(10L);
        verify(unionRepository, atLeastOnce()).delete(union);
    }

    @Test
    void throwsWhenUnionOrUserMissing() {
        UnionRepository unionRepository = mock(UnionRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        PollRepository pollRepository = mock(PollRepository.class);
        PollOptionRepository pollOptionRepository = mock(PollOptionRepository.class);
        VoteRepository voteRepository = mock(VoteRepository.class);
        EventRepository eventRepository = mock(EventRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);

        UnionServiceImpl service = new UnionServiceImpl(
                unionRepository,
                userRepository,
                pollRepository,
                pollOptionRepository,
                voteRepository,
                eventRepository,
                messageRepository
        );

        when(unionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getUnionById(10L));
        assertThrows(RuntimeException.class, () -> service.addMember(10L, 2L));
        assertThrows(RuntimeException.class, () -> service.removeMember(10L, 2L));
    }
}
