package com.cwild.discord.config;

import com.cwild.discord.listener.EventListener;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {

  @Value("${discord.token.filepath}")
  private Path tokenFilePath;

  @Bean
  public <T extends Event> GatewayDiscordClient gatewayDiscordClient(List<EventListener<T>> eventListeners)
      throws IOException {
    if (!Files.exists(tokenFilePath)) {
      throw new IllegalArgumentException(
          "Could not find token file in path \"" + tokenFilePath + "\""
              + (!tokenFilePath.isAbsolute() ? "; absolute path: \""
              + tokenFilePath.toAbsolutePath() + "\"" : ""));
    }
    String token = Files.readString(tokenFilePath);

    GatewayDiscordClient discordClient = DiscordClientBuilder.create(token)
        .build()
        .login()
        .block();

    for (EventListener<T> eventListener : eventListeners) {
      discordClient.on(eventListener.getEventType())
          .flatMap(eventListener::execute)
          .onErrorResume(eventListener::handleError)
          .subscribe();
    }

    return discordClient;
  }
}
