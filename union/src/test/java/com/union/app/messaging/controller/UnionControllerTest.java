package com.union.app.messaging.controller;

import com.union.app.messaging.dto.UnionCreateDto;
import com.union.app.messaging.model.Union;
import com.union.app.messaging.service.UnionService;
import com.union.app.testsupport.TestFixtures;
import com.union.app.user.model.User;
import com.union.app.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnionControllerTest {

    @Test
    void handlesUnionEndpoints() {
        UnionService unionService = mock(UnionService.class);
        UserService userService = mock(UserService.class);
        UnionController controller = new UnionController(unionService, userService);
        User current = TestFixtures.user(1L, "79990001111");
        Union union = TestFixtures.union(10L, "Union", current, current);
        when(userService.getCurrentUser()).thenReturn(current);
        when(unionService.createUnion(any(), eq(current))).thenReturn(union);
        when(unionService.getUserUnions(current)).thenReturn(List.of(union));
        when(unionService.getUnionById(10L)).thenReturn(union);
        when(unionService.updateUnion(eq(10L), any())).thenReturn(union);

        UnionCreateDto dto = new UnionCreateDto();
        dto.setName("Union");
        dto.setDescription("desc");

        assertEquals(10L, controller.createUnion(dto).getId());
        assertEquals(1, controller.getUserUnions().size());
        assertEquals(10L, controller.getUnionById(10L).getId());
        assertTrue(controller.addMember(10L, 2L).getStatusCode().is2xxSuccessful());
        assertTrue(controller.removeMember(10L, 2L).getStatusCode().is2xxSuccessful());
        assertEquals(10L, controller.updateUnion(10L, dto).getId());
        assertTrue(controller.deleteUnion(10L).getStatusCode().is2xxSuccessful());
    }
}
