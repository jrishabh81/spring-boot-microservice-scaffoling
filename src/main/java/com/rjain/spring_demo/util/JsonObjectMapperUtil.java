package com.rjain.spring_demo.util;

import lombok.NoArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JsonObjectMapperUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
