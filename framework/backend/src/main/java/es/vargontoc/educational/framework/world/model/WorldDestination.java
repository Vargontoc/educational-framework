package es.vargontoc.educational.framework.world.model;

import java.util.ArrayList;
import java.util.List;

public class WorldDestination {

    private String destinationId;
    private Long hostId;
    private String hostCode;
    private String hostDisplayName;
    private Long narrativeSituationId;
    private String narrativeSituationCode;
    private String displayText;
    private String biome;
    private List<WorldDiscoveryProposal> discoveryProposals = new ArrayList<>();

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public String getHostCode() {
        return hostCode;
    }

    public void setHostCode(String hostCode) {
        this.hostCode = hostCode;
    }

    public String getHostDisplayName() {
        return hostDisplayName;
    }

    public void setHostDisplayName(String hostDisplayName) {
        this.hostDisplayName = hostDisplayName;
    }

    public Long getNarrativeSituationId() {
        return narrativeSituationId;
    }

    public void setNarrativeSituationId(Long narrativeSituationId) {
        this.narrativeSituationId = narrativeSituationId;
    }

    public String getNarrativeSituationCode() {
        return narrativeSituationCode;
    }

    public void setNarrativeSituationCode(String narrativeSituationCode) {
        this.narrativeSituationCode = narrativeSituationCode;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getBiome() {
        return biome;
    }

    public void setBiome(String biome) {
        this.biome = biome;
    }

    public List<WorldDiscoveryProposal> getDiscoveryProposals() {
        return discoveryProposals;
    }

    public void setDiscoveryProposals(List<WorldDiscoveryProposal> discoveryProposals) {
        this.discoveryProposals = discoveryProposals;
    }
}
