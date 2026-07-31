package es.vargontoc.educational.framework.family.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChildProfile {

    private Long id;
    private Long familyId;
    private String name;
    private boolean active;
    private LocalDate birthday;
    private String avatar;
    private boolean npcVoiceEnabled;
    private boolean npcEnabled;
    private int npcVoiceVolume = 100;
    private ColorVisionMode colorVisionMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isNpcVoiceEnabled() {
        return npcVoiceEnabled;
    }

    public void setNpcVoiceEnabled(boolean npcVoiceEnabled) {
        this.npcVoiceEnabled = npcVoiceEnabled;
    }

    public boolean isNpcEnabled() {
        return npcEnabled;
    }

    public void setNpcEnabled(boolean npcEnabled) {
        this.npcEnabled = npcEnabled;
    }

    public int getNpcVoiceVolume() {
        return npcVoiceVolume;
    }

    public void setNpcVoiceVolume(int npcVoiceVolume) {
        this.npcVoiceVolume = npcVoiceVolume;
    }

    public ColorVisionMode getColorVisionMode() {
        return colorVisionMode;
    }

    public void setColorVisionMode(ColorVisionMode colorVisionMode) {
        this.colorVisionMode = colorVisionMode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
