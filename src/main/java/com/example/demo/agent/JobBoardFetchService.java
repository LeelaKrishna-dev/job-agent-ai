package com.example.demo.agent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class JobBoardFetchService {

    private static final Set<String> TARGET_SITES = Set.of(
            "linkedin.com/jobs",
            "indeed.com",
            "glassdoor.com/Job",
            "myworkdayjobs.com"
    );

    public List<JobPosting> fetchJobs(JobSearchRequest request) {
        int maxResults = request.maxResults() == null ? 25 : Math.max(1, Math.min(100, request.maxResults()));
        int postedWithinHours = request.postedWithinHours() == null ? 24 : request.postedWithinHours();

        Map<String, JobPosting> deduped = new LinkedHashMap<>();
        for (String site : TARGET_SITES) {
            String query = buildQuery(request.keywords(), request.location(), postedWithinHours, site);
            try {
                Document document = search(query);
                for (JobPosting posting : parse(document)) {
                    deduped.putIfAbsent(posting.url(), posting);
                    if (deduped.size() >= maxResults) {
                        break;
                    }
                }
                if (deduped.size() >= maxResults) {
                    break;
                }
            } catch (IOException ignored) {
                // Continue on partial source outages.
            }
        }

        return new ArrayList<>(deduped.values());
    }

    Document search(String query) throws IOException {
        String url = UriComponentsBuilder.fromHttpUrl("https://html.duckduckgo.com/html/")
                .queryParam("q", query)
                .build()
                .toUriString();

        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(15000)
                .get();
    }

    List<JobPosting> parse(Document document) {
        List<JobPosting> postings = new ArrayList<>();

        for (Element result : document.select("div.result")) {
            Element titleLink = result.selectFirst("a.result__a");
            if (titleLink == null) {
                continue;
            }

            String rawUrl = titleLink.attr("href");
            String resolvedUrl = resolveDuckDuckGoRedirect(rawUrl);
            String title = titleLink.text();
            String snippet = result.select("a.result__snippet,div.result__snippet").text();

            if (!isTargetBoard(resolvedUrl)) {
                continue;
            }

            postings.add(new JobPosting(
                    title,
                    resolvedUrl,
                    snippet.isBlank() ? title : snippet,
                    Instant.now(),
                    inferH1bSponsorship(snippet),
                    inferEmploymentType(snippet)
            ));
        }

        return postings;
    }

    private String buildQuery(String keywords, String location, int postedWithinHours, String site) {
        return "%s %s H1B sponsorship full time posted in last %d hours site:%s"
                .formatted(keywords, location, postedWithinHours, site);
    }

    private boolean isTargetBoard(String url) {
        String normalized = url.toLowerCase(Locale.ROOT);
        return TARGET_SITES.stream().anyMatch(normalized::contains);
    }

    private String resolveDuckDuckGoRedirect(String rawUrl) {
        if (!rawUrl.contains("uddg=")) {
            return rawUrl;
        }

        String encoded = rawUrl.substring(rawUrl.indexOf("uddg=") + 5);
        int amp = encoded.indexOf('&');
        if (amp >= 0) {
            encoded = encoded.substring(0, amp);
        }

        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }

    private boolean inferH1bSponsorship(String snippet) {
        String text = snippet.toLowerCase(Locale.ROOT);
        if (text.contains("no sponsorship") || text.contains("without sponsorship")) {
            return false;
        }

        return text.contains("h1b") || text.contains("visa") || text.contains("sponsorship") || snippet.isBlank();
    }

    private EmploymentType inferEmploymentType(String snippet) {
        String text = snippet.toLowerCase(Locale.ROOT);
        if (text.contains("contract") || text.contains("contractor")) {
            return EmploymentType.CONTRACT;
        }
        if (text.contains("w2")) {
            return EmploymentType.W2;
        }
        if (text.contains("part time") || text.contains("part-time")) {
            return EmploymentType.PART_TIME;
        }

        return EmploymentType.FULL_TIME;
    }
}
