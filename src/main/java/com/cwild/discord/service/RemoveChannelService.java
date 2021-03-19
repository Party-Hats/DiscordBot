package com.cwild.discord.service;

import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.VoiceChannel;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RemoveChannelService {

  public Mono<Boolean> hasNoMembers(Mono<VoiceChannel> channel) {
    return channel
        .flatMap(c -> c.getVoiceStates().collectList())
        .map(List::isEmpty);
  }
}
