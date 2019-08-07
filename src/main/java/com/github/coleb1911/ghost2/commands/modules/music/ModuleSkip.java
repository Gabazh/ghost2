package com.github.coleb1911.ghost2.commands.modules.music;

import com.github.coleb1911.ghost2.commands.VoiceUtils;
import com.github.coleb1911.ghost2.commands.meta.CommandContext;
import com.github.coleb1911.ghost2.commands.meta.Module;
import com.github.coleb1911.ghost2.commands.meta.ModuleInfo;
import com.github.coleb1911.ghost2.commands.meta.ReflectiveAccess;
import com.github.coleb1911.ghost2.music.GuildAudioProviders;
import reactor.core.publisher.Mono;

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
        GuildAudioProviders.getIfExists(ctx.getGuild())
                .switchIfEmpty(Mono.fromRunnable(() -> ctx.reply(VoiceUtils.REPLY_NOT_PROVIDING)))
                .subscribe(provider -> {
                    if (provider.getQueue().isEmpty()) {
                        ctx.reply(VoiceUtils.REPLY_EMPTY_QUEUE);
                        return;
                    }

                    if (provider.skipTrack()) {
                        ctx.reply(REPLY_SKIPPED);
                    } else {
                        ctx.reply(REPLY_SKIPPED_END);
                    }
                });
    }
}
