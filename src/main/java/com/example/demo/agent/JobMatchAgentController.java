package com.example.demo.agent;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class JobMatchAgentController {

    private final JobMatchAgentService jobMatchAgentService;

    public JobMatchAgentController(JobMatchAgentService jobMatchAgentService) {
        this.jobMatchAgentService = jobMatchAgentService;
    }

    @PostMapping("/run")
    public AgentResponse run(@Valid @RequestBody AgentRequest request) {
        return jobMatchAgentService.runAgent(request);
    }
}
