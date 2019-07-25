package com.github.coleb1911.ghost2.commands.modules.music;

import com.github.coleb1911.ghost2.commands.VoiceUtils;
import com.github.coleb1911.ghost2.commands.meta.CommandContext;
import com.github.coleb1911.ghost2.commands.meta.Module;
import com.github.coleb1911.ghost2.commands.meta.ModuleInfo;
import com.github.coleb1911.ghost2.commands.meta.ReflectiveAccess;
import com.github.coleb1911.ghost2.music.GuildAudioProvider;
import com.github.coleb1911.ghost2.music.GuildAudioProviders;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import discord4j.core.object.util.Permission;

import javax.validation.constraints.NotNull;

public final class ModulePlay extends Module {
    private static final String REPLY_MISSING_SOURCE = "Please provide a link to a track.";

    @ReflectiveAccess
    public ModulePlay() {
        super(new ModuleInfo.Builder(ModulePlay.class)
                .withName("play")
                .withDescription("Play or queue a song.")
                .withBotPermissions(Permission.CONNECT));
    }

    @Override
    public void invoke(@NotNull CommandContext ctx) {
        // Check arguments
        if (ctx.getArgs().size() < 1) {
            ctx.reply(REPLY_MISSING_SOURCE);
            return;
        }

        // Join channel if not in one already
        if (!VoiceUtils.memberIsInVoiceChannel(ctx.getSelf())) {
            VoiceUtils.join(ctx);
        }

        // Load track
        try {
            GuildAudioProvider provider = GuildAudioProviders.getOrCreate(ctx.getGuild(), ctx.getClient());
            provider.addTrack(ctx.getArgs().get(0), status -> ctx.reply(status.getMessage()));
        } catch (FriendlyException e) {
            ctx.reply(e.getMessage());
        }
    }
}
