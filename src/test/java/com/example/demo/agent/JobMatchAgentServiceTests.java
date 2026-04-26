package com.example.demo.agent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobMatchAgentServiceTests {

    private final JobMatchAgentService service = new JobMatchAgentService();

    @Test
    void returnsOnlyJobsThatMeetThresholdAndBuildsCsv() {
        String resume = "Java Spring Boot REST API microservices PostgreSQL Docker AWS CI/CD testing Agile";

        JobPosting strongMatch = new JobPosting(
                "Backend Engineer",
                "https://jobs.example.com/backend-1",
                "Build REST API using Java Spring Boot PostgreSQL Docker AWS and CI/CD"
        );

        JobPosting weakMatch = new JobPosting(
                "iOS Engineer",
                "https://jobs.example.com/ios-1",
                "Swift UIKit Xcode iOS mobile architecture"
        );

        AgentRequest request = new AgentRequest(resume, List.of(strongMatch, weakMatch));
        AgentResponse response = service.runAgent(request);

        assertEquals(1, response.matches().size());
        MatchedJobResult match = response.matches().get(0);
        assertEquals("Backend Engineer", match.title());
        assertTrue(match.matchScore() >= 75.0);
        assertTrue(match.atsScore() <= 98);
        assertTrue(response.csvReport().contains("Title,URL,MatchScore,ATSScore,TailoredResume"));
        assertTrue(response.csvReport().contains("https://jobs.example.com/backend-1"));
        assertFalse(response.csvReport().contains("https://jobs.example.com/ios-1"));
    }
}
