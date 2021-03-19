package com.cwild.discord.listener;

import discord4j.core.event.domain.Event;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class EventListener<T extends Event> {

  public abstract Class<T> getEventType();

  public abstract Mono<Void> execute(T event);

  public Mono<Void> handleError(Throwable error) {
    log.error("Unable to process {}", getEventType().getSimpleName(), error);
    return Mono.empty();
  }
}
