package com.example.demo.agent;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AgentRequest(
        @NotBlank String resume,
        @NotEmpty List<@Valid JobPosting> jobs,
        Integer postedWithinHours
) {
}
