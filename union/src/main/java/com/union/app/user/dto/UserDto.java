package com.union.app.user.dto;

import com.union.app.user.model.User;
import lombok.Data;

@Data
public class UserDto {
    private Long id;

    private String phoneNumber;

    private String fullName;

    private String status;

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setFullName(user.getFullName());
        dto.setStatus(user.getStatus());
        return dto;
    }
}