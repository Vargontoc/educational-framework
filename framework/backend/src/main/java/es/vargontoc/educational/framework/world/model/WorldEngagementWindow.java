package es.vargontoc.educational.framework.world.model;

import java.util.ArrayList;
import java.util.List;

public class WorldEngagementWindow {

    private Long childSessionId;
    private List<WorldEngineAbandonmentSignal> signals = new ArrayList<>();

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public List<WorldEngineAbandonmentSignal> getSignals() {
        return signals;
    }

    public void setSignals(List<WorldEngineAbandonmentSignal> signals) {
        this.signals = signals;
    }

    public void addSignal(WorldEngineAbandonmentSignal signal) {
        this.signals.add(signal);
    }
}
