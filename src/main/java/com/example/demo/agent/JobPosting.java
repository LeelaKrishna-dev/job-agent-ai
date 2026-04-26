package com.example.demo.agent;

import jakarta.validation.constraints.NotBlank;

public record JobPosting(
        @NotBlank String title,
        @NotBlank String url,
        @NotBlank String description
) {
}
