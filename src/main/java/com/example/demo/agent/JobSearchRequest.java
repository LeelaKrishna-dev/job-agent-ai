package com.example.demo.agent;

import jakarta.validation.constraints.NotBlank;

public record JobSearchRequest(
        @NotBlank String keywords,
        @NotBlank String location,
        Integer postedWithinHours,
        Integer maxResults
) {
}
