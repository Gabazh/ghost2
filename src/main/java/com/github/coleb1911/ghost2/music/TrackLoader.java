package com.github.coleb1911.ghost2.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

class TrackLoader implements AudioLoadResultHandler {
    private final TrackQueue queue;
    private final TrackLoadCallback callback;

    TrackLoader(final TrackQueue queue, final TrackLoadCallback callback) {
        this.queue = queue;
        this.callback = callback;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        callback.callback(queue.add(track));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        callback.callback(queue.addAll(playlist.getTracks()));
    }

    @Override
    public void noMatches() {
        callback.callback(TrackLoadCallback.Status.NOT_FOUND);
    }

    @Override
    public void loadFailed(FriendlyException e) {
        callback.callback(TrackLoadCallback.Status.FAILED);
    }
}
