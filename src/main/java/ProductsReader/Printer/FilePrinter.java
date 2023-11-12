package ProductsReader.Printer;

import java.io.FileWriter;
import java.io.IOException;

public class FilePrinter implements Printer{

  private String fileAbsolutePath;

  public FilePrinter(String rootPath) {
    fileAbsolutePath = rootPath +"/Results.txt";
    try (FileWriter fileWriter = new FileWriter(fileAbsolutePath)) {
      fileWriter.write("");
    } catch (IOException e) {
      System.out.println("Error during erasing");
    }
  }

  @Override
  public void print(String string) {
    try (FileWriter fileWriter = new FileWriter(fileAbsolutePath, true)) {
      fileWriter.write(string);
    } catch (IOException e) {
      System.out.println("Error during writing string to file - " + string);
    }
  }
}
