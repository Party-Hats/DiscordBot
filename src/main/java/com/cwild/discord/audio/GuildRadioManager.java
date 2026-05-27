package com.cwild.discord.audio;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
@RequiredArgsConstructor
public class GuildRadioManager {

  private final RadioUIFactory radioUIFactory;

  private final Map<Snowflake, GuildRadio> managers = new ConcurrentHashMap<>();

  public GuildRadio of(final Guild guild) {
    return managers.computeIfAbsent(guild.getId(), id -> {
      GuildRadio guildRadio = new GuildRadio(guild);
      radioUIFactory.createRadioUIForGuildRadio(guildRadio);
      return guildRadio;
    });
  }
}
