package supermarket.storage;

import supermarket.SupermarketConfig;
import supermarket.product.CountableProduct;
import supermarket.product.Product;
import supermarket.product.ProductCatalog;
import supermarket.product.WeightableProduct;

import java.time.LocalDate;
import java.util.*;

public class SalesHall implements ProductStorage {
    private Map<String, Shelf> shelvesByProductId;
    private Map<String, String> productIdToName;

    public SalesHall() {
        this.shelvesByProductId = new HashMap<>();
        this.productIdToName = new HashMap<>();
    }

    // Создать или получить полку для товара
    private Shelf getOrCreateShelf(Product product) {
        String productId = product.getId();

        if (!shelvesByProductId.containsKey(productId)) {
            // Определяем максимальную вместимость для полки в зависимости от типа товара
            double maxCapacity;
            if (ProductCatalog.isCountableType(product.getType())) {
                maxCapacity = SupermarketConfig.SHELF_MAX_COUNTABLE;
            } else {
                maxCapacity = SupermarketConfig.SHELF_MAX_WEIGHTABLE;
            }

            Shelf shelf = new Shelf(productId, maxCapacity);
            shelvesByProductId.put(productId, shelf);
            productIdToName.put(productId, product.getName());
        }

        return shelvesByProductId.get(productId);
    }

    public boolean addProduct(Product product, LocalDate currentDate) {
        // 1. Проверка срока годности
        if (product.isExpired(currentDate)) {
            System.out.println("   ❌ Товар " + product.getName() + " просрочен и не принят в торговый зал");
            return false;
        }

        // 2. Получаем соответствующую полку
        Shelf shelf = getOrCreateShelf(product);

        // 3. Добавляем товар на полку
        double actuallyAdded = shelf.addProduct(product);

        if (actuallyAdded > 0) {
            double originalAmount = getAmountFromProduct(product);

            if (Math.abs(actuallyAdded - originalAmount) > 0.001) {
                // Если добавили не всё
                System.out.println("   📦 " + product.getName() + ": " +
                        formatAmount(actuallyAdded, product) + " из " +
                        formatAmount(originalAmount, product) + " (ограничение полки)");
            } else {
                // Если добавили всё
                System.out.println("   📦 " + product.getName() + ": +" +
                        formatAmount(actuallyAdded, product));
            }
            return true;
        }

        return false;
    }

    private String formatAmount(double amount, Product product) {
        if (product instanceof WeightableProduct) {
            return String.format("%.3f кг", amount);
        } else {
            return String.format("%.0f шт", amount);
        }
    }

    public List<Product> findProductsById(String productId) {
        Shelf shelf = shelvesByProductId.get(productId);
        if (shelf != null) {
            return shelf.getAllBatches();
        }
        return new ArrayList<>();
    }

    public int removeExpiredProducts(LocalDate currentDate) {
        int removed = 0;
        Iterator<Map.Entry<String, Shelf>> iterator = shelvesByProductId.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Shelf> entry = iterator.next();
            Shelf shelf = entry.getValue();
            String productId = entry.getKey();

            // Получаем все партии с полки
            List<Product> batches = shelf.getAllBatches();
            Iterator<Product> batchIterator = batches.iterator();

            while (batchIterator.hasNext()) {
                Product batch = batchIterator.next();
                if (batch.isExpired(currentDate)) {
                    // Удаляем товар с полки
                    if (shelf.removeProduct(batch)) {
                        System.out.println("   🗑️ Утилизирован из зала: " + productIdToName.get(productId) +
                                " (партия: " + batch.getBatchId() + ")");
                        removed++;
                    }
                }
            }

            // Если полка пуста, удаляем ее
            if (shelf.isEmpty()) {
                iterator.remove();
                productIdToName.remove(productId);
            }
        }
        return removed;
    }

    public void applyDiscountToProductById(String productId, double discount) {
        Shelf shelf = shelvesByProductId.get(productId);
        if (shelf != null) {
            for (Product batch : shelf.getAllBatches()) {
                batch.setDiscount(discount);
            }
        }
    }

    public void applyDiscountToBatch(String productId, String batchId, double discount) {
        Shelf shelf = shelvesByProductId.get(productId);
        if (shelf != null) {
            Product batch = shelf.getBatch(batchId);
            if (batch != null) {
                batch.setDiscount(discount);
            }
        }
    }

    public int applyExpiringDiscounts(LocalDate currentDate) {
        int discountCount = 0;
        for (Map.Entry<String, Shelf> entry : shelvesByProductId.entrySet()) {
            Shelf shelf = entry.getValue();
            String productId = entry.getKey();

            for (Product batch : shelf.getAllBatches()) {
                if (batch.expiresSoon(currentDate) && batch.getDiscount() < SupermarketConfig.EXPIRING_DISCOUNT) {
                    batch.setDiscount(SupermarketConfig.EXPIRING_DISCOUNT);
                    discountCount++;
                    System.out.println("   🏷️ Скидка на товар с истекающим сроком: " +
                            productIdToName.get(productId));
                }
            }
        }
        return discountCount;
    }

    public void applyRandomDiscounts() {
        Random random = new Random();
        int discountCount = 0;

        for (Map.Entry<String, Shelf> entry : shelvesByProductId.entrySet()) {
            Shelf shelf = entry.getValue();

            for (Product batch : shelf.getAllBatches()) {
                if (random.nextDouble() < 0.15) {
                    double discount = SupermarketConfig.RANDOM_DISCOUNT_MIN +
                            random.nextDouble() * (SupermarketConfig.RANDOM_DISCOUNT_MAX - SupermarketConfig.RANDOM_DISCOUNT_MIN);
                    batch.setDiscount(discount);
                    discountCount++;
                }
            }
        }

        if (discountCount > 0) {
            System.out.println("   🏷️ Установлены случайные скидки на " + discountCount + " товаров");
        }
    }

    public void removeAllDiscounts() {
        for (Shelf shelf : shelvesByProductId.values()) {
            for (Product batch : shelf.getAllBatches()) {
                batch.setDiscount(0.0);
            }
        }
    }

    public List<String> getLowStockProductIds() {
        List<String> lowStockIds = new ArrayList<>();

        for (Map.Entry<String, Shelf> entry : shelvesByProductId.entrySet()) {
            String productId = entry.getKey();
            Shelf shelf = entry.getValue();

            // Проверяем заполненность полки
            if (shelf.needsRestocking()) {
                lowStockIds.add(productId);
            }
        }

        return lowStockIds;
    }

    public Map<String, Shelf> getAllShelves() {
        return new HashMap<>(shelvesByProductId);
    }

    public List<Product> getProductsList() {
        List<Product> allProducts = new ArrayList<>();
        for (Shelf shelf : shelvesByProductId.values()) {
            allProducts.addAll(shelf.getAllBatches());
        }
        return allProducts;
    }

    public int getTotalProducts() {
        return shelvesByProductId.size();
    }

    public int getTotalBatches() {
        int total = 0;
        for (Shelf shelf : shelvesByProductId.values()) {
            total += shelf.getAllBatches().size();
        }
        return total;
    }

    public Product getProduct(String productId) {
        Shelf shelf = shelvesByProductId.get(productId);
        if (shelf != null && !shelf.isEmpty()) {
            List<Product> batches = shelf.getAllBatches();
            if (!batches.isEmpty()) {
                return batches.get(0);
            }
        }
        return null;
    }

    public List<Product> getBatchesForProduct(String productId) {
        Shelf shelf = shelvesByProductId.get(productId);
        if (shelf != null) {
            return shelf.getAllBatches();
        }
        return new ArrayList<>();
    }

    public void removeBatch(String productId, String batchId) {
        Shelf shelf = shelvesByProductId.get(productId);
        if (shelf != null) {
            Product batch = shelf.getBatch(batchId);
            if (batch != null) {
                shelf.removeProduct(batch);
                if (shelf.isEmpty()) {
                    shelvesByProductId.remove(productId);
                    productIdToName.remove(productId);
                }
            }
        }
    }

    public void removeProduct(String productId) {
        shelvesByProductId.remove(productId);
        productIdToName.remove(productId);
    }

    public double getTotalAmount(String productId) {
        Shelf shelf = shelvesByProductId.get(productId);
        if (shelf != null) {
            return shelf.getCurrentAmount();
        }
        return 0;
    }

    private double getMinStockForProduct(String productId) {
        Product product = getProduct(productId);
        if (product instanceof CountableProduct) {
            return SupermarketConfig.SALES_HALL_MIN_COUNTABLE;
        } else if (product instanceof WeightableProduct) {
            return SupermarketConfig.SALES_HALL_MIN_WEIGHTABLE;
        }
        return 0;
    }

    public boolean isEmpty() {
        return shelvesByProductId.isEmpty();
    }

    // Вспомогательный метод для получения количества из продукта
    private double getAmountFromProduct(Product product) {
        if (product instanceof CountableProduct) {
            return ((CountableProduct) product).getQuantity();
        } else if (product instanceof WeightableProduct) {
            return ((WeightableProduct) product).getWeight();
        }
        return 0.0;
    }

    // Метод для отображения полок с заполнением менее 15%
    public void displayCriticalShelves() {
        // Собираем все полки с заполнением менее 15%
        List<Shelf> criticalShelves = new ArrayList<>();

        for (Map.Entry<String, Shelf> entry : shelvesByProductId.entrySet()) {
            Shelf shelf = entry.getValue();
            if (shelf.getFillPercentage() < 15.0) {
                criticalShelves.add(shelf);
            }
        }

        // Сортируем по проценту заполнения (от меньшего к большему)
        criticalShelves.sort((s1, s2) -> Double.compare(s1.getFillPercentage(), s2.getFillPercentage()));

        // Выводим результат
        if (!criticalShelves.isEmpty()) {
            System.out.println("⚠️ ПОЛКИ С МАЛЫМ ЗАПАСОМ (<15%):");
            for (Shelf shelf : criticalShelves) {
                printShelfStatus(shelf);
            }
        } else {
            System.out.println("✅ Все полки в норме");
        }
    }

    private void printShelfStatus(Shelf shelf) {
        String productName = shelf.getProductName();
        if (productName.length() > 20) {
            productName = productName.substring(0, 17) + "...";
        }

        double fill = shelf.getFillPercentage();
        String amountInfo = getShelfAmountInfo(shelf);

        System.out.println(String.format("   🔴 %-20s %5.0f%% (%s)",
                productName,
                fill,
                amountInfo));
    }

    private String getShelfAmountInfo(Shelf shelf) {
        double amount = shelf.getCurrentAmount();
        double max = shelf.getMaxCapacity();

        if (shelf.isEmpty()) {
            return "пусто";
        }

        Product sample = shelf.getAllBatches().get(0);
        if (sample instanceof WeightableProduct) {
            return String.format("%.1f/%.0f кг", amount, max);
        } else {
            return String.format("%.0f/%.0f шт", amount, max);
        }
    }

    public String getProductName(String productId) {
        return productIdToName.get(productId);
    }
}