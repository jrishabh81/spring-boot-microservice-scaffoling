/* (C)2026 */
package com.rjain.spring_demo.base.hibernate.mapper;

import org.springframework.data.domain.Page;

import com.rjain.spring_demo.base.hibernate.dto.BaseDto;
import com.rjain.spring_demo.base.hibernate.entity.BaseEntity;

public interface BaseMapper<BASE_DTO extends BaseDto, BASE_ENTITY extends BaseEntity> {
    BASE_DTO toDto(BASE_ENTITY entity);

    BASE_ENTITY toEntity(BASE_DTO dto);

    default Page<BASE_DTO> toDtoPage(Page<BASE_ENTITY> entityPage) {
        return entityPage.map(this::toDto);
    }

    default Page<BASE_ENTITY> toEntityPage(Page<BASE_DTO> dtoPage) {
        return dtoPage.map(this::toEntity);
    }
}
