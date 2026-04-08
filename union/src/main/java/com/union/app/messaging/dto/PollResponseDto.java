package com.union.app.messaging.dto;

import com.union.app.messaging.model.Poll;
import com.union.app.messaging.model.PollOption;
import com.union.app.user.dto.UserDto;
import com.union.app.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Schema(description = "Ответ с результатами голосования")
public class PollResponseDto {

    @Schema(description = "ID голосования", example = "1")
    private Long id;

    @Schema(description = "Вопрос голосования", example = "Какой язык программирования лучше?")
    private String question;

    @Schema(description = "Создатель голосования")
    private UserDto createdBy;

    @Schema(description = "Дата создания", example = "2026-04-04T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "ID объединения", example = "1")
    private Long unionId;

    @Schema(description = "Голосовал ли текущий пользователь")
    private boolean hasUserVoted;

    @Schema(description = "Список вариантов ответа")
    private List<OptionDto> options;

    @Data
    @Schema(description = "Вариант ответа")
    public static class OptionDto {
        @Schema(description = "ID варианта", example = "1")
        private Long id;

        @Schema(description = "Текст варианта", example = "Java")
        private String text;

        @Schema(description = "Количество голосов", example = "42")
        private long voteCount;

        @Schema(description = "Процент голосов", example = "58.33")
        private double votePercentage;

        @Schema(description = "Голосовал ли текущий пользователь за этот вариант")
        private boolean voted;
    }

    public static PollResponseDto fromEntity(Poll poll, User currentUser) {
        if (poll == null) {
            return null;
        }
        PollResponseDto dto = createPollResponseDto(new PollResponseDto(), poll);
        dto.setId(poll.getId());
        dto.setQuestion(poll.getQuestion());
        dto.setCreatedBy(UserDto.fromEntity(poll.getCreatedBy()));
        dto.setCreatedAt(poll.getCreatedAt());
        dto.setUnionId(poll.getUnion() != null ? poll.getUnion().getId() : null);
        List<PollOption> options = poll.getOptions();
        if (options == null) {
            options = new ArrayList<>();
        }
        long totalVotes = getTotalVotes(options);
        boolean hasUserVoted = hasUserVotedInPoll(options, currentUser);
        dto.setHasUserVoted(hasUserVoted);
        List<OptionDto> optionDtos = getOptionsDtos(options, totalVotes, currentUser);
        dto.setOptions(optionDtos);
        return dto;
    }

    private static List<OptionDto> getOptionsDtos(List<PollOption> options, long totalVotes, User currentUser) {
        return options.stream()
                .map(opt -> {
                    OptionDto optDto = new OptionDto();
                    optDto.setId(opt.getId());
                    optDto.setText(opt.getText() != null ? opt.getText() : "");
                    long voteCount = opt.getVotes() != null ? opt.getVotes().size() : 0;
                    optDto.setVoteCount(voteCount);
                    if (totalVotes > 0) {
                        double percentage = (voteCount * 100.0) / totalVotes;
                        optDto.setVotePercentage(Math.round(percentage * 100.0) / 100.0);
                    } else {
                        optDto.setVotePercentage(0.0);
                    }
                    boolean voted = false;
                    if (currentUser != null && opt.getVotes() != null) {
                        voted = opt.getVotes().stream()
                                .anyMatch(vote -> vote.getUser() != null && vote.getUser().getId()
                                        .equals(currentUser.getId()));
                    }
                    optDto.setVoted(voted);
                    return optDto;
                })
                .collect(Collectors.toList());
    }

    private static long getTotalVotes(List<PollOption> options) {
        return options.stream()
                .mapToLong(opt -> opt.getVotes() != null ? opt.getVotes().size() : 0)
                .sum();
    }

    private static PollResponseDto createPollResponseDto(PollResponseDto dto, Poll poll) {
        dto.setId(poll.getId());
        dto.setQuestion(poll.getQuestion());
        dto.setCreatedBy(UserDto.fromEntity(poll.getCreatedBy()));
        dto.setCreatedAt(poll.getCreatedAt());
        dto.setUnionId(poll.getUnion() != null ? poll.getUnion().getId() : null);
        return dto;
    }

    private static boolean hasUserVotedInPoll(List<PollOption> options, User currentUser) {
        if (currentUser == null) {
            return false;
        }
        return options.stream()
                .anyMatch(opt -> opt.getVotes() != null && opt.getVotes().stream().anyMatch(vote ->
                        vote.getUser() != null && vote.getUser().getId().equals(currentUser.getId())));
    }
}