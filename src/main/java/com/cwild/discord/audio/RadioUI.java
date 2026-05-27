package com.cwild.discord.audio;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import java.time.Instant;
import java.util.Optional;
import lombok.Data;
import reactor.core.publisher.Mono;

@Data
public class RadioUI {

  private final Guild guild;
  Snowflake id;

  public boolean shouldEvaluate(
      Optional<Snowflake> guildId,
      User requestingUser,
      Snowflake messageId,
      ReactionEmoji emoji) {
    return requestingUser != null
        && !requestingUser.isBot()
        && id.equals(messageId)
        && guild.getId().equals(guildId.orElse(null));
  }

  public RadioUI(Guild guild, GatewayDiscordClient discordClient) {
    this.guild = guild;

    Mono<TextChannel> radioControlsChannel = discordClient.getChannelById(
        Snowflake.of(822789363326648330L))
        .map(TextChannel.class::cast);
    radioControlsChannel
        .flatMap(c -> c.getMessagesBefore(Snowflake.of(Instant.now()))
            .flatMap(Message::delete).then())
        .block();
    Message message = radioControlsChannel
        .flatMap(c -> c.createEmbed(embedCreateSpec -> embedCreateSpec
            .setTitle("Radio Controls")
            .addField("Legende", "Play/Pause und Lauter/Leiser", true)))
        .doOnSuccess(m -> m.addReaction(ReactionEmoji.unicode("🎵")).block())
        .doOnSuccess(m -> m.addReaction(ReactionEmoji.unicode("⏯️")).block())
        .doOnSuccess(m -> m.addReaction(ReactionEmoji.unicode("🔼")).block())
        .doOnSuccess(m -> m.addReaction(ReactionEmoji.unicode("⏫")).block())
        .doOnSuccess(m -> m.addReaction(ReactionEmoji.unicode("🔽")).block())
        .doOnSuccess(m -> m.addReaction(ReactionEmoji.unicode("⏬")).block())
        .block();
    if (message == null) {
      throw new IllegalStateException("Could not create radio controls message");
    }
    id = message.getId();
  }
}
