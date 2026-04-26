package com.example.demo.agent;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobMatchAgentServiceTests {

    private static final Instant NOW = Instant.parse("2026-04-26T12:00:00Z");
    private final JobMatchAgentService service = new JobMatchAgentService(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void returnsOnlyJobsThatMeetThresholdAndBuildsCsv() {
        String resume = "Java Spring Boot REST API microservices PostgreSQL Docker AWS CI/CD testing Agile";

        JobPosting strongMatch = new JobPosting(
                "Backend Engineer",
                "https://jobs.example.com/backend-1",
                "Build REST API using Java Spring Boot PostgreSQL Docker AWS and CI/CD",
                NOW.minusSeconds(60 * 60),
                true,
                EmploymentType.FULL_TIME
        );

        JobPosting weakMatch = new JobPosting(
                "iOS Engineer",
                "https://jobs.example.com/ios-1",
                "Swift UIKit Xcode iOS mobile architecture",
                NOW.minusSeconds(60 * 20),
                true,
                EmploymentType.FULL_TIME
        );

        AgentRequest request = new AgentRequest(resume, List.of(strongMatch, weakMatch), 24);
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

    @Test
    void filtersByPostedWindowH1bAndFullTimeOnly() {
        String resume = "Java Spring Boot REST API microservices PostgreSQL Docker AWS CI/CD testing Agile";

        JobPosting oldPosting = new JobPosting(
                "Old Backend Role",
                "https://jobs.example.com/old",
                "Build REST API using Java Spring Boot PostgreSQL Docker AWS and CI/CD",
                NOW.minusSeconds(60 * 60 * 5),
                true,
                EmploymentType.FULL_TIME
        );

        JobPosting noSponsorship = new JobPosting(
                "No Sponsorship",
                "https://jobs.example.com/no-h1b",
                "Build REST API using Java Spring Boot PostgreSQL Docker AWS and CI/CD",
                NOW.minusSeconds(60 * 30),
                false,
                EmploymentType.FULL_TIME
        );

        JobPosting contractRole = new JobPosting(
                "Contract Backend",
                "https://jobs.example.com/contract",
                "Build REST API using Java Spring Boot PostgreSQL Docker AWS and CI/CD",
                NOW.minusSeconds(60 * 30),
                true,
                EmploymentType.CONTRACT
        );

        JobPosting eligible = new JobPosting(
                "Eligible Backend",
                "https://jobs.example.com/eligible",
                "Build REST API using Java Spring Boot PostgreSQL Docker AWS and CI/CD",
                NOW.minusSeconds(60 * 30),
                true,
                EmploymentType.FULL_TIME
        );

        AgentRequest request = new AgentRequest(resume, List.of(oldPosting, noSponsorship, contractRole, eligible), 4);

        AgentResponse response = service.runAgent(request);

        assertEquals(1, response.matches().size());
        assertEquals("Eligible Backend", response.matches().get(0).title());
    }

    @Test
    void rejectsUnsupportedPostedWindow() {
        String resume = "Java Spring Boot REST API";
        JobPosting posting = new JobPosting(
                "Backend Engineer",
                "https://jobs.example.com/backend-1",
                "Build REST API using Java Spring Boot",
                NOW,
                true,
                EmploymentType.FULL_TIME
        );

        AgentRequest request = new AgentRequest(resume, List.of(posting), 2);

        assertThrows(IllegalArgumentException.class, () -> service.runAgent(request));
    }
}
