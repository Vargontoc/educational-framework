package es.vargontoc.educational.framework.agents.model;

import java.util.UUID;

public record ChatbotStreamEvent(
    ChatbotEventType event,
    UUID conversationId,
    String content,
    Integer attempt,
    ToolCallInfo toolCallInfo
    
) {
    
    public static ChatbotStreamEvent token(UUID conversation, String token) {
        return new ChatbotStreamEvent(ChatbotEventType.TOKEN, conversation, token, null, null);
    }

    public static ChatbotStreamEvent complete(UUID conversation, String fullResponse) {
        return new ChatbotStreamEvent(ChatbotEventType.COMPLETE, conversation, fullResponse, null, null);
    }

    public static ChatbotStreamEvent error(UUID conversation, String errorMessage, int attempt){
        return new ChatbotStreamEvent(ChatbotEventType.ERROR, conversation, errorMessage, attempt, null);
    }

    public static ChatbotStreamEvent toolCallStart(UUID conversation, ToolCallInfo toolCall){
        return new ChatbotStreamEvent(ChatbotEventType.TOOL_CALL_START, conversation, null, null, toolCall);
    }

    public static ChatbotStreamEvent toolCallResult(UUID conversation, ToolCallInfo toolCall){
        return new ChatbotStreamEvent(ChatbotEventType.TOOL_CALL_RESULT, conversation, null, null, toolCall);
    }
}
