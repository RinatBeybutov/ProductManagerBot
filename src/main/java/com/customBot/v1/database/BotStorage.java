package com.customBot.v1.database;

import ProductsReader.Domain.ProductInfo;
import ProductsReader.ReaderManager;
import com.customBot.v1.config.BotConfig;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class BotStorage {

  private Map<Long, Map<String, ProductInfo>> idToListProducts = new HashMap<>();

  private Map<Long, Map<String, List<String>>> idToListChecks = new HashMap<>();

  private Map<Long, String> idToNote = new HashMap<>();

  private final BotConfig botConfig = new BotConfig();

  private static final BotStorage storage = new BotStorage();

  private final ReaderManager manager;

  private BotStorage() {
    manager = new ReaderManager(botConfig.getFilesPath());
  }

  public static BotStorage getInstance() {
    return storage;
  }

  public void addCheck(Long chatId, String fileName, List<String> lines) {
    manager.saveCheck(fileName, lines);
    if (idToListChecks.containsKey(chatId)) {
      Map<String, List<String>> filenameToLines = idToListChecks.get(chatId);
      filenameToLines.put(fileName, lines);
    } else {
      Map<String, List<String>> nameToCheck = new HashMap<>();
      nameToCheck.put(fileName, lines);
      idToListChecks.put(chatId, nameToCheck);
    }
  }

  public List<Entry<String, ProductInfo>> getSortedProducts(Long chatId)
      throws FileNotFoundException {
    readChecksFromFiles();
    var productLines = getProductLinesFromChecks(chatId);
    var nameToProductInfo = getProductInfoMap(productLines);
    idToListProducts.put(chatId, nameToProductInfo);
    return sortProductMap(nameToProductInfo);
  }

  public List<Entry<String, ProductInfo>> getTop5Products(Long chatId)
      throws FileNotFoundException {
    readChecksFromFiles();
    return getSortedProducts(chatId)
        .stream().limit(5)
        .collect(Collectors.toList());
  }

  public int getTotalCost(Long chatId) {
    return idToListProducts.get(chatId).values()
        .stream()
        .map(ProductInfo::getTotalCost)
        .mapToInt(num -> num)
        .sum();
  }

  public List<Entry<String, Integer>> getCheckToTotalCost(Long chatId)
      throws FileNotFoundException {
    readChecksFromFiles();
    if (idToListChecks.get(chatId) == null) {
      throw new FileNotFoundException();
    }
    return idToListChecks.get(chatId).entrySet()
        .stream()
        .map(entry -> Map.entry(entry.getKey(), entry.getValue().stream()
            .map(BotStorage::extractPriceFromLine)
            .mapToInt(e -> e)
            .sum()))
        .collect(Collectors.toList());
  }

  public String getCheck(Long chatId, String checkDate) {
    readChecksFromFiles();
    Map<String, List<String>> dateToCheckLines = idToListChecks.get(chatId);
    return dateToCheckLines.get(checkDate).stream().collect(Collectors.joining("\n"));
  }

  public Set<String> getCheckDates(Long chatId) {
    readChecksFromFiles();
    return idToListChecks.get(chatId).keySet();
  }

  public void deleteCheck(Long chatId, String date) {
    Map<String, List<String>> dateToChecks = idToListChecks.get(chatId);
    dateToChecks.remove(date);
    manager.deleteFile(date);
  }

  public void saveNote(Long chatId, String messageText) {
    idToNote.put(chatId, messageText);
  }

  public void deleteNote(Long chatId) {
    idToNote.remove(chatId);
  }

  public String getNote(Long chatId) {
    return idToNote.getOrDefault(chatId, "Заметка пуста");
  }
  // ----- Private methods  ----- //

  private void readChecksFromFiles() {
    if (idToListChecks.containsKey(botConfig.getMyChatId())) {
      Map<String, List<String>> dateToLines = idToListChecks.get(botConfig.getMyChatId());
      dateToLines.putAll(manager.getChecksMap());
      idToListChecks.put(botConfig.getMyChatId(), dateToLines);
    } else {
      idToListChecks.put(botConfig.getMyChatId(), manager.getChecksMap());
    }
  }

  private List<String> getProductLinesFromChecks(Long chatId) throws FileNotFoundException {
    if (idToListChecks.get(chatId) == null) {
      throw new FileNotFoundException();
    }
    return idToListChecks.get(chatId).values()
        .stream()
        .flatMap(Collection::stream)
        .toList();
  }

  private List<Entry<String, ProductInfo>> sortProductMap(
      Map<String, ProductInfo> nameToProductInfo) {
    return nameToProductInfo.entrySet()
        .stream()
        .sorted(Entry.comparingByValue())
        .collect(Collectors.toList());
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

  private static Integer extractPriceFromLine(String line) {
    String[] items = line.split(" ");
    if (items.length == 2) {
      return Integer.parseInt(items[1]);
    } else {
      return 0;
    }
  }
}
