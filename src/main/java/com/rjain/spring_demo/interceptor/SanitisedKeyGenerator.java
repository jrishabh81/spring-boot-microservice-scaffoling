/* (C)2025 */
package com.rjain.spring_demo.interceptor;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

@Component
public class SanitisedKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, @Nullable Object... params) {
        return params.length == 0
                ? "defaultKey"
                : StringUtils.normalizeSpace(String.valueOf(params[0]));
    }
}
