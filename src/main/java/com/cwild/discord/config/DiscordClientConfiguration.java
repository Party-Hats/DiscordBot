package com.cwild.discord.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordClientConfiguration {

  @Value("${discord.token.filepath}")
  private Path tokenFilePath;

  @Bean
  public GatewayDiscordClient gatewayDiscordClient()
      throws IOException {
    if (!Files.exists(tokenFilePath)) {
      throw new IllegalArgumentException(
          "Could not find token file in path \"" + tokenFilePath + "\""
              + (!tokenFilePath.isAbsolute() ? "; absolute path: \""
              + tokenFilePath.toAbsolutePath() + "\"" : ""));
    }
    String token = Files.readString(tokenFilePath).trim();

    return DiscordClientBuilder.create(token)
        .build()
        .login()
        .block();
  }
}
