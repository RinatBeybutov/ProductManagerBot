package com.customBot.v1.service;

import com.customBot.v1.config.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

  private final BotConfig botConfig;

  private final MessageCreator messageCreator;

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
    if (update.hasMessage() && update.getMessage().hasText()) {
      processTextMessage(update);
    } else if (update.hasCallbackQuery()) {
      processCallBackQuery(update);
    }
  }

  private void processTextMessage(Update update) {
    Message receivedMessage = update.getMessage();
    String messageText = receivedMessage.getText();
    Long chatId = receivedMessage.getChatId();
    String username = receivedMessage.getChat().getFirstName();
    Command command = getCommand(messageText);

    log.info(String.format("User with name = %s and chatId = %s sent command - %s",
        username, chatId, command));

    switch (command) {
      case START_COMMAND:
        createAndSendMessage(chatId, messageCreator.createGreetingAnswer(username));
        break;
      case GET_ALL_PRODUCTS_LIST_COMMAND:
        createAndSendMessage(chatId, messageCreator.createAllProductsAnswer(chatId));
        break;
      case GET_TOP5_EXPENSIVE_PRODUCTS_COMMAND:
        createAndSendMessage(chatId, messageCreator.createTop5ProductsAnswer(chatId));
        break;
      case GET_CHECK_FOR_VIEW_COMMAND:
        SendMessage message = messageCreator.createChecksListMessage(chatId, ":get", "Список чеков");
        sendMessage(message);
        break;
      case ADD_CHECK_COMMAND:
        createAndSendMessage(chatId, messageCreator.createAddingCheckAnswer(chatId, messageText));
        break;
      case GET_CHECK_FOR_DELETE_COMMAND:
        message = messageCreator.createChecksListMessage(chatId, ":delete", "Удалить чек");
        sendMessage(message);
        break;
      case DELETE_NOTE_COMMAND:
        createAndSendMessage(chatId, messageCreator.createDeleteNoteAnswer(chatId));
        break;
      case GET_NOTE_COMMAND:
        createAndSendMessage(chatId, messageCreator.createGetNoteAnswer(chatId));
        break;
      case ADD_NOTE_COMMAND:
        createAndSendMessage(chatId, messageCreator.createSaveNoteAnswer(chatId, messageText));
        break;
      default:
        createAndSendMessage(chatId, "Такая команда не поддерживается, попробуй /start");
    }
  }

  private void processCallBackQuery(Update update) {
    String callData = update.getCallbackQuery().getData();
    String[] callDataItems = callData.split(":");
    String date = callDataItems[0];
    String command = callDataItems[1];
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    if(command.equals("get")) {
      createAndSendMessage(chatId, messageCreator.getCheckAnswer(chatId, date));
    }
    if(command.equals("delete")) {
      createAndSendMessage(chatId, messageCreator.createDeleteCheckMessage(chatId, date));
    }
  }

  private Command getCommand(String messageText) {
    if (messageText.startsWith("Чек")) {
      return Command.ADD_CHECK_COMMAND;
    } else if (messageText.startsWith("Заметка")) {
      return Command.ADD_NOTE_COMMAND;
    }
    return Command.getCommandByTextCommand(messageText);
  }

  private void sendMessage(SendMessage message) {
    try {
      execute(message);
    } catch (TelegramApiException e) {
      System.out.println("Error while sending message " + message);
    }
  }

  private void createAndSendMessage(Long chatId, String text) {
    SendMessage message = SendMessage.builder()
        .chatId(String.valueOf(chatId))
        .text(text)
        .build();
    try {
      execute(message);
    } catch (TelegramApiException e) {
      System.out.println("Error while sending message " + message);
    }
  }
}
