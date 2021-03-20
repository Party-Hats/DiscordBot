package com.cwild.discord.config;

import com.cwild.discord.listener.EventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn("radioConfiguration")
public class EventListenerRegistration<T extends Event> {

  public EventListenerRegistration(
      GatewayDiscordClient discordClient,
      List<EventListener<T>> eventListeners) {
    for (EventListener<T> eventListener : eventListeners) {
      discordClient.on(eventListener.getEventType())
          .flatMap(eventListener::execute)
          .onErrorResume(eventListener::handleError)
          .subscribe();
    }
  }
}
