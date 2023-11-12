package ProductsReader;

import ProductsReader.Domain.ProductInfo;
import ProductsReader.Printer.FilePrinter;
import ProductsReader.Printer.Printer;
import ProductsReader.Reader.CustomFileReader;
import ProductsReader.Reader.Reader;
import java.util.List;
import java.util.Map.Entry;

public class ReaderManager {
  private static final long COUNT_TOP_PRICE_LIMIT = 5;
  private static final String DELIMITER_STRING = "--------------------\n";
  private Reader reader;
  private Printer printer;
  private Database database = Database.getInstance();

  public ReaderManager(String path) {
    reader = new CustomFileReader(path);
    //printer = new ConsolePrinter();
    printer = new FilePrinter(path);
  }

  public void start() {
    reader.read();
    var totalCost = database.countTotalCost();
    var sortedProducts = database.getSortedProducts();
    printResults(sortedProducts, totalCost);
  }

  private void printResults(List<Entry<String, ProductInfo>> sortedProducts, int totalCost) {
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
}
