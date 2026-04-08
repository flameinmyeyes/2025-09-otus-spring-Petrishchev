package com.union.app.auth.security;

import com.union.app.common.util.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesRequestWhenTokenIsValid() throws Exception {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(provider.validateToken("valid-token")).thenReturn(true);
        when(provider.getPhoneNumberFromToken("valid-token")).thenReturn("79990001122");
        when(userDetailsService.loadUserByUsername("79990001122")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(java.util.List.of());
        when(userDetails.getUsername()).thenReturn("79990001122");

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(Constants.JWT_HEADER, Constants.JWT_PREFIX + "valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("79990001122", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(userDetailsService).loadUserByUsername("79990001122");
        verify(chain).doFilter(request, response);
    }

    @Test
    void skipsAuthenticationWhenHeaderMissing() throws Exception {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider, userDetailsService);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), mock(FilterChain.class));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(provider, userDetailsService);
    }

    @Test
    void skipsAuthenticationWhenTokenIsInvalid() throws Exception {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        when(provider.validateToken("bad-token")).thenReturn(false);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider, userDetailsService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(Constants.JWT_HEADER, Constants.JWT_PREFIX + "bad-token");

        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(provider).validateToken("bad-token");
        verifyNoInteractions(userDetailsService);
    }
}
