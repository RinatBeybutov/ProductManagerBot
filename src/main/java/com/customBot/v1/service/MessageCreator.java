package com.customBot.v1.service;

import com.customBot.v1.database.BotStorage;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Service
public class MessageCreator {

  private final static String DATABASE_NOT_FOUND_DATA_ANSWER = "База данных отвечает, что у нее нет твоих данных ;(";
  private BotStorage storage = BotStorage.getInstance();

  public String createAllProductsAnswer(Long chatId) {
    try {
      String answer = storage.getSortedProducts(chatId).stream()
          .map(entry -> String.format("%s - %s шт, всего - %s руб",
              entry.getKey(), entry.getValue().getCount(), entry.getValue().getTotalCost()))
          .collect(Collectors.joining("\n"));
      String totalCost = String.format("\nОбщая стоимость - %s руб\n", storage.getTotalCost(chatId));
      return answer + totalCost;
    } catch (FileNotFoundException e) {
      return DATABASE_NOT_FOUND_DATA_ANSWER;
    }
  }

  public String createTop5ProductsAnswer(Long chatId) {
    try {
      String answer = storage.getTop5Products(chatId).stream()
          .map(entry -> String.format("%s - %s шт, всего - %s руб",
              entry.getKey(), entry.getValue().getCount(), entry.getValue().getTotalCost()))
          .collect(Collectors.joining("\n"));
      return answer;
    } catch (FileNotFoundException e) {
      return DATABASE_NOT_FOUND_DATA_ANSWER;
    }
  }


  public String createAllChecksAnswer(Long chatId) {
    try {
      List<Entry<String, Integer>> checkToTotalCost = storage.getCheckToTotalCost(chatId);
      String answer = checkToTotalCost.stream()
          .sorted(Comparator.comparing(Entry::getKey))
          .map(entry -> entry.getKey() + "  -  " + entry.getValue() + "\n")
          .collect(Collectors.joining());
      return answer;
    } catch (FileNotFoundException e) {
      return DATABASE_NOT_FOUND_DATA_ANSWER;
    }
  }

  public SendMessage createChecksListMessage(Long chatId, String command, String title) {
    SendMessage m = new SendMessage();
    m.setChatId(String.valueOf(chatId));
    m.setText(title);

    List<List<InlineKeyboardButton>> listButtons = storage.getCheckDates(chatId)
        .stream()
        .sorted()
        .map(date -> {
          InlineKeyboardButton button = new InlineKeyboardButton(date);
          button.setCallbackData(date + command);
          List<InlineKeyboardButton> row = new ArrayList<>();
          row.add(button);
          return row;
        })
        .toList();

    InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
    keyboardMarkup.setKeyboard(listButtons);
    m.setReplyMarkup(keyboardMarkup);
    return m;
  }

  public String getCheckAnswer(Long chatId, String date) {
    return storage.getCheck(chatId, date) + "\n" + date;
  }

  public String createGreetingAnswer(String username) {
    String greetings = """
       Приветствую тебя %s ;) Есть следующие команды:
       1. /products - Получение полного списка всех продуктов с их суммарной стоимостью
       2. /top5 - Получение продуктов, на которые было потрачено больше всего денег
       3. /totalchecks - Получить списком суммарную стоимость каждого чека
       4. /check - Просмотреть конкретный чек
       5. /delete1 - Удалить конкретный чек
       6. Для добавления чека нужно соблюдать следующий формат:
          
          Чек
          29.11
          Продукт1 50
          Сложный-продукт 100
          Магазинский-магазин
          
          ** Наличие лишних пробелов в названии продукта и магазина сломает парсинг
       7. Для добавления заметки нужно соблюдать следующий формат:
          
          Заметка
          Продукт простой
          Продукт сложный
          Камбала
          Соевый соус
       8. /note - Для просмотра заметки
       9. /delete2 - для удаления заметки 
       """;
    return String.format(greetings, username);
  }

  public String createAddingCheckAnswer(Long chatId, String messageText) {
    String[] messageLines = messageText.split("\n");
    String date = messageLines[1];
    List<String> lines = Arrays.stream(messageLines).skip(2).collect(Collectors.toList());
    storage.addCheck(chatId, date, lines);
    return "Добавлен чеееек";
  }

  public String createDeleteCheckMessage(Long chatId, String date) {
    storage.deleteCheck(chatId, date);
    return "Удален чек " + date;
  }

  public String createSaveNoteAnswer(Long chatId, String messageText) {
    String[] messageLines = messageText.split("\n");
    String note = Arrays.stream(messageLines).skip(1).collect(Collectors.joining("\n"));
    storage.saveNote(chatId, note);
    return "Добавлена заметка";
  }

  public String createDeleteNoteAnswer(Long chatId) {
    storage.deleteNote(chatId);
    return "Заметка удалена";
  }

  public String createGetNoteAnswer(Long chatId) {
    return storage.getNote(chatId);
  }
}
