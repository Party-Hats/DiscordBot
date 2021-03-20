package com.cwild.discord.audio;

import com.cwild.discord.config.RadioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Data
public class GuildRadio {

  private final Object voiceConnectionLock = new Object();

  private final Guild guild;
  private final Snowflake guildId;
  @Getter(AccessLevel.NONE)
  private final AudioPlayer player;
  private final RadioEventListener scheduler;
  private final LarvaAudioProvider provider;
  private VoiceConnection currentVoiceConnection;

  public GuildRadio(Guild guild) {
    this.guild = guild;
    guildId = guild.getId();
    player = RadioConfiguration.getPlayerManager().createPlayer();
    player.setVolume(2);
    scheduler = new RadioEventListener(player);
    provider = new LarvaAudioProvider(player);

    player.addListener(scheduler);

    // TODO
    RadioConfiguration.getPlayerManager().loadItemOrdered(this,
        "http://rock-high.rautemusik.fm", new AudioLoadResultHandler() {

          @Override
          public void trackLoaded(AudioTrack track) {
            log.info("track loaded {}", track);
            player.playTrack(track);
//            voiceConnection
          }

          @Override
          public void playlistLoaded(AudioPlaylist playlist) {
            log.info("playlist loaded {}", playlist);
          }

          @Override
          public void noMatches() {
            log.info("no matches");
          }

          @Override
          public void loadFailed(FriendlyException exception) {
            log.info("load failed", exception);
          }
        });
  }

  public void setPaused(boolean paused) {
    log.info("{} - {} playback", guild.getId().asString(),
        paused ? "Pausing" : "Resuming");
    player.setPaused(paused);
  }

  @Synchronized("voiceConnectionLock")
  public Mono<VoiceConnection> joinVoiceChannel(VoiceChannel voiceChannel) {
    return Mono.just(voiceChannel)
        .flatMap(c -> c.join(spec -> spec.setProvider(provider)))
        .doOnSuccess(v -> currentVoiceConnection = v)
        .doOnSuccess(v -> log.info("{} - Connected to voice channel {}", guild.getId().asString(),
            voiceChannel.getId()));
  }

  @Synchronized("voiceConnectionLock")
  public Mono<Void> disconnectVoiceChannel() {
    return currentVoiceConnection.disconnect()
        .flatMap(v -> currentVoiceConnection.getChannelId())
        .doOnSuccess(s -> currentVoiceConnection = null)
        .doOnSuccess(
            s -> log.info("{} - Disconnected from voice channel {}", guild.getId().asString(), s))
        .then();
  }

  public Mono<Snowflake> getConnectedChannel() {
    return currentVoiceConnection == null
        ? Mono.empty()
        : currentVoiceConnection.getChannelId();
  }

  public boolean isPaused() {
    return player.isPaused();
  }

  public int incrementVolume(int delta) {
    int newVolume = player.getVolume() + delta;
    player.setVolume(newVolume);
    log.info("{} - Setting new volume {}", guild.getId().asString(), newVolume);
    return newVolume;
  }

  public int decrementVolume(int delta) {
    int newVolume = player.getVolume() - delta;
    player.setVolume(newVolume);
    log.info("{} - Setting new volume {}", guild.getId().asString(), newVolume);
    return newVolume;
  }
}
