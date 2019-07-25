package com.github.coleb1911.ghost2.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

class TrackQueue extends AudioEventAdapter {
    private final AudioPlayer player;
    private final Queue<AudioTrack> queue;

    TrackQueue(final AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>(50);
    }

    TrackLoadCallback.Status add(AudioTrack track) {
        if (player.startTrack(track, true)) {
            return TrackLoadCallback.Status.PLAYING;
        } else if (queue.offer(track)) {
            return TrackLoadCallback.Status.QUEUED;
        }
        return TrackLoadCallback.Status.FAILED;
    }

    TrackLoadCallback.Status addAll(List<AudioTrack> tracks) {
        for (AudioTrack track : tracks) {
            if (!queue.offer(tracks.remove(0))) {
                return TrackLoadCallback.Status.QUEUED_SOME;
            }
        }
        return TrackLoadCallback.Status.QUEUED_ALL;
    }

    boolean next() {
        return player.startTrack(queue.poll(), false);
    }

    boolean pause() {
        if (!player.isPaused()) {
            player.setPaused(true);
            return true;
        }
        return false;
    }

    boolean resume() {
        if (player.isPaused()) {
            player.setPaused(false);
            return true;
        }
        return false;
    }

    AudioTrack[] getTracks() {
        return queue.toArray(AudioTrack[]::new);
    }

    void destroy() {
        player.removeListener(this);
        queue.clear();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            next();
        }
    }
}
