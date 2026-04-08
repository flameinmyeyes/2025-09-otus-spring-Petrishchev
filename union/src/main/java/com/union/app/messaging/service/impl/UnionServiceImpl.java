package com.union.app.messaging.service.impl;

import com.union.app.user.model.User;
import com.union.app.user.repository.UserRepository;
import com.union.app.messaging.dto.UnionCreateDto;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.repository.EventRepository;
import com.union.app.messaging.repository.MessageRepository;
import com.union.app.messaging.repository.PollOptionRepository;
import com.union.app.messaging.repository.PollRepository;
import com.union.app.messaging.repository.UnionRepository;
import com.union.app.messaging.repository.VoteRepository;
import com.union.app.messaging.service.UnionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UnionServiceImpl implements UnionService {
    private final UnionRepository unionRepository;

    private final UserRepository userRepository;

    private final PollRepository pollRepository;

    private final PollOptionRepository pollOptionRepository;

    private final VoteRepository voteRepository;

    private final EventRepository eventRepository;

    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public Union createUnion(UnionCreateDto dto, User creator) {
        Union union = new Union();
        union.setName(dto.getName().trim());
        union.setDescription(dto.getDescription());
        union.setCreator(creator);

        Set<User> members = new HashSet<>();
        members.add(creator);
        union.setMembers(members);

        return unionRepository.save(union);
    }

    @Override
    public List<Union> getUserUnions(User user) {
        return unionRepository.findByMembersContaining(user);
    }

    @Override
    public Union getUnionById(Long id) {
        return unionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Union not found"));
    }

    @Override
    @Transactional
    public void addMember(Long unionId, Long userId) {
        Union union = getUnionById(unionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        union.getMembers().add(user);
        unionRepository.save(union);
    }

    @Override
    @Transactional
    public void removeMember(Long unionId, Long userId) {
        Union union = getUnionById(unionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        union.getMembers().remove(user);
        unionRepository.save(union);
    }

    @Override
    @Transactional
    public Union updateUnion(Long id, UnionCreateDto dto) {
        Union union = getUnionById(id);
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            union.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            union.setDescription(dto.getDescription());
        }
        return unionRepository.save(union);
    }

    @Override
    @Transactional
    public void deleteUnion(Long id) {
        Union union = getUnionById(id);

        messageRepository.deleteByUnionId(id);
        voteRepository.deleteByPollUnionId(id);
        pollOptionRepository.deleteByPollUnionId(id);
        pollRepository.deleteByUnionId(id);
        eventRepository.deleteByUnionId(id);

        union.getMembers().clear();
        unionRepository.save(union);
        unionRepository.delete(union);
    }

    @Override
    public boolean isMember(Long unionId, User user) {
        Union union = getUnionById(unionId);
        return union.getMembers().contains(user);
    }

    public void checkUserIsCreator(Long unionId, User user) {
        Union union = getUnionById(unionId);
        if (!union.getCreator().getId().equals(user.getId())) {
            throw new SecurityException("Only the creator can manage members");
        }
    }
}
