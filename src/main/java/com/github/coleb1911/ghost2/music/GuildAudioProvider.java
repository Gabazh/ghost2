package com.github.coleb1911.ghost2.music;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.voice.AudioProvider;

import java.nio.ByteBuffer;

public class GuildAudioProvider extends AudioProvider {
    private final Guild guild;

    private final MutableAudioFrame frame;
    private final TrackQueue queue;
    private final TrackLoader loader;
    private final AudioPlayer player;

    GuildAudioProvider(final AudioPlayer player, final DiscordClient client, final Guild guild) {
        super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
        this.player = player;
        this.guild = guild;

        this.frame = new MutableAudioFrame();
        this.queue = new TrackQueue(player);
        this.loader = new TrackLoader(queue);
        frame.setBuffer(getBuffer());
        player.addListener(queue);

        // Kill audio provider on voice disconnect
        client.getEventDispatcher().on(VoiceStateUpdateEvent.class)
                .filter(e -> e.getClient().equals(client))
                .filter(e -> e.getCurrent().getChannelId().isEmpty())
                .take(1)
                .subscribe(e -> this.destroy());
    }

    public void addTrack(String source, TrackLoadCallback callback) {
        GuildAudioProviders.load(source, loader);
        loader.putCallback(source, callback);
    }

    public boolean skipTrack() {
        return queue.next();
    }

    @Override
    public boolean provide() {
        boolean provided = player.provide(frame);
        if (provided) {
            getBuffer().flip();
        }
        return provided;
    }

    void destroy() {
        player.stopTrack();
        queue.destroy();
        GuildAudioProviders.destroy(guild);
    }
}
