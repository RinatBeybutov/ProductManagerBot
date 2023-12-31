package ProductsReader;

import ProductsReader.Domain.ProductInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Storage {

  private Map<String, ProductInfo> nameToProductInfo = new HashMap<>();
  private Map<String, List<String>> dateToCheck = new HashMap<>();

  private static final Storage STORAGE = new Storage();

  private Storage() {}

  public static Storage getInstance() {
    return STORAGE;
  }

  public void updateProductItem(String productName, int productCost) {
    if(nameToProductInfo.containsKey(productName)) {
      ProductInfo productInfo = nameToProductInfo.get(productName);
      productInfo.increaseCost(productCost);
    } else {
      nameToProductInfo.put(productName, new ProductInfo(productCost));
    }
  }

  public void addCheckItem(String date, List<String> checkLines) {
    dateToCheck.put(date, checkLines);
  }

  public void deleteCheck(String date) {
    dateToCheck.remove(date);
  }

  public List<Entry<String, ProductInfo>> getSortedProducts() {
    return nameToProductInfo.entrySet()
        .stream()
        .sorted(Entry.comparingByValue())
        .collect(Collectors.toList());
  }

  public void clearStorage() {
    nameToProductInfo.clear();
    dateToCheck.clear();
  }

  public int countTotalCost() {
    return nameToProductInfo.values()
        .stream()
        .map(ProductInfo::getTotalCost)
        .mapToInt(num -> num)
        .sum();
  }

  public Map<String, ProductInfo> getNameToProductInfo() {
    return new HashMap<>(nameToProductInfo);
  }

  public HashMap<String, List<String>> getCheksMap() {
    return new HashMap<>(dateToCheck);
  }
}
