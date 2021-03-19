package com.cwild.discord.listener;

import com.cwild.discord.service.ChannelService;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectToCreateChannelListener extends EventListener<VoiceStateUpdateEvent> {

  private final ChannelService channelService;

  @Override
  public Class<VoiceStateUpdateEvent> getEventType() {
    return VoiceStateUpdateEvent.class;
  }

  @Override
  public Mono<Void> execute(VoiceStateUpdateEvent event) {
    return Mono.just(event)
        .filter(e -> e.isJoinEvent() || e.isMoveEvent())
        .filterWhen(e -> channelService.isCreateChannel(e.getCurrent().getChannel()))
        .flatMap(e -> e.getCurrent().getGuild()
            .flatMap(channelService::createNewVoiceChannel)
            .doOnSuccess(nc -> log.info("Created voice channel {} ({}) in guild {}",
                nc.getName(), nc.getId(), e.getCurrent().getGuildId())))
        .flatMap(newChannel -> event.getCurrent().getMember()
            .flatMap(m -> m.edit(
                guildMemberEditSpec -> guildMemberEditSpec.setNewVoiceChannel(newChannel.getId()))))
        .flatMap(s -> Mono.empty());
  }
}
