package com.cwild.discord.audio;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import discord4j.voice.AudioProvider;
import java.nio.ByteBuffer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LarvaAudioProvider extends AudioProvider {

  private final AudioPlayer player;
  private final MutableAudioFrame frame;

  public LarvaAudioProvider(final AudioPlayer player) {
    // Allocate a ByteBuffer for Discord4J's AudioProvider to hold audio data for Discord
    super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
    // Set LavaPlayer's AudioFrame to use the same buffer as Discord4J's
    frame = new MutableAudioFrame();
    frame.setBuffer(getBuffer());
    this.player = player;
  }

  @Override
  public boolean provide() {
    // AudioPlayer writes audio data to the AudioFrame
    final boolean didProvide = player.provide(frame);
    // If audio was provided, flip from write-mode to read-mode
    if (didProvide) {
      getBuffer().flip();
    }

    return didProvide;
  }
}
