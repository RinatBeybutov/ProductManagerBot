package ProductsReader.Printer;

import java.io.FileWriter;
import java.io.IOException;

public class FilePrinter implements Printer {

  private String resultsFilePath;
  private String rootPath;

  public FilePrinter(String rootPath) {
    this.rootPath = rootPath;
    resultsFilePath = rootPath + "/Results.txt";
  }

  public void init() {
    try (FileWriter fileWriter = new FileWriter(resultsFilePath)) {
      fileWriter.write("");
    } catch (IOException e) {
      System.out.println("Error during erasing");
    }
  }

  @Override
  public void print(String string) {
    try (FileWriter fileWriter = new FileWriter(resultsFilePath, true)) {
      fileWriter.write(string);
    } catch (IOException e) {
      System.out.println("Error during writing string to file - " + string);
    }
  }

  @Override
  public void print(String file, String text) {
    try (FileWriter fileWriter = new FileWriter(rootPath + "/" +file + ".txt", true)) {
      fileWriter.write(text + "\n");
    } catch (IOException e) {
      System.out.println("Error during writing string to file - " + text);
    }
  }
}
