package com.union.app.messaging.service;

import com.union.app.user.model.User;
import com.union.app.messaging.dto.UnionCreateDto;
import com.union.app.messaging.model.Union;
import java.util.List;

public interface UnionService {
    Union createUnion(UnionCreateDto dto, User creator);

    List<Union> getUserUnions(User user);

    Union getUnionById(Long id);

    void addMember(Long unionId, Long userId);

    void removeMember(Long unionId, Long userId);

    Union updateUnion(Long id, UnionCreateDto dto);

    void deleteUnion(Long id);

    boolean isMember(Long unionId, User user);
}