package com.github.coleb1911.ghost2.commands.modules.music;

import com.github.coleb1911.ghost2.commands.VoiceUtils;
import com.github.coleb1911.ghost2.commands.meta.CommandContext;
import com.github.coleb1911.ghost2.commands.meta.Module;
import com.github.coleb1911.ghost2.commands.meta.ModuleInfo;
import com.github.coleb1911.ghost2.commands.meta.ReflectiveAccess;
import discord4j.core.object.util.Permission;

import javax.validation.constraints.NotNull;

public final class ModuleJoin extends Module {


    @ReflectiveAccess
    public ModuleJoin() {
        super(new ModuleInfo.Builder(ModuleJoin.class)
                .withName("join")
                .withDescription("Make ghost2 join your voice channel.")
                .withBotPermissions(Permission.CONNECT));
    }

    @Override
    public void invoke(@NotNull CommandContext ctx) {
        VoiceUtils.join(ctx);
    }
}
