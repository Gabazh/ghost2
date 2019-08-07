package com.github.coleb1911.ghost2.music;

@FunctionalInterface
public interface TrackLoadCallback {
    void callback(Status status);

    enum Status {
        PLAYING("Playing track."),
        QUEUED("Queued track."),
        QUEUED_ALL("Queued {} tracks."),
        QUEUED_SOME("Queued {} tracks.\nSome tracks were unable to be added due to queue reaching max length."),
        NOT_FOUND("Could not load a track from that link."),
        FAILED("Failed to queue track.");

        private String message;

        Status(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public Status with(Object parameter) {
            message = message.replace("{}", parameter.toString());
            return this;
        }
    }
}
