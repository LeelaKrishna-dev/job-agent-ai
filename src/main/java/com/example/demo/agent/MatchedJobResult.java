package com.example.demo.agent;

public record MatchedJobResult(
        String title,
        String url,
        double matchScore,
        int atsScore,
        String tailoredResume
) {
}
