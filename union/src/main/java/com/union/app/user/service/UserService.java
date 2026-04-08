package com.union.app.user.service;

import com.union.app.user.model.User;
import java.util.List;

public interface UserService {
    User getCurrentUser();

    User getUserById(Long id);

    User getUserByPhoneNumber(String phoneNumber);

    void addFriend(Long friendId);

    void removeFriend(Long friendId);

    List<User> searchUsers(String query);

    User updateProfile(Long userId, String fullName, String status);
}