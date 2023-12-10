package com.customBot.v1.service;

import com.customBot.v1.domain.ProductInfo;
import com.customBot.v1.entity.CheckEntity;
import com.customBot.v1.repository.CheckRepository;
import com.customBot.v1.entity.NoteEntity;
import com.customBot.v1.repository.NoteRepository;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Service
@RequiredArgsConstructor
public class MessageCreator {

  private final CheckRepository checkRepository;

  private final NoteRepository noteRepository;

  @Transactional
  public String createAllProductsAnswer(Long chatId) {
    AtomicInteger sum = new AtomicInteger();

    String products = getSortedProducts(chatId).stream()
        .peek(entry -> sum.addAndGet(entry.getValue().getTotalCost()))
        .map(entry -> String.format("%s - %s шт, всего - %s руб",
            entry.getKey(),
            entry.getValue().getCount(),
            entry.getValue().getTotalCost()))
        .collect(Collectors.joining("\n"));

    String totalCost = String.format("\n\nОбщая стоимость - %s руб\n", sum.get());

    return products + totalCost;
  }

  @Transactional
  public String createTop5ProductsAnswer(Long chatId) {
    return getSortedProducts(chatId)
        .stream()
        .limit(5)
        .map(entry -> String.format("%s - %s шт, всего - %s руб",
            entry.getKey(),
            entry.getValue().getCount(),
            entry.getValue().getTotalCost()))
        .collect(Collectors.joining("\n"));
  }

  @Transactional
  public SendMessage createChecksListMessage(Long chatId, String command, String title) {
    SendMessage m = new SendMessage();
    m.setChatId(String.valueOf(chatId));
    m.setText(title);

    DateFormat format = new SimpleDateFormat("dd.MM.yy");

    List<List<InlineKeyboardButton>> listButtons = checkRepository.getCheckDates(chatId)
        .stream()
        .map(format::format)
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

  @Transactional
  public String getCheckAnswer(Long chatId, String date) {
    try {
      CheckEntity check = checkRepository.findByChatIdAndDate(chatId, new SimpleDateFormat("dd.MM.yy").parse(date));
      return check.getText() + "\n" + date;
    } catch (ParseException e) {
      return "Ошибка обработки даты";
    }
  }

  public String createGreetingAnswer(String username) {
    String greetings = """
       Приветствую тебя %s ;) Есть следующие команды:
       1. /products - Получение полного списка всех продуктов с их суммарной стоимостью
       2. /top5 - Получение продуктов, на которые было потрачено больше всего денег 
       3. /check - Просмотреть конкретный чек
       4. /delete1 - Удалить конкретный чек
       5. Для добавления чека нужно соблюдать следующий формат:
          
          Чек
          29.11.23
          Продукт1 50
          Сложный-продукт 100
          Магазинский-магазин
          
          ** Наличие лишних пробелов в названии продукта и магазина сломает парсинг
       6. Для добавления заметки нужно соблюдать следующий формат:
          
          Заметка
          Продукт простой
          Продукт сложный
          Камбала
          Соевый соус
       7. /note - Для просмотра заметки
       8. /delete2 - для удаления заметки 
       """;
    return String.format(greetings, username);
  }

  @Transactional
  public String createAddingCheckAnswer(Long chatId, String messageText) {
    String[] messageLines = messageText.split("\n");
    String date = messageLines[1];
    List<String> lines = Arrays.stream(messageLines).skip(2).collect(Collectors.toList());

    CheckEntity checkEntity = new CheckEntity();
    checkEntity.setChatId(chatId);
    checkEntity.setText(String.join("\n", lines));
    DateFormat df = new SimpleDateFormat("dd.MM.yy");
    try {
      checkEntity.setDate(df.parse(date));
    } catch (ParseException e) {
      return "Ошибка обработки даты, введите в формате: 08.12.23";
    }
    checkRepository.save(checkEntity);
    return "Добавлен чеееек";
  }

  @Transactional
  public String createDeleteCheckMessage(Long chatId, String date) {
    try {
      checkRepository.deleteByChatIdAndDate(chatId, new SimpleDateFormat("dd.MM.yy").parse(date));
    } catch (ParseException e) {
      System.out.println("error parse");
    }
    return "Удален чек " + date;
  }

  @Transactional
  public String createSaveNoteAnswer(Long chatId, String messageText) {
    String[] messageLines = messageText.split("\n");
    String text = Arrays.stream(messageLines).skip(1).collect(Collectors.joining("\n"));
    NoteEntity noteEntity = new NoteEntity();
    noteEntity.setChatId(chatId);
    noteEntity.setText(text);
    noteRepository.deleteAllByChatId(chatId);
    noteRepository.save(noteEntity);
    return "Добавлена заметка";
  }

  @Transactional
  public String createDeleteNoteAnswer(Long chatId) {
    noteRepository.deleteAllByChatId(chatId);
    return "Заметка удалена";
  }

  @Transactional
  public String createGetNoteAnswer(Long chatId) {
    return noteRepository.findByChatId(chatId).orElse("Заметка пуста");
  }

  private Map<String, ProductInfo> getProductInfoMap(List<String> lines) {
    Map<String, ProductInfo> nameToProductInfo = new HashMap<>();
    for (String line : lines) {
      String[] items = line.split(" ");
      if (items.length == 2) {
        String productName = items[0];
        int price = Integer.parseInt(items[1]);
        if (nameToProductInfo.containsKey(productName)) {
          ProductInfo productInfo = nameToProductInfo.get(productName);
          productInfo.increaseCost(price);
        } else {
          nameToProductInfo.put(productName, new ProductInfo(price));
        }
      }
    }
    return nameToProductInfo;
  }

  private List<Entry<String, ProductInfo>> getSortedProducts(Long chatId) {
    var productLines = getProductLinesFromChecks(chatId);
    var nameToProductInfo = getProductInfoMap(productLines);
    return sortProductMap(nameToProductInfo);
  }

  private List<String> getProductLinesFromChecks(Long chatId) {
    return checkRepository.findAllByChatId(chatId)
        .stream()
        .map(CheckEntity::getText)
        .flatMap(text -> Arrays.stream(text.split("\n")))
        .collect(Collectors.toList());
  }

  private List<Entry<String, ProductInfo>> sortProductMap(Map<String, ProductInfo> nameToProductInfo) {
    return nameToProductInfo.entrySet()
        .stream()
        .sorted(Entry.comparingByValue())
        .collect(Collectors.toList());
  }
}
