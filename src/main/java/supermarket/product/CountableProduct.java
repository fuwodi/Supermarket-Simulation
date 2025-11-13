package supermarket.product;

import java.time.LocalDate;

public class CountableProduct extends Product {
    private int quantity;

    public CountableProduct(String id, String batchId, String name, ProductType type, double price,
                            LocalDate productionDate, int shelfLifeDays, int quantity) {
        super(id, batchId, name, type, price, productionDate, shelfLifeDays);
        this.quantity = quantity;
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void decreaseQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" - %d шт.", quantity);
    }
}