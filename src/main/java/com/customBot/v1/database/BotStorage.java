package com.customBot.v1.database;

import ProductsReader.Domain.ProductInfo;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BotStorage {

  private Map<Long, Map<String, ProductInfo>> chatIdToListProducts = new HashMap<>();

  private Map<Long, Map<String, List<String>>> chatIdToListChecks = new HashMap<>();

  private static final BotStorage storage = new BotStorage();

  private BotStorage() {}

  public static BotStorage getInstance() {
    return storage;
  }

  public void addCheck(Long chatId, String fileName, List<String> lines) {
    if(chatIdToListChecks.containsKey(chatId)) {
      Map<String, List<String>> filenameToLines = chatIdToListChecks.get(chatId);
      filenameToLines.put(fileName, lines);
    } else {
      Map<String, List<String>> nameToCheck = new HashMap<>();
      nameToCheck.put(fileName, lines);
      chatIdToListChecks.put(chatId, nameToCheck);
    }
  }

  public void addMapChecks(Long chatId, Map<String, List<String>> checks) {
    chatIdToListChecks.remove(chatId);
    chatIdToListChecks.put(chatId, checks);
  }

  public List<Entry<String, ProductInfo>> getSortedProducts(Long chatId)
      throws FileNotFoundException {
    var nameToProductInfo = getProductInfoMap(getProductLinesFromChecks(chatId));
    chatIdToListProducts.put(chatId, nameToProductInfo);
    return sortProductMap(nameToProductInfo);
  }

  public List<Entry<String, ProductInfo>> getTop5Products(Long chatId)
      throws FileNotFoundException {
    return getSortedProducts(chatId)
        .stream().limit(5)
        .collect(Collectors.toList());
  }

  public int getTotalCost(Long chatId) {
    return chatIdToListProducts.get(chatId).values()
        .stream()
        .map(ProductInfo::getTotalCost)
        .mapToInt(num -> num)
        .sum();
  }

  // ----- Private methods  ----- //

  private List<String> getProductLinesFromChecks(Long chatId) throws FileNotFoundException {
    if(chatIdToListChecks.get(chatId) == null) {
      throw new FileNotFoundException();
    }
    return chatIdToListChecks.get(chatId).values()
        .stream()
        .flatMap(Collection::stream)
        .toList();
  }

  private List<Entry<String, ProductInfo>> sortProductMap(Map<String, ProductInfo> nameToProductInfo) {
    return nameToProductInfo.entrySet()
        .stream()
        .sorted(Entry.comparingByValue())
        .collect(Collectors.toList());
  }

  private Map<String, ProductInfo> getProductInfoMap(List<String> lines) {
    Map<String, ProductInfo> nameToProductInfo = new HashMap<>();
    for(String line : lines) {
      String[] items = line.split(" ");
      if(items.length == 2) {
        String productName = items[0];
        int price = Integer.parseInt(items[1]);
        if(nameToProductInfo.containsKey(productName)) {
          ProductInfo productInfo = nameToProductInfo.get(productName);
          productInfo.increaseCost(price);
        } else {
          nameToProductInfo.put(productName, new ProductInfo(price));
        }
      }
    }
    return nameToProductInfo;
  }

  public List<Entry<String, Integer>> getCheckToTotalCost(Long chatId)
      throws FileNotFoundException {
    if(chatIdToListChecks.get(chatId) == null) {
      throw new FileNotFoundException();
    }
    return chatIdToListChecks.get(chatId).entrySet()
        .stream()
        .map(entry -> {
          return Map.entry(entry.getKey(), entry.getValue().stream().map(line -> {
            String[] items = line.split(" ");
            if (items.length == 2) {
              return Integer.parseInt(items[1]);
            } else {
              return 0;
            }
          }).mapToInt(e -> e).sum());
        }).collect(Collectors.toList());
  }
}
