package com.example.demo.agent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record JobPosting(
        @NotBlank String title,
        @NotBlank String url,
        @NotBlank String description,
        @NotNull Instant postedAt,
        boolean h1bSponsorship,
        @NotNull EmploymentType employmentType
) {
}
