package es.vargontoc.educational.framework.avatar.domain;

public  record AvatarLifecycleResult(
        GameAvatarEvent event,
        byte[] audioData
    ) {
        public static AvatarLifecycleResult noSession(Long sessionId) {
            return new AvatarLifecycleResult(null, null);
        }

        public static AvatarLifecycleResult suppressed(Long sessionId) {
            return new AvatarLifecycleResult(null, null);
        }

        public boolean isPresent() {
            return event != null;
        }
    }
