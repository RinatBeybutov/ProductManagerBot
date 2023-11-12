package ProductsReader.Reader;

import ProductsReader.Database;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CustomFileReader implements Reader{

  private File baseFolder;

  private Database database = Database.getInstance();

  public CustomFileReader(String baseFolderPath) {
    this.baseFolder = new File(baseFolderPath);
  }

  @Override
  public void read() {
    database.clearProducts();
    Arrays.stream(baseFolder.listFiles())
        .filter(file -> file.getName().contains("txt"))
        .forEach(file -> {
          try {
            readFile(file.getAbsolutePath(), file.getName());
          } catch (IOException e) {
            System.out.println("Error during reading file " + file.getAbsolutePath());
          }
        });
  }

  private void readFile(String filePath, String fileName) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      List<String> fileLines = reader.lines().toList();
      database.addCheckItem(fileName, fileLines);
      fileLines.forEach(this::parseLine);
    }
  }

  private void parseLine(String line) {
    String[] items = line.split(" ");
    if(items.length == 2) {
      String productName = items[0];
      int productCost = Integer.parseInt(items[1]);
      database.updateProductItem(productName, productCost);
    }
  }
}
