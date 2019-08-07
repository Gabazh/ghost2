package com.github.coleb1911.ghost2.commands.modules.music;

import com.github.coleb1911.ghost2.commands.DiscordUtils;
import com.github.coleb1911.ghost2.commands.VoiceUtils;
import com.github.coleb1911.ghost2.commands.meta.CommandContext;
import com.github.coleb1911.ghost2.commands.meta.Module;
import com.github.coleb1911.ghost2.commands.meta.ModuleInfo;
import com.github.coleb1911.ghost2.commands.meta.ReflectiveAccess;
import com.github.coleb1911.ghost2.music.GuildAudioProvider;
import com.github.coleb1911.ghost2.music.GuildAudioProviders;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.spec.EmbedCreateSpec;
import org.apache.commons.lang3.time.DurationFormatUtils;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class ModuleQueue extends Module {
    private static final String EM_PREV = "\u2B05";
    private static final String EM_NEXT = "\u27A1";

    @ReflectiveAccess
    public ModuleQueue() {
        super(new ModuleInfo.Builder(ModuleQueue.class)
                .withName("queue")
                .withAliases("queuelist")
                .withDescription("View the tracks currently in the queue."));
    }

    @Override
    public void invoke(@NotNull final CommandContext ctx) {
        GuildAudioProviders.getIfExists(ctx.getGuild())
                .switchIfEmpty(Mono.fromRunnable(() -> ctx.reply(VoiceUtils.REPLY_NOT_PROVIDING)))
                .map(GuildAudioProvider::getQueue)
                .subscribe(tracks -> {
                    if (tracks.isEmpty()) {
                        ctx.reply(VoiceUtils.REPLY_EMPTY_QUEUE);
                        return;
                    }

                    // Create embed
                    Message embedMessage = ctx.replyEmbed(createTracklistEmbed(ctx, tracks, 0));

                    if (tracks.size() > 10) {
                        AtomicInteger selector = new AtomicInteger();
                        embedMessage.addReaction(ReactionEmoji.unicode(EM_PREV)).subscribe();
                        embedMessage.addReaction(ReactionEmoji.unicode(EM_NEXT)).subscribe();

                        // Scroll on reaction
                        ctx.getClient().getEventDispatcher().on(ReactionAddEvent.class)
                                .filter(e -> e.getMessageId().equals(embedMessage.getId()))
                                .filter(e -> e.getUserId().equals(ctx.getInvoker().getId()))
                                .timeout(Duration.ofSeconds(30L), s -> embedMessage.removeAllReactions().subscribe())
                                .subscribe(e -> {
                                    switch (e.getEmoji().asUnicodeEmoji().orElse(ReactionEmoji.unicode("")).getRaw()) {
                                        case EM_NEXT:
                                            if (selector.get() < (tracks.size() - 10))
                                                selector.set(selector.addAndGet(10));
                                            break;
                                        case EM_PREV:
                                            if (selector.get() > 0)
                                                selector.set(selector.addAndGet(-10));
                                            break;
                                        default:
                                            embedMessage.removeReaction(e.getEmoji(), e.getUserId());
                                            return;
                                    }

                                    embedMessage.removeReaction(e.getEmoji(), e.getUserId()).subscribe();
                                    embedMessage.edit(s -> s.setEmbed(createTracklistEmbed(ctx, tracks, selector.get()))).subscribe();
                                });
                    }
                });
    }

    private Consumer<EmbedCreateSpec> createTracklistEmbed(final CommandContext ctx,
                                                           final List<AudioTrack> tracks, final int selector) {
        return embedSpec -> {
            embedSpec.setTitle("Queue");
            for (int i = selector; i < selector + 10; i++) {
                if (i >= tracks.size()) break;
                AudioTrackInfo info = tracks.get(i).getInfo();
                embedSpec.addField((i + 1) + ". " + info.title, DurationFormatUtils.formatDuration(info.length, "HH:mm:ss"), false);
            }


            // Check for reaction management perms
            PermissionSet perms = DiscordUtils.getPermissionsFor(ctx.getSelf());
            if (!perms.containsAll(PermissionSet.of(Permission.MANAGE_MESSAGES, Permission.ADD_REACTIONS))) {
                embedSpec.setFooter("Missing permissions for reaction scrolling. Only showing 10 results.", null);
                return;
            }
            embedSpec.setFooter((selector + 1) + "-" + (selector + 10) + " of " + tracks.size() + " results", null);
        };
    }
}