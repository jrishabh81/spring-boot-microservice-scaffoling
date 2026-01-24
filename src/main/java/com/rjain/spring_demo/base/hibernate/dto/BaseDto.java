/* (C)2026 */
package com.rjain.spring_demo.base.hibernate.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseDto {
    private Long id;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
}
