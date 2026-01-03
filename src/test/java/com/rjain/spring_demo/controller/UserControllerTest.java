/* (C)2025 */
package com.rjain.spring_demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rjain.spring_demo.hibernate.dto.UserDto;
import com.rjain.spring_demo.service.UserService;
import com.rjain.spring_demo.util.JsonObjectMapperUtil;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UserService userService;

    // ==================== CREATE USER TESTS ====================

    @Test
    @DisplayName("POST /user should create user and return 201 CREATED")
    void testCreateUserSuccess() throws Exception {
        // Arrange
        UserDto inputDto =
                UserDto.builder()
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .build();

        UserDto responseDto =
                UserDto.builder()
                        .id(1L)
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .createdAt(Instant.now())
                        .build();

        when(userService.createUser(any())).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(
                        post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("john_doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(userService, times(1)).createUser(any());
    }

    @Test
    @DisplayName("POST /user should return 400 BAD_REQUEST when username exists")
    void testCreateUserWithDuplicateUsername() throws Exception {
        // Arrange
        UserDto inputDto = UserDto.builder().username("john_doe").email("john@example.com").build();

        when(userService.createUser(any()))
                .thenThrow(
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.BAD_REQUEST,
                                "username already exists"));

        // Act & Assert
        mockMvc.perform(
                        post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).createUser(any());
    }

    @Test
    @DisplayName("POST /user should return 400 BAD_REQUEST when email exists")
    void testCreateUserWithDuplicateEmail() throws Exception {
        // Arrange
        UserDto inputDto = UserDto.builder().username("john_doe").email("john@example.com").build();

        when(userService.createUser(any()))
                .thenThrow(
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.BAD_REQUEST,
                                "email already exists"));

        // Act & Assert
        mockMvc.perform(
                        post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).createUser(any());
    }

    @Test
    @DisplayName("POST /user should create user with minimal data")
    void testCreateUserWithMinimalData() throws Exception {
        // Arrange
        UserDto inputDto = UserDto.builder().username("minimal_user").build();

        UserDto responseDto = UserDto.builder().id(2L).username("minimal_user").build();

        when(userService.createUser(any())).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(
                        post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.username", is("minimal_user")));

        verify(userService, times(1)).createUser(any());
    }

    // ==================== GET USER TESTS ====================

    @Test
    @DisplayName("GET /user/{id} should return user when found")
    void testGetUserSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        UserDto responseDto =
                UserDto.builder()
                        .id(userId)
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .createdAt(Instant.now())
                        .build();

        when(userService.getUser(userId)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/user/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("john_doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));

        verify(userService, times(1)).getUser(userId);
    }

    @Test
    @DisplayName("GET /user/{id} should return 404 NOT_FOUND when user not found")
    void testGetUserNotFound() throws Exception {
        // Arrange
        Long userId = 999L;

        when(userService.getUser(userId))
                .thenThrow(
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND, "user not found"));

        // Act & Assert
        mockMvc.perform(get("/user/{id}", userId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUser(userId);
    }

    @Test
    @DisplayName("GET /user/{id} should handle different user IDs")
    void testGetUserWithDifferentIds() throws Exception {
        // Arrange
        UserDto userDto1 = UserDto.builder().id(1L).username("user1").build();
        UserDto userDto2 = UserDto.builder().id(2L).username("user2").build();

        when(userService.getUser(1L)).thenReturn(userDto1);
        when(userService.getUser(2L)).thenReturn(userDto2);

        // Act & Assert - User 1
        mockMvc.perform(get("/user/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("user1")));

        // Act & Assert - User 2
        mockMvc.perform(get("/user/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.username", is("user2")));

        verify(userService, times(1)).getUser(1L);
        verify(userService, times(1)).getUser(2L);
    }

    // ==================== LIST USERS TESTS ====================

    @Test
    @DisplayName("GET /user should return list of users with pagination")
    void testListUsersSuccess() throws Exception {
        // Arrange
        UserDto user1 =
                UserDto.builder().id(1L).username("user1").email("user1@example.com").build();
        UserDto user2 =
                UserDto.builder().id(2L).username("user2").email("user2@example.com").build();
        List<UserDto> userList = List.of(user1, user2);

        when(userService.listUsers(any())).thenReturn(userList);

        // Act & Assert
        mockMvc.perform(
                        get("/user")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].username", is("user2")));

        verify(userService, times(1)).listUsers(any());
    }

    @Test
    @DisplayName("GET /user should return empty list when no users exist")
    void testListUsersEmpty() throws Exception {
        // Arrange
        when(userService.listUsers(any())).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(
                        get("/user")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).listUsers(any());
    }

    @Test
    @DisplayName("GET /user should handle different page numbers")
    void testListUsersWithPagination() throws Exception {
        // Arrange
        UserDto user3 = UserDto.builder().id(3L).username("user3").build();
        UserDto user4 = UserDto.builder().id(4L).username("user4").build();

        when(userService.listUsers(any())).thenReturn(List.of(user3, user4));

        // Act & Assert
        mockMvc.perform(
                        get("/user")
                                .param("page", "1")
                                .param("size", "2")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(userService, times(1)).listUsers(any());
    }

    // ==================== UPDATE USER TESTS ====================

    @Test
    @DisplayName("PUT /user/{id} should update user and return 200 OK")
    void testUpdateUserSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        UserDto updateDto =
                UserDto.builder()
                        .username("updated_user")
                        .email("updated@example.com")
                        .firstName("Updated")
                        .lastName("Name")
                        .active(false)
                        .build();

        UserDto responseDto =
                UserDto.builder()
                        .id(userId)
                        .username("updated_user")
                        .email("updated@example.com")
                        .firstName("Updated")
                        .lastName("Name")
                        .active(false)
                        .build();

        when(userService.updateUser(eq(userId), any())).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(
                        put("/user/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("updated_user")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.active", is(false)));

        verify(userService, times(1)).updateUser(eq(userId), any());
    }

    @Test
    @DisplayName("PUT /user/{id} should return 404 NOT_FOUND when user not found")
    void testUpdateUserNotFound() throws Exception {
        // Arrange
        Long userId = 999L;
        UserDto updateDto = UserDto.builder().username("updated").build();

        when(userService.updateUser(eq(userId), any()))
                .thenThrow(
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND, "user not found"));

        // Act & Assert
        mockMvc.perform(
                        put("/user/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(userId), any());
    }

    @Test
    @DisplayName("PUT /user/{id} should return 400 BAD_REQUEST when username already exists")
    void testUpdateUserWithDuplicateUsername() throws Exception {
        // Arrange
        Long userId = 1L;
        UserDto updateDto = UserDto.builder().username("taken_username").build();

        when(userService.updateUser(eq(userId), any()))
                .thenThrow(
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.BAD_REQUEST,
                                "username already exists"));

        // Act & Assert
        mockMvc.perform(
                        put("/user/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(userId), any());
    }

    @Test
    @DisplayName("PUT /user/{id} should return 400 BAD_REQUEST when email already exists")
    void testUpdateUserWithDuplicateEmail() throws Exception {
        // Arrange
        Long userId = 1L;
        UserDto updateDto = UserDto.builder().email("taken@example.com").build();

        when(userService.updateUser(eq(userId), any()))
                .thenThrow(
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.BAD_REQUEST,
                                "email already exists"));

        // Act & Assert
        mockMvc.perform(
                        put("/user/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(userId), any());
    }

    @Test
    @DisplayName("PUT /user/{id} should allow partial updates")
    void testUpdateUserPartialUpdate() throws Exception {
        // Arrange
        Long userId = 1L;
        UserDto updateDto = UserDto.builder().firstName("NewFirst").build();

        UserDto responseDto = UserDto.builder().id(userId).firstName("NewFirst").build();

        when(userService.updateUser(eq(userId), any())).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(
                        put("/user/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("NewFirst")));

        verify(userService, times(1)).updateUser(eq(userId), any());
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    @DisplayName("DELETE /user/{id} should delete user and return 204 NO_CONTENT")
    void testDeleteUserSuccess() throws Exception {
        // Arrange
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/user/{id}", userId)).andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("DELETE /user/{id} should return 404 NOT_FOUND when user not found")
    void testDeleteUserNotFound() throws Exception {
        // Arrange
        Long userId = 999L;

        doThrow(
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND, "user not found"))
                .when(userService)
                .deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/user/{id}", userId)).andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("DELETE /user/{id} should handle multiple delete requests")
    void testDeleteMultipleUsers() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(anyLong());

        // Act & Assert - Delete user 1
        mockMvc.perform(delete("/user/{id}", 1L)).andExpect(status().isNoContent());

        // Act & Assert - Delete user 2
        mockMvc.perform(delete("/user/{id}", 2L)).andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
        verify(userService, times(1)).deleteUser(2L);
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("POST /user should handle invalid request body gracefully")
    void testCreateUserWithInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /user/{id} should handle invalid ID format")
    void testGetUserWithInvalidIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/user/{id}", "invalid_id")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /user/{id} should handle invalid ID format")
    void testUpdateUserWithInvalidIdFormat() throws Exception {
        // Arrange
        UserDto updateDto = UserDto.builder().username("test").build();

        // Act & Assert
        mockMvc.perform(
                        put("/user/{id}", "invalid_id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        JsonObjectMapperUtil.getObjectMapper()
                                                .writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /user/{id} should handle invalid ID format")
    void testDeleteUserWithInvalidIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/user/{id}", "invalid_id")).andExpect(status().isBadRequest());
    }
}
