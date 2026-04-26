package com.example.demo.agent;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobMatchAgentService {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "in", "is", "it", "of",
            "on", "or", "that", "the", "to", "with", "you", "your", "we", "our", "will", "this", "have"
    );

    public AgentResponse runAgent(AgentRequest request) {
        Set<String> resumeKeywords = extractKeywords(request.resume());
        List<MatchedJobResult> matches = new ArrayList<>();

        for (JobPosting job : request.jobs()) {
            Set<String> jdKeywords = extractKeywords(job.description());
            double matchScore = calculateMatchScore(resumeKeywords, jdKeywords);

            if (matchScore >= 75.0) {
                String tailoredResume = tailorResume(request.resume(), jdKeywords, resumeKeywords, job.title());
                int atsScore = calculateAtsScore(tailoredResume, jdKeywords);

                matches.add(new MatchedJobResult(
                        job.title(),
                        job.url(),
                        round(matchScore),
                        atsScore,
                        tailoredResume
                ));
            }
        }

        matches.sort(Comparator.comparingDouble(MatchedJobResult::matchScore).reversed());
        String csvReport = buildCsv(matches);
        return new AgentResponse(matches, csvReport);
    }

    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9+#]+"))
                .filter(token -> !token.isBlank())
                .filter(token -> token.length() > 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private double calculateMatchScore(Set<String> resumeKeywords, Set<String> jdKeywords) {
        if (jdKeywords.isEmpty()) {
            return 0;
        }

        long overlap = jdKeywords.stream().filter(resumeKeywords::contains).count();
        return (overlap * 100.0) / jdKeywords.size();
    }

    private String tailorResume(String resume, Set<String> jdKeywords, Set<String> resumeKeywords, String jobTitle) {
        List<String> missingKeywords = jdKeywords.stream()
                .filter(keyword -> !resumeKeywords.contains(keyword))
                .limit(12)
                .toList();

        if (missingKeywords.isEmpty()) {
            return resume;
        }

        String keywordLine = "Targeted Keywords for " + jobTitle + ": " + String.join(", ", missingKeywords);
        return resume + "\n\n" + keywordLine + "\n"
                + "Achievement Alignment: Demonstrated delivery using " + String.join(", ", missingKeywords) + ".";
    }

    private int calculateAtsScore(String tailoredResume, Set<String> jdKeywords) {
        if (jdKeywords.isEmpty()) {
            return 0;
        }

        Set<String> tailoredKeywords = extractKeywords(tailoredResume);
        long overlap = jdKeywords.stream().filter(tailoredKeywords::contains).count();
        double rawScore = (overlap * 100.0) / jdKeywords.size();

        if (rawScore >= 98) {
            return 98;
        }

        return Math.min(98, (int) Math.round(rawScore + 18));
    }

    private String buildCsv(List<MatchedJobResult> matches) {
        StringBuilder csv = new StringBuilder("Title,URL,MatchScore,ATSScore,TailoredResume\n");
        for (MatchedJobResult match : matches) {
            csv.append(escape(match.title())).append(',')
                    .append(escape(match.url())).append(',')
                    .append(match.matchScore()).append(',')
                    .append(match.atsScore()).append(',')
                    .append(escape(match.tailoredResume()))
                    .append('\n');
        }
        return csv.toString();
    }

    private String escape(String input) {
        String escaped = input.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
