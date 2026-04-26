package com.example.demo.agent;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
public class JobMatchAgentController {

    private final JobMatchAgentService jobMatchAgentService;
    private final JobBoardFetchService jobBoardFetchService;

    public JobMatchAgentController(JobMatchAgentService jobMatchAgentService, JobBoardFetchService jobBoardFetchService) {
        this.jobMatchAgentService = jobMatchAgentService;
        this.jobBoardFetchService = jobBoardFetchService;
    }

    @PostMapping("/run")
    public AgentResponse run(@Valid @RequestBody AgentRequest request) {
        return jobMatchAgentService.runAgent(request);
    }

    @PostMapping("/fetch")
    public List<JobPosting> fetch(@Valid @RequestBody JobSearchRequest request) {
        return jobBoardFetchService.fetchJobs(request);
    }

    @PostMapping("/run-live")
    public AgentResponse runLive(@Valid @RequestBody RunLiveAgentRequest request) {
        List<JobPosting> jobs = jobBoardFetchService.fetchJobs(request.search());
        AgentRequest agentRequest = new AgentRequest(request.resume(), jobs, request.search().postedWithinHours());
        return jobMatchAgentService.runAgent(agentRequest);
    }
}
