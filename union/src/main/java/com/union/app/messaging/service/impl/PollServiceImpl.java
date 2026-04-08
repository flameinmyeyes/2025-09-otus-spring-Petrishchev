package com.union.app.messaging.service.impl;

import com.union.app.messaging.dto.PollCreateDto;
import com.union.app.messaging.dto.PollResponseDto;
import com.union.app.messaging.dto.PollUpdateDto;
import com.union.app.messaging.model.Poll;
import com.union.app.messaging.model.PollOption;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.model.Vote;
import com.union.app.messaging.repository.PollOptionRepository;
import com.union.app.messaging.repository.PollRepository;
import com.union.app.messaging.repository.UnionRepository;
import com.union.app.messaging.repository.VoteRepository;
import com.union.app.messaging.service.PollService;
import com.union.app.messaging.service.UnionService;
import com.union.app.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {
    private final PollRepository pollRepository;

    private final UnionRepository unionRepository;

    private final VoteRepository voteRepository;

    private final PollOptionRepository pollOptionRepository;

    private final UnionService unionService;

    @Override
    @Transactional
    public Poll createPoll(Long unionId, PollCreateDto dto, User creator) {
        if (!unionService.isMember(unionId, creator)) {
            throw new SecurityException("User is not a member of this union");
        }
        Union union = unionRepository.findById(unionId)
                .orElseThrow(() -> new RuntimeException("Union not found"));
        if (dto.getQuestion() == null || dto.getQuestion().trim().isEmpty()) {
            throw new IllegalArgumentException("Введите вопрос");
        }
        if (dto.getOptions() == null || dto.getOptions().stream().filter(option -> option != null
                && !option.trim().isEmpty()).count() < 2) {
            throw new IllegalArgumentException("Добавьте минимум 2 варианта ответа");
        }
        Poll poll = new Poll();
        poll.setQuestion(dto.getQuestion().trim());
        poll.setUnion(union);
        poll.setCreatedBy(creator);
        List<PollOption> options = getCreatePollOptions(dto, poll);
        poll.setOptions(options);
        return pollRepository.save(poll);
    }

    @Override
    @Transactional
    public Poll updatePoll(Long pollId, PollUpdateDto dto, User currentUser) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        if (!poll.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only the creator can edit this poll");
        }
        if (dto.getQuestion() == null || dto.getQuestion().trim().isEmpty()) {
            throw new IllegalArgumentException("Введите вопрос");
        }
        if (dto.getOptions() == null || dto.getOptions().stream().filter(option -> option != null
                && !option.trim().isEmpty()).count() < 2) {
            throw new IllegalArgumentException("Добавьте минимум 2 варианта ответа");
        }
        voteRepository.deleteByOptionPollId(pollId);
        poll.getOptions().clear();
        poll.setQuestion(dto.getQuestion().trim());
        List<PollOption> options = getUpdatePollOptions(dto, poll);
        poll.getOptions().addAll(options);

        return pollRepository.save(poll);
    }

    @Override
    @Transactional
    public void vote(Long pollId, Long optionId, User user) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        if (voteRepository.existsByUserAndOptionPoll(user, poll)) {
            throw new RuntimeException("User already voted in this poll");
        }
        PollOption option = poll.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Option not found"));
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setOption(option);
        voteRepository.save(vote);
    }

    @Override
    public PollResponseDto getPollResults(Long pollId, User currentUser) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        return PollResponseDto.fromEntity(poll, currentUser);
    }

    @Override
    @Transactional
    public void deletePoll(Long pollId, User currentUser) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));

        if (!poll.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new SecurityException("Only the creator can delete this poll");
        }

        voteRepository.deleteByOptionPollId(pollId);
        pollOptionRepository.deleteByPollId(pollId);
        pollRepository.delete(poll);
    }

    @Override
    public List<PollResponseDto> getPollsByUnion(Long unionId, User currentUser) {
        Union union = unionRepository.findById(unionId)
                .orElseThrow(() -> new RuntimeException("Union not found"));

        List<Poll> polls = pollRepository.findByUnion(union);

        return polls.stream()
                .map(poll -> PollResponseDto.fromEntity(poll, currentUser))
                .collect(Collectors.toList());
    }

    private List<PollOption> getCreatePollOptions(PollCreateDto dto, Poll poll) {
        return dto.getOptions().stream()
                .filter(optionText -> optionText != null && !optionText.trim().isEmpty())
                .map(optionText -> {
                    PollOption option = new PollOption();
                    option.setText(optionText.trim());
                    option.setPoll(poll);
                    return option;
                })
                .collect(Collectors.toList());
    }

    private List<PollOption> getUpdatePollOptions(PollUpdateDto dto, Poll poll) {
        return dto.getOptions().stream()
                .filter(optionText -> optionText != null && !optionText.trim().isEmpty())
                .map(optionText -> {
                    PollOption option = new PollOption();
                    option.setText(optionText.trim());
                    option.setPoll(poll);
                    return option;
                })
                .collect(Collectors.toList());
    }
}
