package es.vargontoc.educational.framework.family.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "family")
public class FamilyJpaEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "pin_hash", nullable = false, length = 255)
    private String pinHash;

    @Column(name = "audio_general_enabled", nullable = false)
    private boolean audioGeneralEnabled = true;

    @Column(name = "audio_general_volume", nullable = false)
    private int audioGeneralVolume = 100;

    @Column(name = "npc_enabled", nullable = false)
    private boolean npcEnabled = true;

    @Column(name = "npc_voice_enabled", nullable = false)
    private boolean npcVoiceEnabled = true;

    @Column(name = "npc_voice_volume", nullable = false)
    private int npcVoiceVolume = 100;

    @Column(name = "narrative_voice_enabled", nullable = false)
    private boolean narrativeVoiceEnabled = true;

    @Column(name = "narrative_voice_volume", nullable = false)
    private int narrativeVoiceVolume = 100;

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
}
