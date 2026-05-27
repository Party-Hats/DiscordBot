package com.cwild.discord.audio;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.reaction.ReactionEmoji.Unicode;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RadioEmojiStrategy {
  MUSICAL_NOTE(ReactionEmoji.unicode("🎵")) {
    @Override
    public void doBehavior(ReactionAddEvent event, GuildRadio radio) {
      VoiceChannel voiceChannel = event.getMember().map(m ->
          m.getVoiceState()
              .flatMap(VoiceState::getChannel)
              .block())
          .orElse(null);
      Snowflake block = radio.getConnectedChannel().block();
      if (voiceChannel != null && voiceChannel.getId().equals(block)) {
        radio.disconnectVoiceChannel().block();
      } else {
        event.getMember().ifPresent(m ->
            m.getVoiceState()
                .flatMap(VoiceState::getChannel).flatMap(radio::joinVoiceChannel)
                .block());
      }
    }
  },
  PLAY_PAUSE(ReactionEmoji.unicode("⏯️")) {
    @Override
    public void doBehavior(ReactionAddEvent event, GuildRadio radio) {
      radio.setPaused(!radio.isPaused());
    }
  },
  UPWARDS(ReactionEmoji.unicode("🔼")) {
    @Override
    public void doBehavior(ReactionAddEvent event, GuildRadio radio) {
      radio.incrementVolume(2);
    }
  },
  DOWNWARDS(ReactionEmoji.unicode("🔽")) {
    @Override
    public void doBehavior(ReactionAddEvent event, GuildRadio radio) {
      radio.decrementVolume(2);
    }
  },
  FAST_UP(ReactionEmoji.unicode("⏫")) {
    @Override
    public void doBehavior(ReactionAddEvent event, GuildRadio radio) {
      radio.incrementVolume(5);
    }
  },
  FAST_DOWN(ReactionEmoji.unicode("⏬")) {
    @Override
    public void doBehavior(ReactionAddEvent event, GuildRadio radio) {
      radio.decrementVolume(5);
    }
  };

  private final Unicode unicode;

  public abstract void doBehavior(ReactionAddEvent event, GuildRadio radio);

  public static RadioEmojiStrategy forUnicode(Unicode unicode) {
    return Arrays.stream(RadioEmojiStrategy.values())
        .filter(s -> s.unicode.equals(unicode))
        .findFirst()
        .orElse(null);
  }
}
