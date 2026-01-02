/* (C)2025 */
package com.rjain.spring_demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.rjain.spring_demo.hibernate.dto.UserDto;
import com.rjain.spring_demo.hibernate.entity.User;
import com.rjain.spring_demo.hibernate.mapper.UserMapper;
import com.rjain.spring_demo.hibernate.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto createUser(UserDto dto) {
        if (dto.getUsername() != null && userRepository.existsByUsername(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already exists");
        }
        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email already exists");
        }
        User entity = userMapper.toEntity(dto);
        // ensure id is null so JPA will generate
        entity.setId(null);
        User saved = userRepository.save(entity);
        return userMapper.toDto(saved);
    }

    public UserDto getUser(Long id) {
        return userRepository
                .findById(id)
                .map(userMapper::toDto)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    public List<UserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(Long id, UserDto dto) {
        User existing =
                userRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "user not found"));

        // check username/email uniqueness if changed
        if (dto.getUsername() != null && !dto.getUsername().equals(existing.getUsername())) {
            userRepository
                    .findByUsername(dto.getUsername())
                    .ifPresent(
                            u -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "username already exists");
                            });
            existing.setUsername(dto.getUsername());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
            userRepository
                    .findByEmail(dto.getEmail())
                    .ifPresent(
                            u -> {
                                throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "email already exists");
                            });
            existing.setEmail(dto.getEmail());
        }

        if (dto.getFirstName() != null) {
            existing.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            existing.setLastName(dto.getLastName());
        }
        if (dto.getActive() != null) {
            existing.setActive(dto.getActive());
        }

        User saved = userRepository.save(existing);
        return userMapper.toDto(saved);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found");
        }
        userRepository.deleteById(id);
    }
}
