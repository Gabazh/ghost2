package com.github.coleb1911.ghost2.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class TrackQueue extends AudioEventAdapter {
    private static final int QUEUE_MAX_SIZE = 50;

    private final AudioPlayer player;
    private final Queue<AudioTrack> queue;

    TrackQueue(final AudioPlayer player) {
        this.player = player;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    TrackLoadCallback.Status add(AudioTrack track) {
        if (queue.size() < QUEUE_MAX_SIZE) {
            if (player.startTrack(track, true)) {
                return TrackLoadCallback.Status.PLAYING;
            } else if (queue.offer(track)) {
                return TrackLoadCallback.Status.QUEUED;
            }
        }
        return TrackLoadCallback.Status.FAILED;
    }

    TrackLoadCallback.Status addAll(List<AudioTrack> tracks) {
        while (!tracks.isEmpty()) {
            if (add(tracks.remove(0)).equals(TrackLoadCallback.Status.FAILED)) {
                return TrackLoadCallback.Status.QUEUED_SOME.with(queue.size());
            }
        }
        return TrackLoadCallback.Status.QUEUED_ALL.with(queue.size());
    }

    boolean shuffle() {
        if (queue.isEmpty()) {
            return false;
        }

        List<AudioTrack> tracks = Arrays.asList(queue.toArray(AudioTrack[]::new));
        Collections.shuffle(tracks);
        queue.clear();
        queue.addAll(tracks);
        return true;
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

    List<AudioTrack> getTracks() {
        return List.of(queue.toArray(AudioTrack[]::new));
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
