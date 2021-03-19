package com.cwild.discord.service;

import com.cwild.discord.Constants;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ChannelService {

  public Mono<Boolean> isCreateChannel(Mono<VoiceChannel> channel) {
    return channel
        .filter(c -> Constants.CREATE_CHANNEL_NAME.equalsIgnoreCase(c.getName()))
        .hasElement();
  }
  public Mono<Boolean> hasNoMembers(Mono<VoiceChannel> channel) {
    return channel
        .flatMap(c -> c.getVoiceStates().collectList())
        .map(List::isEmpty);
  }

  public Mono<Boolean> isLimited(Mono<VoiceChannel> channel) {
    return channel
        .filter(v -> v.getPermissionOverwrites().stream()
            .anyMatch(p -> p.getDenied().contains(Permission.CONNECT)))
        .hasElement()
        .map(b -> !b);
  }

  private static String findNextChannelName(List<GuildChannel> allChannels) {
    List<String> channelNames = allChannels.stream()
        .filter(c -> VoiceChannel.class.isAssignableFrom(c.getClass()))
        .map(GuildChannel::getName)
        .collect(Collectors.toList());

    int iter = 1;
    String namePrefix = "Talk_";
    String name;
    do {
      name = namePrefix + iter++;
    } while (channelNames.contains(name));
    return name;
  }

  public Mono<VoiceChannel> createNewVoiceChannel(Guild targetGuild) {
    return targetGuild.getChannels().collectList()
        .map(ChannelService::findNextChannelName)
        .flatMap(name -> targetGuild.createVoiceChannel(
            voiceChannelCreateSpec -> voiceChannelCreateSpec.setName(name)));
  }
}
