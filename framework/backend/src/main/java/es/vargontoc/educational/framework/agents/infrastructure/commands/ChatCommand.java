package es.vargontoc.educational.framework.agents.infrastructure.commands;

public interface ChatCommand {
    
    String getTrigger();
    String getDescription();
    String execute(String args);
}
