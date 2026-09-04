package es.vargontoc.educational.framework.agents.infrastructure.tools;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.content.ports.out.StoryCatalogPort;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;

@Component
public class ChatbotTools {

    private final ChildProfileUseCase childProfile;
    private final StoryCatalogPort catalog;
    public ChatbotTools(ChildProfileUseCase childProfile, StoryCatalogPort catalog) {
        this.childProfile = childProfile;
        this.catalog = catalog;
    }

    @Tool(name = "getAllChilds", description = "Obtiene los perfiles registrados en la aplicacion")
    List<String> getAllChilds(){
        return childProfile.getAllChildren().stream().map(c -> c.getName()).collect(Collectors.toList());
    }

    @Tool(name = "getChild", description = "Obtiene un perfil registrado en la aplicación")
    ChildProfile getChild(@ToolParam(description = "Nombre del perfil registrado") String name) {
        return childProfile.getAllChildren().stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Tool(name = "countStories", description = "Obtiene el numero de cuentos registrados en la app")
    int countCatalog() {
        return catalog.loadCatalog().size();
    }
}
