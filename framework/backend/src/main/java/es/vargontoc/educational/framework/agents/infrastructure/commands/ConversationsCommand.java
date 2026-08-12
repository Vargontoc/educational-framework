package es.vargontoc.educational.framework.agents.infrastructure.commands;

import org.springframework.stereotype.Component;

@Component
public class ConversationsCommand implements ChatCommand {

    @Override
    public String getTrigger() {
        return "/conversations";
    }

    @Override
    public String getDescription() {
        return "Muestra lista de conversaciones";
    }

    @Override
    public String execute(String args) {
        return "";
    }
    
}
