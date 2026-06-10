package es.vargontoc.educational.framework.avatar.infrastructure.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.avatar.cache")
public class AvatarAudioCacheProperties {

    private boolean enabled = true;
    private int maxEntries = 1024;
    private int expireAfterWriteMinutes = 60;
    private String diskPath = "cache/avatar-audio";
    private int maxDiskEntries = 4096;
    private int audioFormatVersion = 1;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public int getExpireAfterWriteMinutes() {
        return expireAfterWriteMinutes;
    }

    public void setExpireAfterWriteMinutes(int expireAfterWriteMinutes) {
        this.expireAfterWriteMinutes = expireAfterWriteMinutes;
    }

    public String getDiskPath() {
        return diskPath;
    }

    public void setDiskPath(String diskPath) {
        this.diskPath = diskPath;
    }

    public int getMaxDiskEntries() {
        return maxDiskEntries;
    }

    public void setMaxDiskEntries(int maxDiskEntries) {
        this.maxDiskEntries = maxDiskEntries;
    }

    public int getAudioFormatVersion() {
        return audioFormatVersion;
    }

    public void setAudioFormatVersion(int audioFormatVersion) {
        this.audioFormatVersion = audioFormatVersion;
    }
}