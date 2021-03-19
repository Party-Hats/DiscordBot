package com.cwild.discord.service;

import com.cwild.discord.Constants;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.retriever.EntityRetrievalStrategy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CreateChannelService {

  public Mono<Boolean> isCreateChannel(Mono<VoiceChannel> channel) {
    return channel
        .filter(c -> Constants.CREATE_CHANNEL_NAME.equalsIgnoreCase(c.getName()))
        .hasElement();
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
        .map(CreateChannelService::findNextChannelName)
        .flatMap(name -> targetGuild.createVoiceChannel(
            voiceChannelCreateSpec -> voiceChannelCreateSpec.setName(name)));
  }
}
