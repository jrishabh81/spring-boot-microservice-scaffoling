/* (C)2025 */
package com.rjain.spring_demo.hibernate.mapper;

import org.mapstruct.Mapper;

import com.rjain.spring_demo.hibernate.dto.UserDto;
import com.rjain.spring_demo.hibernate.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto dto);
}
