package com.union.app.user.service.impl;

import com.union.app.user.model.User;
import com.union.app.user.repository.UserRepository;
import com.union.app.exception.AlreadyFriendsException;
import com.union.app.exception.FriendNotFoundException;
import com.union.app.exception.SelfFriendException;
import com.union.app.exception.UserNotFoundException;
import com.union.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public void addFriend(Long friendId) {
        User currentUser = getCurrentUser();
        User friend = getUserById(friendId);

        if (currentUser.getId().equals(friend.getId())) {
            throw new SelfFriendException("Cannot add yourself as friend");
        }

        currentUser = userRepository.findByIdWithFriends(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        friend = userRepository.findByIdWithFriends(friend.getId())
                .orElseThrow(() -> new FriendNotFoundException("Friend not found"));

        if (currentUser.getFriends().contains(friend)) {
            throw new AlreadyFriendsException("Already friends");
        }

        currentUser.getFriends().add(friend);
        friend.getFriends().add(currentUser);

        userRepository.save(currentUser);
        userRepository.save(friend);
    }

    @Override
    @Transactional
    public void removeFriend(Long friendId) {
        User currentUser = getCurrentUser();
        User friend = getUserById(friendId);

        currentUser.getFriends().remove(friend);
        friend.getFriends().remove(currentUser);

        userRepository.save(currentUser);
        userRepository.save(friend);
    }

    @Override
    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        User currentUser = getCurrentUser();
        return userRepository.searchUsers(query).stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String fullName, String status) {
        User user = getUserById(userId);
        if (fullName != null && !fullName.isEmpty()) {
            user.setFullName(fullName);
        }
        if (status != null) {
            user.setStatus(status);
        }
        return userRepository.save(user);
    }
}