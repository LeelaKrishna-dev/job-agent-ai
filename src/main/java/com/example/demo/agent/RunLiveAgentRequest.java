package com.example.demo.agent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record RunLiveAgentRequest(
        @NotBlank String resume,
        @Valid JobSearchRequest search
) {
}
