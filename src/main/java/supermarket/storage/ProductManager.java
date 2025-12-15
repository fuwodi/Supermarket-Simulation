package supermarket.storage;

import supermarket.SupermarketConfig;
import supermarket.product.*;

import java.time.LocalDate;
import java.util.*;

public class ProductManager {
    private final Warehouse warehouse;
    private final SalesHall salesHall;
    private final Random random;

    public ProductManager(Warehouse warehouse, SalesHall salesHall) {
        this.warehouse = warehouse;
        this.salesHall = salesHall;
        this.random = new Random();
    }

    public void checkAndRestockAll() {
        System.out.println("\n🔄 Товаровед проверяет запасы...");

        checkAndRestockWarehouse();

        checkAndRestockSalesHall();
    }

    public void checkAndRestockWarehouse() {
        if (warehouse.isEmpty() || warehouse.needsRestocking()) {
            handleWarehouseRestocking();
        } else {
            restockLowWarehouseItems();
        }
    }

    private void handleWarehouseRestocking() {
        if (warehouse.isEmpty()) {
            System.out.println("⚠️ Товаровед: Склад пуст! Срочное пополнение...");
            emergencyWarehouseDelivery(12 + random.nextInt(8));
        } else if (warehouse.needsRestocking()) {
            System.out.println("⚠️ Товаровед: На складе мало товаров. Пополняем...");
            emergencyWarehouseDelivery(8 + random.nextInt(5));
        }
    }

    private void emergencyWarehouseDelivery(int count) {
        int addedCount = 0;
        for (int i = 0; i < count; i++) {
            ProductType randomType = ProductType.values()[random.nextInt(ProductType.values().length)];
            Product product = ProductFactory.createRandomProduct(randomType);
            if (warehouse.addProduct(product, LocalDate.now())) {
                addedCount++;
                System.out.println("   📦 На склад добавлен: " + product.getName());
            }
        }
        System.out.println("   ✅ Товаровед пополнил склад: +" + addedCount + " товаров");
    }

    private void restockLowWarehouseItems() {
        List<String> lowStockProducts = warehouse.getLowStockProductIds();
        int restockedCount = 0;

        for (String productId : lowStockProducts) {
            if (restockedCount >= 3) break;

            String productName = ProductCatalog.getProductNameById(productId);
            System.out.println("   🔄 Товаровед пополняет на складе: " + productName);

            int batchesToAdd = 1 + random.nextInt(2);
            for (int i = 0; i < batchesToAdd; i++) {
                Product product = ProductFactory.createProductById(productId);
                if (warehouse.addProduct(product, LocalDate.now())) {
                    restockedCount++;
                    System.out.println("     📦 Добавлена партия: " + product.getName());
                }
            }
        }

        if (restockedCount > 0) {
            System.out.println("   ✅ Товаровед пополнил " + restockedCount + " позиций на складе");
        }
    }

    public void checkAndRestockSalesHall() {
        if (salesHall.getTotalProducts() == 0) {
            transferProductsToHall();
            return;
        }

        List<String> lowStockProducts = salesHall.getLowStockProductIds();
        int restockedCount = 0;

        for (String productId : lowStockProducts) {
            if (restockedCount >= 5) break;

            if (needsRestocking(productId) && warehouse.getTotalAmount(productId) > 0) {
                restockProduct(productId);
                restockedCount++;
            }
        }

        if (restockedCount == 0 && !lowStockProducts.isEmpty()) {
            System.out.println("   ℹ️ Товаровед: Нет товаров на складе для пополнения зала");
        }
    }

    public void transferProductsToHall() {
        System.out.println("\n🔄 Перемещение товаров в зал:");

        int transferredCount = 0;
        int maxTransfers = 10;

        Map<String, List<Product>> allProducts = warehouse.getAllProducts();
        List<Product> availableProducts = new ArrayList<>();

        // Собираем все доступные товары
        for (Map.Entry<String, List<Product>> entry : allProducts.entrySet()) {
            availableProducts.addAll(entry.getValue());
        }

        // Сортируем по приоритету: сначала товары с низкими запасами в зале
        availableProducts.sort((p1, p2) -> {
            double stock1 = salesHall.getTotalAmount(p1.getId());
            double stock2 = salesHall.getTotalAmount(p2.getId());
            return Double.compare(stock1, stock2); // Сначала самые низкие запасы
        });

        for (Product product : availableProducts) {
            if (transferredCount >= maxTransfers) break;

            String productId = product.getId();
            double hallStock = salesHall.getTotalAmount(productId);
            double minStock = getMinStockForProduct(productId);

            // Перемещаем только если нужно пополнить
            if (hallStock < minStock * 1.5) {
                if (salesHall.addProduct(product, LocalDate.now())) {
                    warehouse.removeBatch(productId, product.getBatchId());
                    transferredCount++;
                }
            }
        }

        if (transferredCount > 0) {
            System.out.println("   ✅ Перемещено: " + transferredCount + " товаров");
        } else {
            System.out.println("   ℹ️ Перемещение не требуется");
        }
    }

    public void restockProduct(String productId) {
        List<Product> warehouseBatches = warehouse.findProductsById(productId);
        int restocked = 0;

        for (Product product : warehouseBatches) {
            if (salesHall.addProduct(product, LocalDate.now())) {
                warehouse.removeBatch(productId, product.getBatchId());
                restocked++;
                break;
            }
        }

        if (restocked > 0) {
            String productName = ProductCatalog.getProductNameById(productId);
            System.out.println("   🔄 Товаровед пополнил в зале: " + productName);
        }
    }

    private boolean needsRestocking(String productId) {
        double currentAmount = salesHall.getTotalAmount(productId);
        double minStock = getMinStockForProduct(productId);
        return currentAmount < minStock;
    }

    private double getMinStockForProduct(String productId) {
        Product product = salesHall.getProduct(productId);
        if (product == null) {
            return 1.0;
        }
        if (product instanceof CountableProduct) {
            return SupermarketConfig.SALES_HALL_MIN_COUNTABLE;
        } else if (product instanceof WeightableProduct) {
            return SupermarketConfig.SALES_HALL_MIN_WEIGHTABLE;
        }
        return 1.0;
    }

    public void generateDelivery() {
        int productsToAdd;

        if (warehouse.isEmpty()) {
            productsToAdd = 15 + random.nextInt(10);
            System.out.println("🚨 Товаровед: Склад пуст! Заказываем большую партию...");
        } else if (warehouse.needsRestocking()) {
            productsToAdd = 8 + random.nextInt(7);
            System.out.println("⚠️ Товаровед: Заказываем товары для пополнения склада...");
        } else {
            productsToAdd = 3 + random.nextInt(4);
            System.out.println("📦 Товаровед: Регулярная доставка на склад...");
        }

        int addedCount = 0;
        for (int i = 0; i < productsToAdd; i++) {
            ProductType randomType = ProductType.values()[random.nextInt(ProductType.values().length)];
            Product product = ProductFactory.createRandomProduct(randomType);
            if (warehouse.addProduct(product, LocalDate.now())) {
                addedCount++;
                System.out.println("   📦 Доставлен на склад: " + product.getName());
            }
        }

        if (addedCount > 0) {
            System.out.println("   ✅ Товаровед принял на склад: " + addedCount + " товаров");
        }
    }
}