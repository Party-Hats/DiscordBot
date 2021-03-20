package com.cwild.discord.listener;

import com.cwild.discord.audio.GuildRadio;
import com.cwild.discord.audio.RadioEmojiStrategy;
import com.cwild.discord.audio.RadioUI;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji.Unicode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class RadioControlsListener extends EventListener<ReactionAddEvent> {

  private final RadioUI radioUI;
  private final GuildRadio radio;

  @Override
  public Class<ReactionAddEvent> getEventType() {
    return ReactionAddEvent.class;
  }

  @Override
  public Mono<Void> execute(ReactionAddEvent event) {
    User user = event.getUser().block();
    if (!radioUI.shouldEvaluate(event.getGuildId(), user, event.getMessageId(), event.getEmoji())) {
      return Mono.empty();
    }

    RadioEmojiStrategy radioEmojiStrategy = event.getEmoji()
        .asUnicodeEmoji()
        .map(RadioEmojiStrategy::forUnicode)
        .orElse(null);
    if (radioEmojiStrategy == null) {
      return event.getMessage()
          .flatMap(m -> m.removeReaction(event.getEmoji(), event.getUserId()));
    }
    radioEmojiStrategy.doBehavior(event, radio);

    // First tries to update embed
//    return event.getMessage()
//        .flatMap(m -> m.edit(spec -> spec.setEmbed(eSpec -> {
//          List<Embed> embeds = m.getEmbeds();
//          Embed embed = embeds.get(0);
//          eSpec.addField("Volume", updated.toString(), false);
//        })))
//        .then();
    return event.getMessage()
        .flatMap(m -> m.edit(messageEditSpec -> messageEditSpec.setContent(m.getContent()
            + event.getEmoji().asUnicodeEmoji().map(Unicode::getRaw).orElse(null))))
        .flatMap(m -> m.removeReaction(event.getEmoji(), event.getUserId()));

    // Example on how to remove message
//    if (event.getEmoji()
//        .asUnicodeEmoji()
//        .map(e -> e.getRaw().equals("\uD83D\uDE48"))
//        .orElse(false)) {
//      return event.getMessage()
//          .doOnSuccess(e -> e.getChannel().flatMap(c -> c.createMessage("Zorry")).block())
//          .flatMap(Message::delete);
//    }

  }
}
