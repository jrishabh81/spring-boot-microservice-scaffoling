package com.rjain.spring_demo.hibernate.mapper;

import com.rjain.spring_demo.hibernate.dto.UserDto;
import com.rjain.spring_demo.hibernate.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto dto);
}
