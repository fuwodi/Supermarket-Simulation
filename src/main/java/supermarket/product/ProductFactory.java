package supermarket.product;

import java.time.LocalDate;
import java.util.Random;

public class ProductFactory {
    private static Random random = new Random();

    public static Product createRandomProduct(ProductType type) {
        ProductCatalog.ProductInfo info = ProductCatalog.getRandomProductInfo(type);

        String id = info.getId();
        String batchId = generateBatchId();
        LocalDate productionDate = LocalDate.now().minusDays(random.nextInt(5));
        int shelfLife = type.getShelfLifeDays();

        if (ProductCatalog.isCountableType(type)) {
            int quantity = 20 + random.nextInt(30);
            return new CountableProduct(id, batchId, info.getName(), type,
                    info.getBasePrice(), productionDate, shelfLife, quantity);
        } else {
            double weight = 5.0 + random.nextDouble() * 10.0;
            return new WeightableProduct(id, batchId, info.getName(), type,
                    info.getBasePrice(), productionDate, shelfLife, weight);
        }
    }

    public static Product createProductById(String productId) {
        ProductCatalog.ProductInfo info = ProductCatalog.findProductById(productId);
        if (info == null) {
            throw new IllegalArgumentException("Продукт не найден по ID: " + productId);
        }

        ProductType type = ProductCatalog.getProductTypeById(productId);
        String id = productId;
        String batchId = generateBatchId();
        LocalDate productionDate = LocalDate.now().minusDays(random.nextInt(3));
        int shelfLife = type.getShelfLifeDays();

        if (ProductCatalog.isCountableType(type)) {
            int quantity = 25;
            return new CountableProduct(id, batchId, info.getName(), type,
                    info.getBasePrice(), productionDate, shelfLife, quantity);
        } else {
            double weight = 8.0;
            return new WeightableProduct(id, batchId, info.getName(), type,
                    info.getBasePrice(), productionDate, shelfLife, weight);
        }
    }

    private static String generateBatchId() {
        return "BATCH_" + LocalDate.now() + "_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
    }
}