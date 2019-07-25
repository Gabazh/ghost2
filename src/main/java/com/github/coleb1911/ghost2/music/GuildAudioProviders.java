package com.github.coleb1911.ghost2.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildAudioProviders {
    private static final AudioPlayerManager PLAYER_MANAGER;
    private static final Map<Guild, GuildAudioProvider> PROVIDERS;

    static {
        PLAYER_MANAGER = new DefaultAudioPlayerManager();
        PLAYER_MANAGER.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        PLAYER_MANAGER.setItemLoaderThreadPoolSize(Runtime.getRuntime().availableProcessors());
        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);

        PROVIDERS = new ConcurrentHashMap<>();
    }

    static void load(final String source, final TrackLoader loader) throws FriendlyException {
        PLAYER_MANAGER.loadItem(source, loader);
    }

    static void destroy(final Guild guild) {
        PROVIDERS.remove(guild);
    }

    public static GuildAudioProvider getOrCreate(final Guild guild, final DiscordClient client) {
        if (!PROVIDERS.containsKey(guild)) {
            PROVIDERS.put(guild, new GuildAudioProvider(PLAYER_MANAGER.createPlayer(), client, guild));
        }
        return PROVIDERS.get(guild);
    }

    public static GuildAudioProvider getIfExists(final Guild guild) {
        return PROVIDERS.get(guild);
    }

    public static void shutdown() {
        PROVIDERS.values().forEach(GuildAudioProvider::destroy);
        PLAYER_MANAGER.shutdown();
    }
}