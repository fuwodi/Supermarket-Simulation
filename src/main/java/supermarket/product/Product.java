package supermarket.product;

import supermarket.SupermarketConfig;
import java.time.LocalDate;

public abstract class Product {
    protected final String id;
    protected final String batchId;
    protected String name;
    protected ProductType type;
    protected double price;
    protected LocalDate productionDate;
    protected int shelfLifeDays;
    protected double discount;

    public Product(String id, String batchId, String name, ProductType type, double price,
                   LocalDate productionDate, int shelfLifeDays) {
        this.id = id;
        this.batchId = batchId;
        this.name = name;
        this.type = type;
        this.price = price;
        this.productionDate = productionDate;
        this.shelfLifeDays = shelfLifeDays;
        this.discount = 0.0;
    }

    public boolean isExpired(LocalDate currentDate) {
        return currentDate.isAfter(productionDate.plusDays(shelfLifeDays));
    }

    public boolean expiresSoon(LocalDate currentDate) {
        return currentDate.plusDays(SupermarketConfig.DAYS_FOR_DISCOUNT).isAfter(productionDate.plusDays(shelfLifeDays));
    }

    public double getFinalPrice() {
        return price * (1 - discount);
    }

    public LocalDate getExpiryDate() {
        return productionDate.plusDays(shelfLifeDays);
    }

    public String getId() { return id; }
    public String getBatchId() { return batchId; }
    public String getName() { return name; }
    public ProductType getType() { return type; }
    public double getPrice() { return price; }
    public LocalDate getProductionDate() { return productionDate; }
    public int getShelfLifeDays() { return shelfLifeDays; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    @Override
    public String toString() {
        return String.format("%s [%s] - %.2f руб. (скидка: %.0f%%)",
                name, id, getFinalPrice(), discount * 100);
    }
}