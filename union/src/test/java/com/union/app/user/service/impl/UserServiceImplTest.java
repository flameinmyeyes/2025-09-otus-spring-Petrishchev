package com.union.app.user.service.impl;

import com.union.app.exception.AlreadyFriendsException;
import com.union.app.exception.SelfFriendException;
import com.union.app.exception.UserNotFoundException;
import com.union.app.testsupport.TestFixtures;
import com.union.app.user.model.User;
import com.union.app.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesCurrentUserFromSecurityContext() {
        UserRepository repo = mock(UserRepository.class);
        User user = TestFixtures.user(1L, "79990001111");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("79990001111", "n/a"));
        when(repo.findByPhoneNumber("79990001111")).thenReturn(Optional.of(user));

        UserServiceImpl service = new UserServiceImpl(repo);
        assertSame(user, service.getCurrentUser());
    }

    @Test
    void throwsWhenCurrentUserNotFound() {
        UserRepository repo = mock(UserRepository.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("79990001111", "n/a"));
        when(repo.findByPhoneNumber("79990001111")).thenReturn(Optional.empty());

        UserServiceImpl service = new UserServiceImpl(repo);
        assertThrows(UserNotFoundException.class, service::getCurrentUser);
    }

    @Test
    void addFriendDetectsSelfAndExistingFriendAndSavesBothSides() {
        UserRepository repo = mock(UserRepository.class);
        User current = TestFixtures.user(1L, "79990001111");
        User friend = TestFixtures.user(2L, "79990001122");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(current.getPhoneNumber(), "n/a"));

        when(repo.findByPhoneNumber(current.getPhoneNumber())).thenReturn(Optional.of(current));
        when(repo.findById(2L)).thenReturn(Optional.of(friend));
        when(repo.findByIdWithFriends(1L)).thenReturn(Optional.of(current));
        when(repo.findByIdWithFriends(2L)).thenReturn(Optional.of(friend));

        UserServiceImpl service = new UserServiceImpl(repo);
        service.addFriend(2L);

        assertTrue(current.getFriends().contains(friend));
        assertTrue(friend.getFriends().contains(current));
        verify(repo, atLeastOnce()).save(current);
        verify(repo, atLeastOnce()).save(friend);

        User self = TestFixtures.user(1L, "79990001111");
        when(repo.findById(1L)).thenReturn(Optional.of(self));
        assertThrows(SelfFriendException.class, () -> service.addFriend(1L));

        User existingCurrent = TestFixtures.user(1L, "79990001111");
        existingCurrent.getFriends().add(friend);
        User existingFriend = TestFixtures.user(2L, "79990001122");
        existingFriend.getFriends().add(existingCurrent);
        when(repo.findByIdWithFriends(1L)).thenReturn(Optional.of(existingCurrent));
        when(repo.findByIdWithFriends(2L)).thenReturn(Optional.of(existingFriend));
        assertThrows(AlreadyFriendsException.class, () -> service.addFriend(2L));
    }

    @Test
    void removeFriendUpdatesBothSides() {
        UserRepository repo = mock(UserRepository.class);
        User current = TestFixtures.user(1L, "79990001111");
        User friend = TestFixtures.user(2L, "79990001122");
        current.getFriends().add(friend);
        friend.getFriends().add(current);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(current.getPhoneNumber(), "n/a"));
        when(repo.findByPhoneNumber(current.getPhoneNumber())).thenReturn(Optional.of(current));
        when(repo.findById(2L)).thenReturn(Optional.of(friend));

        UserServiceImpl service = new UserServiceImpl(repo);
        service.removeFriend(2L);

        assertFalse(current.getFriends().contains(friend));
        assertFalse(friend.getFriends().contains(current));
    }

    @Test
    void searchUsersSkipsBlankAndFiltersCurrentUser() {
        UserRepository repo = mock(UserRepository.class);
        User current = TestFixtures.user(1L, "79990001111");
        User other = TestFixtures.user(2L, "79990001122");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(current.getPhoneNumber(), "n/a"));
        when(repo.findByPhoneNumber(current.getPhoneNumber())).thenReturn(Optional.of(current));
        when(repo.searchUsers("john")).thenReturn(List.of(current, other));

        UserServiceImpl service = new UserServiceImpl(repo);

        assertTrue(service.searchUsers("").isEmpty());
        assertEquals(List.of(other), service.searchUsers("john"));
    }

    @Test
    void updateProfileHandlesOptionalFields() {
        UserRepository repo = mock(UserRepository.class);
        User user = TestFixtures.user(1L, "79990001111");
        when(repo.findById(1L)).thenReturn(Optional.of(user));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserServiceImpl service = new UserServiceImpl(repo);
        User updated = service.updateProfile(1L, "New Name", null);

        assertEquals("New Name", updated.getFullName());
        assertEquals("Status 1", updated.getStatus());

        updated = service.updateProfile(1L, "", "New Status");
        assertEquals("New Name", updated.getFullName());
        assertEquals("New Status", updated.getStatus());
    }
}
