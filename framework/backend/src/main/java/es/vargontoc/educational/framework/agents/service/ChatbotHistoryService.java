package es.vargontoc.educational.framework.agents.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.vargontoc.educational.framework.agents.infrastructure.commands.ChatCommand;
import es.vargontoc.educational.framework.agents.infrastructure.dto.CommandResponse;
import es.vargontoc.educational.framework.agents.infrastructure.dto.UpdateConversationTitleDto;
import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotMessage;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotHistoryUseCase;
import es.vargontoc.educational.framework.agents.ports.out.ChatbotConversationRepository;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import jakarta.validation.ValidationException;

@Service
@Transactional
public class ChatbotHistoryService implements ChatbotHistoryUseCase {
    
    private final ChatbotConversationRepository repository;
    private final List<ChatCommand> cmds;
    public ChatbotHistoryService(ChatbotConversationRepository repository, List<ChatCommand> cmds){
        this.repository = repository;
        this.cmds = cmds;
    }

    @Override
    public ChatbotConversation createConversation(Long familyId) {
        return repository.save(new ChatbotConversation(familyId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatbotConversation> getConversation(UUID conversationId, Long familyId) {
        Optional<ChatbotConversation> conversation = repository.findByConversationIdAndFamilyId(conversationId, familyId);
        conversation.ifPresent(c -> {
            List<ChatbotMessage> messages = repository.findMessagesByConversationId(c.getId());
            c.getMessages().addAll(messages);
        });
        return conversation;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatbotConversation> listConversations(Long familyId, int limit) {
        return repository.findByFamilyIdOrderByLastMessageAtDesc(familyId, limit);
    }


    @Override
    public ChatbotMessage addMessage(Long conversationId, String role, String content) {
        return repository.saveMessage(new ChatbotMessage(conversationId, role, content));
    }

    @Override
    public List<CommandResponse> getCommands() {
        return cmds.stream().map(this::toCommandResponse).toList();
    }

    private CommandResponse toCommandResponse(ChatCommand cmd){
        return new CommandResponse(cmd.getTrigger(), cmd.getDescription());
    }

    @Override
    public ChatbotConversation updateConversation(UpdateConversationTitleDto request, Long familyId) {
        if(request.conversation() == null)
            throw new ValidationException("Field UUID not found on request");
        
        var c = repository.findByConversationIdAndFamilyId(request.conversation(), familyId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        if(request.title() != null && !request.title().trim().isEmpty()){
            c.setTitle(request.title());
            return repository.save(c);
        }

        return c;
    }

    @Override
    public void deleteConversation(UUID conversation, Long familyId) {
        var c = repository.findByConversationIdAndFamilyId(conversation, familyId).orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        repository.delete(c.getId());
    }

    
}
