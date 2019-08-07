package com.github.coleb1911.ghost2.music;

import com.github.coleb1911.ghost2.commands.VoiceUtils;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GuildAudioProvider extends AudioProvider {
    private final Guild guild;

    private final MutableAudioFrame frame;
    private final TrackQueue queue;
    private final AudioPlayer player;

    GuildAudioProvider(final AudioPlayer player, final DiscordClient client, final Guild guild) {
        super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
        this.player = player;
        this.guild = guild;

        this.frame = new MutableAudioFrame();
        frame.setBuffer(getBuffer());

        this.queue = new TrackQueue(player);
        player.addListener(queue);

        // Kill audio provider on self disconnect
        client.getEventDispatcher().on(VoiceStateUpdateEvent.class)
                .filter(e -> e.getClient().equals(client))
                .filter(e -> e.getCurrent().getChannelId().isEmpty())
                .take(1)
                .subscribe(e -> this.destroy());

        // Disconnect on empty channel
        client.getEventDispatcher().on(VoiceStateUpdateEvent.class)
                .map(VoiceStateUpdateEvent::getOld).filter(Optional::isPresent).map(Optional::get)
                .map(VoiceState::getChannel).map(Mono::block).filter(Objects::nonNull)
                .filter(channel -> channel.getVoiceStates().count().blockOptional().orElse(-1L) <= 1)
                .take(1)
                .subscribe(VoiceUtils::leaveChannel);
    }

    public void addTrack(String source, TrackLoadCallback callback) {
        GuildAudioProviders.load(source, new TrackLoader(queue, callback));
    }

    public boolean shuffle() {
        return queue.shuffle();
    }

    public boolean skipTrack() {
        return queue.next();
    }

    public List<AudioTrack> getQueue() {
        return queue.getTracks();
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
