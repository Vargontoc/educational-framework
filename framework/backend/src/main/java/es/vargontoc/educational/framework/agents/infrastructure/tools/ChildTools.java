package es.vargontoc.educational.framework.agents.infrastructure.tools;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;

@Component
public class ChildTools {
    
    private static final Logger LOG = LoggerFactory.getLogger(ChildTools.class);

    private final ChildProfileUseCase childProfile;

    public ChildTools(ChildProfileUseCase childProfile) {
        this.childProfile = childProfile;
    }

    @Tool(name = "getAllChilds", description = "Obtiene los perfiles registrados en la aplicacion")
    List<String> getAllChilds(){
        return childProfile.getAllChildren().stream().map(ChildProfile::getName).collect(Collectors.toList());
    }

    @Tool(name = "getChild", description = "Obtiene un perfil registrado en la aplicación")
    ChildProfile getChild(@ToolParam(description = "Nombre del perfil registrado") String name) {
        return childProfile.getAllChildren().stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
