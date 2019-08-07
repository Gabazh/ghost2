package com.github.coleb1911.ghost2.commands.modules.music;

import com.github.coleb1911.ghost2.commands.VoiceUtils;
import com.github.coleb1911.ghost2.commands.meta.CommandContext;
import com.github.coleb1911.ghost2.commands.meta.Module;
import com.github.coleb1911.ghost2.commands.meta.ModuleInfo;
import com.github.coleb1911.ghost2.commands.meta.ReflectiveAccess;
import com.github.coleb1911.ghost2.music.GuildAudioProviders;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;

public final class ModuleShuffle extends Module {
    private static final String REPLY_SUCCESS = "Shuffled.";
    private static final String REPLY_QUEUE_EMPTY = "Nothing to shuffle. Queue a song with `play`.";

    @ReflectiveAccess
    public ModuleShuffle() {
        super(new ModuleInfo.Builder(ModuleShuffle.class)
                .withName("shuffle")
                .withDescription("Shuffle the songs currently in the queue"));
    }

    @Override
    public void invoke(@NotNull CommandContext ctx) {
        GuildAudioProviders.getIfExists(ctx.getGuild())
                .switchIfEmpty(Mono.fromRunnable(() -> ctx.reply(VoiceUtils.REPLY_NOT_PROVIDING)))
                .subscribe(provider -> {
                    if (provider.shuffle()) {
                        ctx.reply(REPLY_SUCCESS);
                    } else ctx.reply(REPLY_QUEUE_EMPTY);
                });
    }
}
