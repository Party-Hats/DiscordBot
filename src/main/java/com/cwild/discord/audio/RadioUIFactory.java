package com.cwild.discord.audio;

import com.cwild.discord.listener.RadioControlsListener;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.GenericWebApplicationContext;

@Component
@RequiredArgsConstructor
public class RadioUIFactory {

  private final GenericWebApplicationContext applicationContext;
  private final GatewayDiscordClient gatewayDiscordClient;

  public void createRadioUIForGuildRadio(GuildRadio guildRadio) {
    RadioUI radioUI = new RadioUI(guildRadio.getGuild(), gatewayDiscordClient);
    String beanName =
        RadioControlsListener.class.getSimpleName() + "-" + guildRadio.getGuildId().asString();
    applicationContext.registerBean(beanName, RadioControlsListener.class,
        () -> new RadioControlsListener(radioUI, guildRadio));
  }
}
