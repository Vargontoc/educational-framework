package es.vargontoc.educational.framework.avatar.infrastructure.cache;

import java.util.Objects;

public final class AvatarCacheKey {

    private final String locale;
    private final String tone;
    private final int textHash;
    private final String context;
    private final int audioFormatVersion;

    public AvatarCacheKey(String locale, String tone, int textHash, String context, int audioFormatVersion) {
        this.locale = locale != null ? locale : "";
        this.tone = tone != null ? tone : "";
        this.textHash = textHash;
        this.context = context != null ? context : "";
        this.audioFormatVersion = audioFormatVersion;
    }

    public String locale() {
        return locale;
    }

    public String tone() {
        return tone;
    }

    public int textHash() {
        return textHash;
    }

    public String context() {
        return context;
    }

    public int audioFormatVersion() {
        return audioFormatVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvatarCacheKey that = (AvatarCacheKey) o;
        return textHash == that.textHash &&
               audioFormatVersion == that.audioFormatVersion &&
               Objects.equals(locale, that.locale) &&
               Objects.equals(tone, that.tone) &&
               Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale, tone, textHash, context, audioFormatVersion);
    }

    @Override
    public String toString() {
        return "AvatarCacheKey{locale=" + locale + ", tone=" + tone +
               ", textHash=" + textHash + ", context=" + context +
               ", audioFormatVersion=" + audioFormatVersion + "}";
    }
}