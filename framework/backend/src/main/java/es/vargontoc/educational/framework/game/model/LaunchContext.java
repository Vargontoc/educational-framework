package es.vargontoc.educational.framework.game.model;

public class LaunchContext {

    private String worldHostId;
    private String habitatTag;
    private String discoveryElementId;
    private String narrativeContextId;

    public LaunchContext() {
    }

    public LaunchContext(String worldHostId, String habitatTag, String discoveryElementId, String narrativeContextId) {
        this.worldHostId = worldHostId;
        this.habitatTag = habitatTag;
        this.discoveryElementId = discoveryElementId;
        this.narrativeContextId = narrativeContextId;
    }

    public String getWorldHostId() {
        return worldHostId;
    }

    public void setWorldHostId(String worldHostId) {
        this.worldHostId = worldHostId;
    }

    public String getHabitatTag() {
        return habitatTag;
    }

    public void setHabitatTag(String habitatTag) {
        this.habitatTag = habitatTag;
    }

    public String getDiscoveryElementId() {
        return discoveryElementId;
    }

    public void setDiscoveryElementId(String discoveryElementId) {
        this.discoveryElementId = discoveryElementId;
    }

    public String getNarrativeContextId() {
        return narrativeContextId;
    }

    public void setNarrativeContextId(String narrativeContextId) {
        this.narrativeContextId = narrativeContextId;
    }
}
