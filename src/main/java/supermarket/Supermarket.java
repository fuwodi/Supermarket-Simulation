package supermarket;

import java.time.LocalDate;
import java.util.Random;

import supermarket.product.Product;
import supermarket.product.ProductFactory;
import supermarket.product.ProductType;

public class Supermarket {
    private Warehouse warehouse;
    private  SalesHall salesHall;
    private LocalDate currentDate;
    private Random random;
    private int dayNumber;

    public Supermarket(LocalDate startDate) {
        this.warehouse = new Warehouse();
        this.salesHall = new SalesHall();
        this.currentDate = startDate;
        this.random = new Random();
        this.dayNumber = 1;

        initializeWithProducts();
    }

    private void initializeWithProducts() {
        for (int i = 0; i < 10; i++) {
            ProductType randomType = ProductType.values()[random.nextInt(ProductType.values().length)];
            Product product = ProductFactory.createRandomProduct(randomType);
            boolean add = warehouse.addProduct(product, currentDate);
            if (add) {
                System.out.println("Добавлен товар: " + product.getName());
            }
        }
        System.out.println("Магазин готов к работе!");

    }
}
