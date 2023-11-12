package ProductsReader;

import ProductsReader.Domain.ProductInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Database {

  Map<String, ProductInfo> nameToProductInfo = new HashMap<>();
  Map<String, List<String>> dateToCheck = new HashMap<>();

  private static final Database database = new Database();

  private Database() {}

  public static Database getInstance() {
    return database;
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

  public void clearProducts() {
    nameToProductInfo.clear();
  }

  public int countTotalCost() {
    return nameToProductInfo.values()
        .stream()
        .map(ProductInfo::getTotalCost)
        .mapToInt(num -> num)
        .sum();
  }
}
