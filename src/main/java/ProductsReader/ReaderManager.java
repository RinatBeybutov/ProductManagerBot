package ProductsReader;

import ProductsReader.Domain.ProductInfo;
import ProductsReader.Printer.FilePrinter;
import ProductsReader.Printer.Printer;
import ProductsReader.Reader.CustomFileReader;
import ProductsReader.Reader.Reader;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ReaderManager {
  private static final long COUNT_TOP_PRICE_LIMIT = 5;
  private static final String DELIMITER_STRING = "--------------------\n";
  private Reader reader;
  private Printer printer;
  private Storage storage = Storage.getInstance();
  private final String rootPath;

  public ReaderManager(String path) {
    reader = new CustomFileReader(path);
    //printer = new ConsolePrinter();
    printer = new FilePrinter(path);
    rootPath = path;
  }

  public void start() {
    reader.read();
    var totalCost = storage.countTotalCost();
    var sortedProducts = storage.getSortedProducts();
    printResults(sortedProducts, totalCost);
  }

  public Map<String, ProductInfo> getProductsMap() {
    reader.read();
    return storage.getNameToProductInfo();
  }

  public HashMap<String, List<String>> getChecksMap() {
    reader.read();
    return storage.getCheksMap();
  }

  private void printResults(List<Entry<String, ProductInfo>> sortedProducts, int totalCost) {
    printer.init();
    printer.print(DELIMITER_STRING);
    int counter = 1;
    for(Entry<String, ProductInfo> entry : sortedProducts) {
      ProductInfo productInfo = entry.getValue();
      printer.print(String.format("%s) %s - %s шт, всего - %s руб\n",counter++, entry.getKey(), productInfo.getCount(),
          productInfo.getTotalCost()));
    }
    printer.print(DELIMITER_STRING);
    printer.print(String.format("Общая стоимость - %s руб\n",  totalCost));
    printer.print(DELIMITER_STRING);
    printer.print(String.format("Топ %s продуктов по расходам:\n", COUNT_TOP_PRICE_LIMIT));
    sortedProducts.stream()
        .limit(COUNT_TOP_PRICE_LIMIT)
        .forEach(entry -> printer.print(String.format("%s - %s\n", entry.getKey(), entry.getValue().getTotalCost())));
    System.out.println("Результаты записаны!");
  }

  public void saveCheck(String fileName, List<String> lines) {
    printer.print(fileName, lines.stream().collect(Collectors.joining("\n")));
  }

  public void deleteFile(String date) {
    String filePath = rootPath + "/" + date + ".txt";
    File file = new File(filePath);
    file.delete();
  }
}
