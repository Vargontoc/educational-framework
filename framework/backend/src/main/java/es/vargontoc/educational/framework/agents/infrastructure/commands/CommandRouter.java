package es.vargontoc.educational.framework.agents.infrastructure.commands;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class CommandRouter {
    
    private final Map<String, ChatCommand> commands;

    public CommandRouter(List<ChatCommand> commandList) {
        this.commands = commandList.stream().collect(Collectors.toMap(ChatCommand::getTrigger, cmd -> cmd));
    }

    public Optional<String> route(String prompt) {
        String trimmed = prompt.trim();

        if(!trimmed.startsWith("/")){
            return Optional.empty();
        }

        String[] parts = trimmed.split("\\s+", 2);
        String trigger = parts[0].toLowerCase();
        String arguments = parts.length > 1 ? parts[1] : "";

        ChatCommand command = commands.get(trigger);
        if(command != null)
            return Optional.of(command.execute(arguments));
        return Optional.of("Comando no reconocido. Escribe /help para ver la lista");
    }
}
