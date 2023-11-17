package com.customBot.v1.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.yaml")
@Data
public class BotConfig {

  @Value("${bot.name}")
  private String botName;

  @Value("${bot.token}")
  private String token;

  //@Value("${files.path}")
  private String filesPath = "C://Users//Админ/Desktop/Чеки продукты";

  private final Long myChatId = Long.valueOf(447300915l);

}
