package com.github.coleb1911.ghost2.music;

@FunctionalInterface
public interface TrackLoadCallback {
    void callback(Status status);

    enum Status {
        PLAYING("Playing track."),
        QUEUED("Queued track."),
        QUEUED_ALL("Queued tracks."),
        QUEUED_SOME("Queued tracks.\nSome were unable to be added due to queue reaching max length."),
        FAILED("Failed to queue track.");

        private final String message;

        Status(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
