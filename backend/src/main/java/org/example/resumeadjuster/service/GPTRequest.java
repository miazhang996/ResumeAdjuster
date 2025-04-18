package org.example.resumeadjuster.service;

import java.util.List;
import java.util.Map;

public class GPTRequest {
    public String model = "gpt-3.5-turbo";
    public List<Map<String, String>> messages;

    public GPTRequest(String prompt) {
        this.messages = List.of(Map.of("role", "user", "content", prompt));
    }
}
