package com.union.app.messaging.service.impl;

import com.union.app.messaging.dto.PollCreateDto;
import com.union.app.messaging.dto.PollResponseDto;
import com.union.app.messaging.dto.PollUpdateDto;
import com.union.app.messaging.model.Poll;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.repository.PollOptionRepository;
import com.union.app.messaging.repository.PollRepository;
import com.union.app.messaging.repository.UnionRepository;
import com.union.app.messaging.repository.VoteRepository;
import com.union.app.messaging.service.UnionService;
import com.union.app.testsupport.TestFixtures;
import com.union.app.user.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PollServiceImplTest {

    @Test
    void createsUpdatesVotesDeletesAndLoadsPolls() {
        PollRepository pollRepository = mock(PollRepository.class);
        UnionRepository unionRepository = mock(UnionRepository.class);
        VoteRepository voteRepository = mock(VoteRepository.class);
        PollOptionRepository pollOptionRepository = mock(PollOptionRepository.class);
        UnionService unionService = mock(UnionService.class);

        User creator = TestFixtures.user(1L, "79990001111");
        Union union = TestFixtures.union(10L, "Union", creator, creator);
        when(unionService.isMember(10L, creator)).thenReturn(true);
        when(unionRepository.findById(10L)).thenReturn(Optional.of(union));

        PollCreateDto createDto = new PollCreateDto();
        createDto.setQuestion("  Question  ");
        createDto.setOptions(List.of(" A ", "B", " "));
        Poll poll = TestFixtures.poll(1L, "Question", union, creator, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                TestFixtures.option(11L, "A"), TestFixtures.option(12L, "B"));
        when(pollRepository.save(any())).thenReturn(poll);

        PollServiceImpl service = new PollServiceImpl(pollRepository, unionRepository, voteRepository, pollOptionRepository, unionService);
        Poll created = service.createPoll(10L, createDto, creator);
        assertEquals("Question", created.getQuestion());

        PollUpdateDto updateDto = new PollUpdateDto();
        updateDto.setQuestion(" Updated ");
        updateDto.setOptions(List.of("X", "Y"));
        Poll existing = TestFixtures.poll(2L, "Old", union, creator, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                TestFixtures.option(21L, "A"), TestFixtures.option(22L, "B"));
        when(pollRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(pollRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Poll updated = service.updatePoll(2L, updateDto, creator);
        assertEquals("Updated", updated.getQuestion());
        assertEquals(2, updated.getOptions().size());

        User other = TestFixtures.user(2L, "79990001122");
        assertThrows(SecurityException.class, () -> service.updatePoll(2L, updateDto, other));

        PollUpdateDto invalid = new PollUpdateDto();
        invalid.setQuestion(" ");
        invalid.setOptions(List.of("X", "Y"));
        assertThrows(IllegalArgumentException.class, () -> service.updatePoll(2L, invalid, creator));

        PollUpdateDto fewOptions = new PollUpdateDto();
        fewOptions.setQuestion("Q");
        fewOptions.setOptions(List.of("only-one"));
        assertThrows(IllegalArgumentException.class, () -> service.updatePoll(2L, fewOptions, creator));

        Poll votePoll = TestFixtures.poll(3L, "Q", union, creator, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                TestFixtures.option(31L, "A"), TestFixtures.option(32L, "B"));
        when(pollRepository.findById(3L)).thenReturn(Optional.of(votePoll));
        when(voteRepository.existsByUserAndOptionPoll(any(), any())).thenReturn(false);
        when(voteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        service.vote(3L, 32L, creator);

        assertThrows(RuntimeException.class, () -> service.vote(4L, 31L, creator));
        when(pollRepository.findById(4L)).thenReturn(Optional.of(votePoll));
        when(voteRepository.existsByUserAndOptionPoll(any(), any())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.vote(4L, 31L, creator));
        when(voteRepository.existsByUserAndOptionPoll(any(), any())).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.vote(3L, 99L, creator));

        when(pollRepository.findById(5L)).thenReturn(Optional.of(votePoll));
        PollResponseDto results = service.getPollResults(5L, creator);
        assertEquals(3L, results.getId());

        when(pollRepository.findById(6L)).thenReturn(Optional.of(votePoll));
        service.deletePoll(6L, creator);
        verify(voteRepository, atLeastOnce()).deleteByOptionPollId(anyLong());
        verify(pollOptionRepository, atLeastOnce()).deleteByPollId(anyLong());
        verify(pollRepository).delete(votePoll);

        when(unionRepository.findById(10L)).thenReturn(Optional.of(union));
        when(pollRepository.findByUnion(union)).thenReturn(List.of(votePoll));
        assertEquals(1, service.getPollsByUnion(10L, creator).size());

        when(unionRepository.findById(11L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getPollsByUnion(11L, creator));
    }

    @Test
    void rejectsNonMembersAndWrongCreators() {
        PollRepository pollRepository = mock(PollRepository.class);
        UnionRepository unionRepository = mock(UnionRepository.class);
        VoteRepository voteRepository = mock(VoteRepository.class);
        PollOptionRepository pollOptionRepository = mock(PollOptionRepository.class);
        UnionService unionService = mock(UnionService.class);
        User creator = TestFixtures.user(1L, "79990001111");
        when(unionService.isMember(10L, creator)).thenReturn(false);

        PollServiceImpl service = new PollServiceImpl(pollRepository, unionRepository, voteRepository, pollOptionRepository, unionService);
        PollCreateDto createDto = new PollCreateDto();
        createDto.setQuestion("Question");
        createDto.setOptions(List.of("A", "B"));
        assertThrows(SecurityException.class, () -> service.createPoll(10L, createDto, creator));

        when(unionService.isMember(10L, creator)).thenReturn(true);
        when(unionRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.createPoll(10L, createDto, creator));
    }
}
