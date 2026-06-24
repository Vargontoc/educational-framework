package es.vargontoc.educational.framework.world.infrastructure.websocket.dto;

import java.util.List;

public record WorldDestinationPayload(
    String destinationId,
    WorldHostPayload host,
    WorldNarrativeSituationPayload narrativeSituation,
    String biome,
    List<WorldDiscoveryElementPayload> discoveryElements
) {
}