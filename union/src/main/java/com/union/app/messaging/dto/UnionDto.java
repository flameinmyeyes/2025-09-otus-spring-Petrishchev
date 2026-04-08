package com.union.app.messaging.dto;

import com.union.app.user.dto.UserDto;
import com.union.app.messaging.model.Union;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class UnionDto {
    private Long id;

    private String name;

    private String description;

    private UserDto creator;

    private List<UserDto> members;

    public static UnionDto fromEntity(Union union) {
        if (union == null) {
            return null;
        }
        UnionDto dto = new UnionDto();
        dto.setId(union.getId());
        dto.setName(union.getName());
        dto.setDescription(union.getDescription());
        dto.setCreator(UserDto.fromEntity(union.getCreator()));
        dto.setMembers(union.getMembers().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList()));
        return dto;
    }
}