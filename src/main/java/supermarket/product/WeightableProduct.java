package supermarket.product;

import java.time.LocalDate;

public class WeightableProduct extends Product {
    private double weight;

    public WeightableProduct(String id, String batchId, String name, ProductType type, double price,
                             LocalDate productionDate, int shelfLifeDays, double weight) {
        super(id, batchId, name, type, price, productionDate, shelfLifeDays);
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void decreaseWeight(double amount) {
        this.weight -= amount;
    }
}