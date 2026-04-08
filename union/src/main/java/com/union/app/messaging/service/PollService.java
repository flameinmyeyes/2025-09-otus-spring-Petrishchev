package com.union.app.messaging.service;

import com.union.app.messaging.dto.PollResponseDto;
import com.union.app.user.model.User;
import com.union.app.messaging.dto.PollCreateDto;
import com.union.app.messaging.dto.PollUpdateDto;
import com.union.app.messaging.model.Poll;

import java.util.List;

public interface PollService {
    Poll createPoll(Long unionId, PollCreateDto dto, User creator);

    Poll updatePoll(Long pollId, PollUpdateDto dto, User currentUser);

    void vote(Long pollId, Long optionId, User user);

    PollResponseDto getPollResults(Long pollId, User currentUser);

    void deletePoll(Long pollId, User currentUser);

    List<PollResponseDto> getPollsByUnion(Long unionId, User currentUser);
}
