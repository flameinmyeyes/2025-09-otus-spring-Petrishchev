package com.union.app.auth.security;

import com.union.app.testsupport.TestFixtures;
import com.union.app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    @Test
    void loadsUserByPhoneNumber() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByPhoneNumber("79990001122")).thenReturn(Optional.of(TestFixtures.user(1L, "79990001122")));

        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        var userDetails = service.loadUserByUsername("79990001122");

        assertAll(
                () -> assertEquals("79990001122", userDetails.getUsername()),
                () -> assertEquals("encoded-password-1", userDetails.getPassword()),
                () -> assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")))
        );
    }

    @Test
    void throwsWhenUserNotFound() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByPhoneNumber("79990001122")).thenReturn(Optional.empty());

        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("79990001122"));
    }
}
