package es.vargontoc.educational.framework.family.infrastructure.persistence;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "child_profile")
public class ChildProfileJpaEntity extends BaseEntity {

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false, length = 100)
    private String avatar;

    @Column(name = "npc_voice_enabled", nullable = false)
    private boolean npcVoiceEnabled;

    @Column(name = "npc_enabled", nullable = false)
    private boolean npcEnabled;

    @Column(name = "npc_voice_volume", nullable = false)
    private int npcVoiceVolume = 100;

    @Column(name = "color_vision_mode", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ColorVisionMode colorVisionMode;

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
}
