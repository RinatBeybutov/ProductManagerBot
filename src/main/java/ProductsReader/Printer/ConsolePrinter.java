package ProductsReader.Printer;

public class ConsolePrinter implements Printer{

  @Override
  public void print(String string) {
    System.out.print(string);
  }
}
