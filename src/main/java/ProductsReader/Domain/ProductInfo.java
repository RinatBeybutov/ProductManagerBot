package ProductsReader.Domain;

public class ProductInfo implements Comparable<ProductInfo>{
  private int count;
  private int totalCost;

  public ProductInfo(int cost) {
    count = 1;
    totalCost = cost;
  }

  public void increaseCost(int cost) {
    totalCost += cost;
    count++;
  }

  public int getTotalCost() {
    return totalCost;
  }

  public int getCount() {
    return count;
  }

  @Override
  public int compareTo(ProductInfo o) {
    return o.getTotalCost() - this.getTotalCost();
  }
}
