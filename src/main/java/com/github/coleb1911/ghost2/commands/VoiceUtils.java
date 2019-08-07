package com.github.coleb1911.ghost2.commands;

import com.github.coleb1911.ghost2.commands.meta.CommandContext;
import com.github.coleb1911.ghost2.music.GuildAudioProviders;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceUtils {
    public static final String REPLY_NOT_PROVIDING = "I'm not playing any music.";
    public static final String REPLY_EMPTY_QUEUE = "There's nothing in the queue.";

    private static final String REPLY_OCCUPIED = "I'm already in a voice channel.";
    private static final String REPLY_NO_VOICE_CHANNEL = "You're not in a voice channel.";
    private static final String REPLY_NO_CONNECT_PERM = "I can't connect to that channel.";
    private static final Map<VoiceChannel, VoiceConnection> CONNECTIONS = new ConcurrentHashMap<>();

    public static boolean memberIsInVoiceChannel(Member member) {
        Optional<VoiceState> state = member.getVoiceState().blockOptional();
        return state.isPresent() && state.get().getChannel().block() != null;
    }

    public static void join(CommandContext ctx) {
        // Make sure the bot isn't in a voice channel with other users
        if (memberIsInVoiceChannel(ctx.getSelf())) {
            ctx.reply(REPLY_OCCUPIED);
            return;
        }

        // Make sure the invoker is in a voice channel
        VoiceChannel channel = ctx.getInvoker().getVoiceState().flatMap(VoiceState::getChannel).block();
        if (!memberIsInVoiceChannel(ctx.getInvoker())) {
            ctx.reply(REPLY_NO_VOICE_CHANNEL);
            return;
        }
        assert channel != null;

        // Check permissions for channel
        PermissionSet channelPerms = channel.getEffectivePermissions(ctx.getSelf().getId()).block();
        if (channelPerms == null || !channelPerms.contains(Permission.CONNECT)) {
            ctx.reply(REPLY_NO_CONNECT_PERM);
            return;
        }

        // Join channel
        channel.join(spec -> {
            GuildAudioProviders.getOrCreate(ctx.getGuild(), ctx.getClient())
                    .subscribe(spec::setProvider);
        }).doOnSuccess(connection -> CONNECTIONS.put(channel, connection)).subscribe();
    }

    public static void leave(CommandContext ctx) {
        if (memberIsInVoiceChannel(ctx.getSelf())) {
            ctx.getSelf().getVoiceState()
                    .map(VoiceState::getChannel).map(Mono::block)
                    .subscribe(VoiceUtils::leaveChannel);
        }
    }

    public static void leaveChannel(VoiceChannel channel) {
        VoiceConnection conn = CONNECTIONS.get(channel);

        // Dirty workaround for https://github.com/Discord4J/Discord4J/issues/536
        if (conn != null) {
            Mono.fromRunnable(conn::disconnect)
                    .retryBackoff(5L, Duration.ofSeconds(1L))
                    .doOnError(ignore -> {
                    })
                    .subscribe();
        }
    }
}
