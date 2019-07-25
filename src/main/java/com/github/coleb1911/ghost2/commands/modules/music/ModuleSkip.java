package com.github.coleb1911.ghost2.commands.modules.music;

import com.github.coleb1911.ghost2.commands.meta.CommandContext;
import com.github.coleb1911.ghost2.commands.meta.Module;
import com.github.coleb1911.ghost2.commands.meta.ModuleInfo;
import com.github.coleb1911.ghost2.commands.meta.ReflectiveAccess;
import com.github.coleb1911.ghost2.music.GuildAudioProvider;
import com.github.coleb1911.ghost2.music.GuildAudioProviders;

import javax.validation.constraints.NotNull;

public final class ModuleSkip extends Module {
    private static final String REPLY_SKIPPED = "Skipped.";
    private static final String REPLY_SKIPPED_END = "Skipped. End of queue.";

    @ReflectiveAccess
    public ModuleSkip() {
        super(new ModuleInfo.Builder(ModuleSkip.class)
                .withName("skip")
                .withDescription("Skips the currently playing track."));

    }

    @Override
    public void invoke(@NotNull CommandContext ctx) {
        GuildAudioProvider provider = GuildAudioProviders.getIfExists(ctx.getGuild());
        if (provider.skipTrack()) {
            ctx.reply(REPLY_SKIPPED);
        } else {
            ctx.reply(REPLY_SKIPPED_END);
        }
    }
}
