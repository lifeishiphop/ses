package com.web.back.utils;

import java.util.Collections;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseManager {
    public Map<String, String> getErrorResponse(Exception e){
        return Collections.singletonMap("error - ", e.getMessage());
    }
}