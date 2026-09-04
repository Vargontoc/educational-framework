package es.vargontoc.educational.framework.agents.infrastructure.commands;

import org.springframework.stereotype.Component;

@Component
public class NewCommand implements ChatCommand{

    @Override
    public String getTrigger() {
        return "/new";
    }

    @Override
    public String getDescription() {
        return "Abre una nueva conversación";
    }

    @Override
    public String execute(String args) {
        return "";
    
    }
    
}
