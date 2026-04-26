package com.example.demo.agent;

import java.util.List;

public record AgentResponse(
        List<MatchedJobResult> matches,
        String csvReport
) {
}
