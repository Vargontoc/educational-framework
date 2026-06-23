package es.vargontoc.educational.framework.world.model;

import java.math.BigDecimal;

public class WorldEnginePriorityAdjustment {

    private String engineType;
    private BigDecimal priorityMultiplier;
    private String reason;

    public WorldEnginePriorityAdjustment() {
    }

    public WorldEnginePriorityAdjustment(String engineType, BigDecimal priorityMultiplier, String reason) {
        this.engineType = engineType;
        this.priorityMultiplier = priorityMultiplier;
        this.reason = reason;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public BigDecimal getPriorityMultiplier() {
        return priorityMultiplier;
    }

    public void setPriorityMultiplier(BigDecimal priorityMultiplier) {
        this.priorityMultiplier = priorityMultiplier;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}