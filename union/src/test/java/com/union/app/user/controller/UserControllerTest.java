package com.union.app.user.controller;

import com.union.app.testsupport.TestFixtures;
import com.union.app.user.dto.UserDto;
import com.union.app.user.model.User;
import com.union.app.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Test
    void handlesUserEndpoints() {
        UserService service = mock(UserService.class);
        UserController controller = new UserController(service);
        User current = TestFixtures.user(1L, "79990001111", "Current", "Online");
        User friend = TestFixtures.user(2L, "79990001122", "Friend", "Busy");
        current.getFriends().add(friend);
        User other = TestFixtures.user(3L, "79990001133", "Other", "Away");

        when(service.getCurrentUser()).thenReturn(current);
        when(service.getUserById(3L)).thenReturn(other);
        when(service.searchUsers("query")).thenReturn(List.of(other));
        when(service.updateProfile(1L, "Updated", "New Status")).thenReturn(TestFixtures.user(1L, "79990001111", "Updated", "New Status"));

        assertEquals(1L, controller.getCurrentUser().getId());
        assertEquals(3L, controller.getUserById(3L).getId());
        assertTrue(controller.addFriend(2L).getStatusCode().is2xxSuccessful());
        assertEquals(1, controller.getFriends().size());
        assertEquals(1, controller.searchUsers("query").size());

        UserDto update = new UserDto();
        update.setFullName("Updated");
        update.setStatus("New Status");
        assertEquals("Updated", controller.updateProfile(update).getFullName());
    }
}
