import ProductsReader.ReaderManager;

public class Main {

  public static void main(String[] args) {
    String path = "C://Users//Админ/Desktop/Чеки продукты";

    ReaderManager readerManager = new ReaderManager(path);
    readerManager.start();
  }
}
