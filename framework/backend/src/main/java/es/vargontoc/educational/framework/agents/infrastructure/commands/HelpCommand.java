package es.vargontoc.educational.framework.agents.infrastructure.commands;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements ChatCommand {

    private final List<ChatCommand> commands;

    public HelpCommand(List<ChatCommand> commands) {
        this.commands = commands;
    }

    @Override
    public String getTrigger() {
        return "/help";
    }

    @Override
    public String getDescription() {
        return "Muestra la ayuda e información comandos disponibles";
    }

    @Override
    public String execute(String args) {
        return formatedHelp();
    }
    
    private String formatedHelp() {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            Bienvenido a la aplicación Nubi, aqui mostramos los comandos disponibles:

        """);

        commands.stream().forEach(c -> {
            sb.append(c.getTrigger()).append(" -> ").append(c.getDescription()).append("\n");
        });

        return sb.toString();
    }
}
