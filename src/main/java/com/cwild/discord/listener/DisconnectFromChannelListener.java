package com.cwild.discord.listener;

import com.cwild.discord.service.ChannelService;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.channel.Channel;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectFromChannelListener extends EventListener<VoiceStateUpdateEvent> {

  private final ChannelService channelService;

  @Override
  public Class<VoiceStateUpdateEvent> getEventType() {
    return VoiceStateUpdateEvent.class;
  }

  @Override
  public Mono<Void> execute(VoiceStateUpdateEvent event) {
    return Mono.just(event)
        .filter(e -> e.isLeaveEvent() || e.isMoveEvent())
        .map(e -> e.getOld().orElse(null))
        .filter(Objects::nonNull)
        .filterWhen(v -> channelService.isCreateChannel(v.getChannel())
            .map(b -> !b))
        .filterWhen(e -> channelService.hasNoMembers(e.getChannel()))
        .filterWhen(e -> channelService.isLimited(e.getChannel()))
        .flatMap(e -> {
          log.info("Deleting channel {} in guild {}", e.getChannelId().orElse(null),
              e.getGuildId());
          return e.getChannel().flatMap(Channel::delete);
        });
  }
}
