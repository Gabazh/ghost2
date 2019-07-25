package com.github.coleb1911.ghost2.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class TrackLoader implements AudioLoadResultHandler {
    private final TrackQueue queue;
    private final Map<String, TrackLoadCallback> callbacks;

    TrackLoader(final TrackQueue queue) {
        this.queue = queue;
        this.callbacks = new ConcurrentHashMap<>();
    }

    void putCallback(String source, TrackLoadCallback callback) {
        callbacks.put(source, callback);
    }

    private void execCallback(String id, TrackLoadCallback.Status status) {
        Optional<String> key = callbacks.keySet().stream()
                .filter(s -> s.contains(id))
                .findFirst();
        key.ifPresent(s -> callbacks.remove(s).callback(status));
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        execCallback(track.getIdentifier(), queue.add(track));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        // to be implemented
    }

    @Override
    public void noMatches() {
        throw new FriendlyException("Link is invalid.", FriendlyException.Severity.COMMON, null);
    }

    @Override
    public void loadFailed(FriendlyException e) {
        throw e;
    }
}
