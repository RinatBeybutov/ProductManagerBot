package com.customBot.v1.service;

import ProductsReader.ReaderManager;
import com.customBot.v1.config.BotConfig;
import com.customBot.v1.database.BotStorage;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

  private BotStorage storage = BotStorage.getInstance();

  private final BotConfig botConfig;


  @Override
  public String getBotUsername() {
    return botConfig.getBotName();
  }

  @Override
  public String getBotToken() {
    return botConfig.getToken();
  }

  @Override
  public void onUpdateReceived(Update update) {
    if(update.hasMessage() && update.getMessage().hasText()) {
      String messageText = update.getMessage().getText();
      Long chatId = update.getMessage().getChatId();

      String username = update.getMessage().getChat().getFirstName();

      log.info(String.format("User with name = %s and chatId = %s sent command - %s",
          username, chatId, messageText));

      switch (messageText) {
        case "/start":
          startCommand(chatId, username);
          break;
        case "/products":
          getProductsCommand(chatId);
          break;
        case "/top5":
          getTop5ProductsCommand(chatId);
            break;
        case "/checks":
          getChecksCommand(chatId);
          break;
        default:
          sendMessage(chatId, "Команда пока не поддерживается");
      }
    }
  }

  private void getChecksCommand(Long chatId) {
    try {
      List<Entry<String, Integer>> checkToTotalCost = storage.getCheckToTotalCost(chatId);
      String answer = checkToTotalCost.stream()
          .sorted(Comparator.comparing(Entry::getKey))
          .map(entry -> entry.getKey() + "  -  " + entry.getValue() + "\n")
          .collect(Collectors.joining());
      sendMessage(chatId, answer);
    } catch (FileNotFoundException e) {
      sendMessage(chatId, "База данных отвечает, что у нее нет твоих данных ;(");
    }
  }

  private void getTop5ProductsCommand(Long chatId) {
    try {
      String answer = storage.getTop5Products(chatId).stream()
          .map(entry -> String.format("%s - %s шт, всего - %s руб",
              entry.getKey(), entry.getValue().getCount(), entry.getValue().getTotalCost()))
          .collect(Collectors.joining("\n"));
      sendMessage(chatId, answer);
    } catch (FileNotFoundException e) {
      sendMessage(chatId, "База данных отвечает, что у нее нет твоих данных ;(");
    }
  }

  private void getProductsCommand(Long chatId) {
    try {
      String answer = storage.getSortedProducts(chatId).stream()
          .map(entry -> String.format("%s - %s шт, всего - %s руб",
              entry.getKey(), entry.getValue().getCount(), entry.getValue().getTotalCost()))
          .collect(Collectors.joining("\n"));
      String totalCost = String.format("\nОбщая стоимость - %s руб\n", storage.getTotalCost(chatId));
      sendMessage(chatId, answer + totalCost);
    } catch (FileNotFoundException e) {
      sendMessage(chatId, "База данных отвечает, что у нее нет твоих данных ;(");
    }
  }

  private void startCommand(Long chatId, String name) {
    ReaderManager manager = new ReaderManager(botConfig.getFilesPath());
    storage.addMapChecks(chatId, manager.getChecksMap());
    sendMessage(chatId, "Я закинул в базу инфу из файлов, " + name + " :)");
  }

  private void sendMessage(Long chatId, String text) {
    SendMessage m = new SendMessage();
    m.setChatId(String.valueOf(chatId));
    m.setText(text);

    try {
      execute(m);
    } catch (TelegramApiException e) {
      System.out.println("Error while sendind message " + m);
    }
  }
}
