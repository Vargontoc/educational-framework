package es.vargontoc.educational.framework.agents.infrastructure.tools;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import es.vargontoc.educational.framework.content.ports.out.StoryCatalogPort;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.AbandonmentSignalResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DiaryActivityResponse;
import es.vargontoc.educational.framework.tracking.infrastructure.dto.DiarySummaryResponse;
import es.vargontoc.educational.framework.tracking.model.DiaryPeriod;
import es.vargontoc.educational.framework.tracking.service.DiaryService;

@Component
public class ChatbotTools {

    private final ChildProfileUseCase childProfile;
    private final StoryCatalogPort catalog;
    private final DiaryService diaryService;

    public ChatbotTools(ChildProfileUseCase childProfile, StoryCatalogPort catalog, DiaryService diaryService) {
        this.childProfile = childProfile;
        this.catalog = catalog;
        this.diaryService = diaryService;
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

    // ========== DIARIO PARENTAL TOOLS ==========

    @Tool(name = "getDiarySummary", description = "Obtiene el resumen del diario parental de un niño: tiempo jugado y actividades únicas completadas en un periodo. Si no se especifica perfil, resuelve automáticamente si solo hay uno.")
    String getDiarySummary(
            @ToolParam(description = "Nombre del perfil infantil. Si está vacío o null, se resuelve automáticamente si solo hay un perfil.") String childName,
            @ToolParam(description = "Periodo: TODAY, WEEK, MONTH o ALL. Por defecto WEEK.") String period) {
        
        ProfileResolution resolution = resolveProfile(childName);
        if (resolution.message != null) {
            return resolution.message;
        }

        DiaryPeriod diaryPeriod = parsePeriod(period);
        DiarySummaryResponse summary = diaryService.getSummary(resolution.profile.getId(), diaryPeriod);

        return String.format(
            "Resumen del diario de %s (%s): Tiempo jugado: %d minutos. Actividades únicas completadas: %d.",
            resolution.profile.getName(),
            periodLabel(diaryPeriod),
            summary.getPlayedTimeMinutes(),
            summary.getUniqueActivitiesCompleted()
        );
    }

    @Tool(name = "getDiaryActivities", description = "Obtiene la lista de actividades completadas por un niño en el diario parental, con categoría y nivel actual de dificultad. Si no se especifica perfil, resuelve automáticamente si solo hay uno.")
    String getDiaryActivities(
            @ToolParam(description = "Nombre del perfil infantil. Si está vacío o null, se resuelve automáticamente si solo hay un perfil.") String childName,
            @ToolParam(description = "Periodo: TODAY, WEEK, MONTH o ALL. Por defecto WEEK.") String period) {
        
        ProfileResolution resolution = resolveProfile(childName);
        if (resolution.message != null) {
            return resolution.message;
        }

        DiaryPeriod diaryPeriod = parsePeriod(period);
        List<DiaryActivityResponse> activities = diaryService.getActivities(resolution.profile.getId(), diaryPeriod);

        if (activities.isEmpty()) {
            return String.format(
                "No hay actividades registradas para %s en el periodo %s.",
                resolution.profile.getName(),
                periodLabel(diaryPeriod)
            );
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Actividades de %s (%s):\n", resolution.profile.getName(), periodLabel(diaryPeriod)));
        
        String currentCategory = null;
        for (DiaryActivityResponse activity : activities) {
            if (!activity.getCategory().equals(currentCategory)) {
                currentCategory = activity.getCategory();
                sb.append(String.format("\n[%s]\n", categoryLabel(currentCategory)));
            }
            String subcat = activity.getSubcategory() != null ? String.format(" (%s)", activity.getSubcategory()) : "";
            sb.append(String.format("  • %s%s — Nivel actual: %s\n",
                activity.getName(),
                subcat,
                difficultyLabel(activity.getCurrentDifficulty())
            ));
        }

        return sb.toString().trim();
    }

    @Tool(name = "getDiaryAbandonmentSignal", description = "Obtiene la señal de abandono de una actividad específica del diario parental. Solo devuelve datos si hay 4 o más abandonos en los últimos 6 intentos iniciales. Si no se especifica perfil, resuelve automáticamente si solo hay uno.")
    String getDiaryAbandonmentSignal(
            @ToolParam(description = "Nombre del perfil infantil. Si está vacío o null, se resuelve automáticamente si solo hay un perfil.") String childName,
            @ToolParam(description = "Nombre de la actividad para consultar la señal de abandono.") String activityName) {
        
        ProfileResolution resolution = resolveProfile(childName);
        if (resolution.message != null) {
            return resolution.message;
        }

        // Buscar el activityId por nombre
        List<DiaryActivityResponse> allActivities = diaryService.getActivities(resolution.profile.getId(), DiaryPeriod.ALL);
        DiaryActivityResponse matchedActivity = allActivities.stream()
            .filter(a -> a.getName() != null && a.getName().equalsIgnoreCase(activityName))
            .findFirst()
            .orElse(null);

        if (matchedActivity == null) {
            return String.format(
                "No se encontró la actividad '%s' en el diario de %s.",
                activityName,
                resolution.profile.getName()
            );
        }

        AbandonmentSignalResponse signal = diaryService.getAbandonmentSignal(
            resolution.profile.getId(), matchedActivity.getActivityId());

        if (signal == null) {
            return String.format(
                "No hay señal de abandono significativa para '%s' en el diario de %s.",
                activityName,
                resolution.profile.getName()
            );
        }

        return String.format(
            "Señal de abandono para '%s' en el diario de %s: %d abandonos en los últimos 6 intentos iniciales.",
            activityName,
            resolution.profile.getName(),
            signal.getAbandonmentCount()
        );
    }

    // ========== PROFILE RESOLUTION ==========

    private record ProfileResolution(ChildProfile profile, String message) {}

    private ProfileResolution resolveProfile(String childName) {
        List<ChildProfile> profiles = childProfile.getAllChildren();

        if (profiles.isEmpty()) {
            return new ProfileResolution(null, "No hay perfiles infantiles registrados en la aplicación.");
        }

        if (childName == null || childName.trim().isEmpty()) {
            if (profiles.size() == 1) {
                return new ProfileResolution(profiles.get(0), null);
            } else {
                String names = profiles.stream().map(ChildProfile::getName).collect(Collectors.joining(", "));
                return new ProfileResolution(null, 
                    String.format("Hay varios perfiles registrados (%s). ¿Sobre cuál quieres consultar?", names));
            }
        }

        ChildProfile matched = profiles.stream()
            .filter(c -> c.getName().equalsIgnoreCase(childName.trim()))
            .findFirst()
            .orElse(null);

        if (matched == null) {
            String names = profiles.stream().map(ChildProfile::getName).collect(Collectors.joining(", "));
            return new ProfileResolution(null, 
                String.format("No se encontró el perfil '%s'. Perfiles disponibles: %s.", childName, names));
        }

        return new ProfileResolution(matched, null);
    }

    // ========== HELPERS ==========

    private DiaryPeriod parsePeriod(String period) {
        if (period == null || period.trim().isEmpty()) {
            return DiaryPeriod.WEEK;
        }
        try {
            return DiaryPeriod.valueOf(period.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return DiaryPeriod.WEEK;
        }
    }

    private String periodLabel(DiaryPeriod period) {
        return switch (period) {
            case TODAY -> "hoy";
            case WEEK -> "esta semana";
            case MONTH -> "este mes";
            case ALL -> "todo el historial";
        };
    }

    private String categoryLabel(String category) {
        return switch (category) {
            case "RECOGNITION" -> "Reconocimiento";
            case "COMPARISON" -> "Comparación";
            case "MEMORY" -> "Memoria";
            default -> category;
        };
    }

    private String difficultyLabel(String difficulty) {
        if (difficulty == null) return "Fácil";
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> "Fácil";
            case "MEDIUM" -> "Normal";
            case "HARD" -> "Difícil";
            default -> difficulty;
        };
    }
}
