package es.vargontoc.educational.framework.family.model;

import java.time.LocalDateTime;

public class Family {

    private Long id;
    private String name;
    private String pinHash;
    private boolean audioGeneralEnabled = true;
    private int audioGeneralVolume = 100;
    private boolean npcEnabled = true;
    private boolean npcVoiceEnabled = true;
    private int npcVoiceVolume = 100;
    private boolean narrativeVoiceEnabled = true;
    private int narrativeVoiceVolume = 100;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }
    
    public boolean isAudioGeneralEnabled() {
        return audioGeneralEnabled;
    }

    public void setAudioGeneralEnabled(boolean audioGeneralEnabled) {
        this.audioGeneralEnabled = audioGeneralEnabled;
    }

    public int getAudioGeneralVolume() {
        return audioGeneralVolume;
    }

    public void setAudioGeneralVolume(int audioGeneralVolume) {
        this.audioGeneralVolume = audioGeneralVolume;
    }

    public boolean isNpcEnabled() {
        return npcEnabled;
    }

    public void setNpcEnabled(boolean npcEnabled) {
        this.npcEnabled = npcEnabled;
    }

    public boolean isNpcVoiceEnabled() {
        return npcVoiceEnabled;
    }

    public void setNpcVoiceEnabled(boolean npcVoiceEnabled) {
        this.npcVoiceEnabled = npcVoiceEnabled;
    }

    public int getNpcVoiceVolume() {
        return npcVoiceVolume;
    }

    public void setNpcVoiceVolume(int npcVoiceVolume) {
        this.npcVoiceVolume = npcVoiceVolume;
    }

    public boolean isNarrativeVoiceEnabled() {
        return narrativeVoiceEnabled;
    }

    public void setNarrativeVoiceEnabled(boolean narrativeVoiceEnabled) {
        this.narrativeVoiceEnabled = narrativeVoiceEnabled;
    }

    public int getNarrativeVoiceVolume() {
        return narrativeVoiceVolume;
    }

    public void setNarrativeVoiceVolume(int narrativeVoiceVolume) {
        this.narrativeVoiceVolume = narrativeVoiceVolume;
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
