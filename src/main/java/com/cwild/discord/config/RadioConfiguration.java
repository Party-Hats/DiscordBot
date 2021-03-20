package com.cwild.discord.config;

import com.cwild.discord.audio.GuildRadioManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.GatewayDiscordClient;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
public class RadioConfiguration {

  @Getter
  private static AudioPlayerManager playerManager;

  private final GatewayDiscordClient discordClient;
  private final GuildRadioManager guildRadioManager;

  public RadioConfiguration(
      GatewayDiscordClient discordClient,
      GuildRadioManager guildRadioManager) {
    this.discordClient = discordClient;
    this.guildRadioManager = guildRadioManager;
    playerManager = new DefaultAudioPlayerManager();
    playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
    AudioSourceManagers.registerRemoteSources(playerManager);
    AudioSourceManagers.registerLocalSource(playerManager);

    log.info("Initializing guild radios");
    this.discordClient.getGuilds()
        .map(this.guildRadioManager::of)
        .map(r -> {
          log.info("Created radio for guild {}", r.getGuildId().asString());
          return r;
        })
        .collectList()
        .doOnSuccess(l -> log.info("Initialized all guild radios"))
        .block();
  }

}
