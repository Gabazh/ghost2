package com.github.coleb1911.ghost2.commands;

import discord4j.core.object.entity.Member;
import discord4j.core.object.util.PermissionSet;

public class DiscordUtils {
    public static PermissionSet getPermissionsFor(Member member) {
        return member.getBasePermissions().blockOptional().orElse(PermissionSet.none());
    }
}
