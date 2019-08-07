package com.github.coleb1911.ghost2.commands.modules.music;

import com.github.coleb1911.ghost2.commands.VoiceUtils;
import com.github.coleb1911.ghost2.commands.meta.CommandContext;
import com.github.coleb1911.ghost2.commands.meta.Module;
import com.github.coleb1911.ghost2.commands.meta.ModuleInfo;
import com.github.coleb1911.ghost2.commands.meta.ReflectiveAccess;

import javax.validation.constraints.NotNull;

public final class ModuleLeave extends Module {
    private static final String REPLY_NO_VOICE_CHANNEL = "I'm not in a voice channel.";


    @ReflectiveAccess
    public ModuleLeave() {
        super(new ModuleInfo.Builder(ModuleLeave.class)
                .withName("leave")
                .withAliases("stop")
                .withDescription("Clear the queue and leave the voice channel"));
    }

    @Override
    public void invoke(@NotNull CommandContext ctx) {
        if (!VoiceUtils.memberIsInVoiceChannel(ctx.getSelf())) {
            ctx.reply(REPLY_NO_VOICE_CHANNEL);
            return;
        }

        VoiceUtils.leave(ctx);
    }
}
