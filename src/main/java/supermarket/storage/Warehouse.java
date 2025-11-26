package supermarket.storage;

import supermarket.SupermarketConfig;
import supermarket.product.*;

import java.time.LocalDate;
import java.util.*;

public class Warehouse implements ProductStorage {
    private Map<String, List<Product>> productsByBatch;

    public Warehouse() {
        this.productsByBatch = new HashMap<>();
    }

    public boolean addProduct(Product product, LocalDate currentDate) {
        if (product.isExpired(currentDate)) {
            System.out.println(" Товар " + product.getName() + " просрочен и не принят на склад");
            return false;
        }

        String productId = product.getId();

        if (!productsByBatch.containsKey(productId)) {
            productsByBatch.put(productId, new ArrayList<>());
        }

        // На складе партии всегда уникальные, просто добавляем
        productsByBatch.get(productId).add(product);
        return true;
    }

    public Product getProduct(String productId) {
        List<Product> batches = productsByBatch.get(productId);
        return (batches != null && !batches.isEmpty()) ? batches.get(0) : null;
    }

    public List<Product> findProductsById(String productId) {
        List<Product> batches = productsByBatch.get(productId);
        if (batches != null) {
            return new ArrayList<>(batches);
        }
        return new ArrayList<>();
    }

    public void removeProduct(String productId) {
        productsByBatch.remove(productId);
    }

    public void removeBatch(String productId, String batchId) {
        List<Product> batches = productsByBatch.get(productId);
        if (batches != null) {
            batches.removeIf(product -> product.getBatchId().equals(batchId));
            if (batches.isEmpty()) {
                productsByBatch.remove(productId);
            }
        }
    }

    public List<Product> getBatchesForProduct(String productId) {
        return new ArrayList<>(productsByBatch.getOrDefault(productId, new ArrayList<>()));
    }

    public int removeExpiredProducts(LocalDate currentDate) {
        int removed = 0;
        Iterator<Map.Entry<String, List<Product>>> iterator = productsByBatch.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<Product>> entry = iterator.next();
            List<Product> batches = entry.getValue();

            // Удаляем просроченные партии
            Iterator<Product> batchIterator = batches.iterator();
            while (batchIterator.hasNext()) {
                Product batch = batchIterator.next();
                if (batch.isExpired(currentDate)) {
                    System.out.println(" Утилизирован просроченный товар со склада: " +
                            batch.getName() + " (партия: " + batch.getBatchId() + ")");
                    batchIterator.remove();
                    removed++;
                }
            }

            // Если все партии товара удалены, убираем запись
            if (batches.isEmpty()) {
                iterator.remove();
            }
        }
        return removed;
    }

    public Map<String, List<Product>> getAllProducts() {
        Map<String, List<Product>> copy = new HashMap<>();
        for (Map.Entry<String, List<Product>> entry : productsByBatch.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    public int getTotalProducts() {
        return productsByBatch.size();
    }

    public int getTotalBatches() {
        int total = 0;
        for (List<Product> batches : productsByBatch.values()) {
            total += batches.size();
        }
        return total;
    }

    public Collection<List<Product>> getProductsCollection() {
        return productsByBatch.values();
    }

    public double getTotalAmount(String productId) {
        List<Product> batches = productsByBatch.get(productId);
        if (batches == null) return 0;

        double total = 0;
        for (Product batch : batches) {
            if (batch instanceof CountableProduct) {
                total += ((CountableProduct) batch).getQuantity();
            } else if (batch instanceof WeightableProduct) {
                total += ((WeightableProduct) batch).getWeight();
            }
        }
        return total;
    }

    /**
     * Проверяет, нуждается ли склад в пополнении (мало видов товаров)
     */
    public boolean needsRestocking() {
        return getTotalProducts() < 8; // Если меньше 8 видов товаров
    }

    /**
     * Проверяет, пустой ли склад
     */
    public boolean isEmpty() {
        return productsByBatch.isEmpty();
    }

    /**
     * Возвращает список товаров, которых мало на складе
     */
    public List<String> getLowStockProductIds() {
        List<String> lowStockIds = new ArrayList<>();
        for (String productId : productsByBatch.keySet()) {
            double totalAmount = getTotalAmount(productId);
            double minStock = getMinStockForProduct(productId);

            if (totalAmount < minStock) {
                lowStockIds.add(productId);
            }
        }
        return lowStockIds;
    }

    private double getMinStockForProduct(String productId) {
        Product product = getProduct(productId);
        if (product instanceof CountableProduct) {
            return SupermarketConfig.WAREHOUSE_MIN_COUNTABLE;
        } else if (product instanceof WeightableProduct) {
            return SupermarketConfig.WAREHOUSE_MIN_WEIGHTABLE;
        }
        return 1.0;
    }
}