package es.vargontoc.educational.framework.agents.model;

import java.time.LocalDateTime;

public record ToolCallInfo(
    String toolName,
    String parameters,
    String status,
    String summary,
    Long durationMs,
    LocalDateTime timestamp
) {
    public static ToolCallInfo start(String toolName, String parameters){
        return new ToolCallInfo(toolName, parameters, "STARTED", null, null, LocalDateTime.now());
    }

    public static ToolCallInfo result(String toolName, String status, String summary, Long durationMs){
        return new ToolCallInfo(toolName, null, status, summary, durationMs, LocalDateTime.now());
    }
}
