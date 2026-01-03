/* (C)2025 */
package com.rjain.spring_demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import com.rjain.spring_demo.hibernate.dto.UserDto;
import com.rjain.spring_demo.hibernate.entity.User;
import com.rjain.spring_demo.hibernate.mapper.UserMapper;
import com.rjain.spring_demo.hibernate.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private UserMapper userMapper;

    @InjectMocks private UserService userService;

    @BeforeEach
    void setUp() {
        // Mocks are automatically initialized by @ExtendWith(MockitoExtension.class)
        // and automatically injected via @InjectMocks
    }

    // ==================== CREATE USER TESTS ====================

    @Test
    @DisplayName("should create user successfully with valid data")
    void testCreateUserSuccess() {
        // Arrange
        UserDto inputDto =
                UserDto.builder()
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .build();

        User userEntity =
                User.builder()
                        .id(null)
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .build();

        User savedEntity =
                User.builder()
                        .id(1L)
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .createdAt(Instant.now())
                        .build();

        UserDto expectedDto =
                UserDto.builder()
                        .id(1L)
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .createdAt(Instant.now())
                        .build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userMapper.toEntity(inputDto)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedEntity);
        when(userMapper.toDto(savedEntity)).thenReturn(expectedDto);

        // Act
        UserDto result = userService.createUser(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john_doe", result.getUsername());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertTrue(result.getActive());

        verify(userRepository).existsByUsername("john_doe");
        verify(userRepository).existsByEmail("john@example.com");
        verify(userMapper).toEntity(inputDto);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(savedEntity);
    }

    @Test
    @DisplayName("should throw exception when username already exists")
    void testCreateUserWithDuplicateUsername() {
        // Arrange
        UserDto inputDto = UserDto.builder().username("john_doe").email("john@example.com").build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> userService.createUser(inputDto));

        assertEquals("username already exists", exception.getReason());
        verify(userRepository).existsByUsername("john_doe");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw exception when email already exists")
    void testCreateUserWithDuplicateEmail() {
        // Arrange
        UserDto inputDto = UserDto.builder().username("john_doe").email("john@example.com").build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> userService.createUser(inputDto));

        assertEquals("email already exists", exception.getReason());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should create user when username is null")
    void testCreateUserWithNullUsername() {
        // Arrange
        UserDto inputDto = UserDto.builder().username(null).email("john@example.com").build();

        User userEntity = new User();
        User savedEntity = User.builder().id(1L).build();
        UserDto expectedDto = UserDto.builder().id(1L).build();

        when(userMapper.toEntity(inputDto)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedEntity);
        when(userMapper.toDto(savedEntity)).thenReturn(expectedDto);

        // Act
        UserDto result = userService.createUser(inputDto);

        // Assert
        assertNotNull(result);
        verify(userRepository, never()).existsByUsername(any());
    }

    // ==================== GET USER TESTS ====================

    @Test
    @DisplayName("should get user by id successfully")
    void testGetUserSuccess() {
        // Arrange
        Long userId = 1L;
        User userEntity =
                User.builder()
                        .id(userId)
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .build();

        UserDto expectedDto =
                UserDto.builder()
                        .id(userId)
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(expectedDto);

        // Act
        UserDto result = userService.getUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("john_doe", result.getUsername());

        verify(userRepository).findById(userId);
        verify(userMapper).toDto(userEntity);
    }

    @Test
    @DisplayName("should throw exception when user not found")
    void testGetUserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> userService.getUser(userId));

        assertEquals("user not found", exception.getReason());
        verify(userRepository).findById(userId);
    }

    // ==================== LIST USERS TESTS ====================

    @Test
    @DisplayName("should list users with pagination")
    void testListUsersSuccess() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = User.builder().id(1L).username("user1").build();
        User user2 = User.builder().id(2L).username("user2").build();
        List<User> userList = List.of(user1, user2);
        Page<User> userPage = new PageImpl<>(userList, pageable, 2);

        UserDto dto1 = UserDto.builder().id(1L).username("user1").build();
        UserDto dto2 = UserDto.builder().id(2L).username("user2").build();

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        // Act
        List<UserDto> result = userService.listUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());

        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("should return empty list when no users exist")
    void testListUsersEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        List<UserDto> result = userService.listUsers(pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll(pageable);
    }

    // ==================== UPDATE USER TESTS ====================

    @Test
    @DisplayName("should update user successfully")
    void testUpdateUserSuccess() {
        // Arrange
        Long userId = 1L;
        User existingUser =
                User.builder()
                        .id(userId)
                        .username("old_username")
                        .email("old@example.com")
                        .firstName("Old")
                        .lastName("Name")
                        .active(true)
                        .build();

        UserDto updateDto =
                UserDto.builder()
                        .username("new_username")
                        .email("new@example.com")
                        .firstName("New")
                        .lastName("Name")
                        .active(false)
                        .build();

        User updatedUser =
                User.builder()
                        .id(userId)
                        .username("new_username")
                        .email("new@example.com")
                        .firstName("New")
                        .lastName("Name")
                        .active(false)
                        .build();

        UserDto expectedDto =
                UserDto.builder()
                        .id(userId)
                        .username("new_username")
                        .email("new@example.com")
                        .firstName("New")
                        .lastName("Name")
                        .active(false)
                        .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("new_username")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        // Act
        UserDto result = userService.updateUser(userId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("new_username", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertFalse(result.getActive());

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("should throw exception when updating user with duplicate username")
    void testUpdateUserWithDuplicateUsername() {
        // Arrange
        Long userId = 1L;
        User existingUser =
                User.builder().id(userId).username("old_username").email("old@example.com").build();

        UserDto updateDto = UserDto.builder().username("taken_username").build();

        User otherUser = User.builder().id(2L).username("taken_username").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("taken_username")).thenReturn(Optional.of(otherUser));

        // Act & Assert
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userService.updateUser(userId, updateDto));

        assertEquals("username already exists", exception.getReason());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw exception when updating user with duplicate email")
    void testUpdateUserWithDuplicateEmail() {
        // Arrange
        Long userId = 1L;
        User existingUser =
                User.builder().id(userId).username("john_doe").email("old@example.com").build();

        UserDto updateDto = UserDto.builder().email("taken@example.com").build();

        User otherUser = User.builder().id(2L).email("taken@example.com").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(otherUser));

        // Act & Assert
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userService.updateUser(userId, updateDto));

        assertEquals("email already exists", exception.getReason());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw exception when updating non-existent user")
    void testUpdateUserNotFound() {
        // Arrange
        Long userId = 999L;
        UserDto updateDto = UserDto.builder().username("new").build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> userService.updateUser(userId, updateDto));

        assertEquals("user not found", exception.getReason());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("should update only provided fields")
    void testUpdateUserPartialUpdate() {
        // Arrange
        Long userId = 1L;
        User existingUser =
                User.builder()
                        .id(userId)
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .active(true)
                        .build();

        UserDto updateDto = UserDto.builder().firstName("Johnny").build();

        User updatedUser =
                User.builder()
                        .id(userId)
                        .username("john_doe")
                        .email("john@example.com")
                        .firstName("Johnny")
                        .lastName("Doe")
                        .active(true)
                        .build();

        UserDto expectedDto = UserDto.builder().id(userId).firstName("Johnny").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expectedDto);

        // Act
        UserDto result = userService.updateUser(userId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Johnny", result.getFirstName());

        verify(userRepository).save(any(User.class));
    }

    // ==================== DELETE USER TESTS ====================

    @Test
    @DisplayName("should delete user successfully")
    void testDeleteUserSuccess() {
        // Arrange
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // Assert
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("should throw exception when deleting non-existent user")
    void testDeleteUserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> userService.deleteUser(userId));

        assertEquals("user not found", exception.getReason());
        verify(userRepository, never()).deleteById(userId);
    }
}
