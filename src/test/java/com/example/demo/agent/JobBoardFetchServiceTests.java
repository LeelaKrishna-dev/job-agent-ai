package com.example.demo.agent;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobBoardFetchServiceTests {

    private final JobBoardFetchService service = new JobBoardFetchService();

    @Test
    void parsesDuckDuckGoResultsForSupportedBoards() {
        String html = """
                <html><body>
                  <div class=\"result\">
                    <a class=\"result__a\" href=\"https://duckduckgo.com/l/?uddg=https%3A%2F%2Fwww.linkedin.com%2Fjobs%2Fview%2F123\">Backend Engineer</a>
                    <a class=\"result__snippet\">Full time role with H1B sponsorship available</a>
                  </div>
                  <div class=\"result\">
                    <a class=\"result__a\" href=\"https://example.com/not-a-board\">Random</a>
                    <a class=\"result__snippet\">Ignore this</a>
                  </div>
                </body></html>
                """;

        Document doc = Jsoup.parse(html);
        List<JobPosting> postings = service.parse(doc);

        assertEquals(1, postings.size());
        JobPosting posting = postings.get(0);
        assertEquals("Backend Engineer", posting.title());
        assertTrue(posting.url().contains("linkedin.com/jobs/view/123"));
        assertTrue(posting.h1bSponsorship());
        assertEquals(EmploymentType.FULL_TIME, posting.employmentType());
    }
}
