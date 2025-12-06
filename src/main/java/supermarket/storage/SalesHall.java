package supermarket.storage;

import supermarket.SupermarketConfig;
import supermarket.product.CountableProduct;
import supermarket.product.Product;
import supermarket.product.WeightableProduct;

import java.time.LocalDate;
import java.util.*;

public class SalesHall implements ProductStorage {
    private Map<String, List<Product>> productsByBatch;

    public SalesHall() {
        this.productsByBatch = new HashMap<>();
    }

    public boolean addProduct(Product product, LocalDate currentDate) {
        if (product.isExpired(currentDate)) {
            System.out.println(" Товар " + product.getName() + " просрочен и не принят в торговый зал");
            return false;
        }

        String productId = product.getId();
        String batchId = product.getBatchId();

        if (!productsByBatch.containsKey(productId)) {
            productsByBatch.put(productId, new ArrayList<>());
        }

        List<Product> batches = productsByBatch.get(productId);

        for (Product existingBatch : batches) {
            if (existingBatch.getBatchId().equals(batchId)) {
                if (existingBatch instanceof CountableProduct && product instanceof CountableProduct) {
                    CountableProduct existingCountable = (CountableProduct) existingBatch;
                    CountableProduct newCountable = (CountableProduct) product;
                    existingCountable.setQuantity(existingCountable.getQuantity() + newCountable.getQuantity());
                    return true;
                } else if (existingBatch instanceof WeightableProduct && product instanceof WeightableProduct) {
                    WeightableProduct existingWeightable = (WeightableProduct) existingBatch;
                    WeightableProduct newWeightable = (WeightableProduct) product;
                    existingWeightable.setWeight(existingWeightable.getWeight() + newWeightable.getWeight());
                    return true;
                }
            }
        }

        batches.add(product);
        return true;
    }

    public List<Product> findProductsById(String productId) {
        List<Product> batches = productsByBatch.get(productId);
        if (batches != null) {
            return new ArrayList<>(batches);
        }
        return new ArrayList<>();
    }

    public int removeExpiredProducts(LocalDate currentDate) {
        int removed = 0;
        Iterator<Map.Entry<String, List<Product>>> iterator = productsByBatch.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, List<Product>> entry = iterator.next();
            List<Product> batches = entry.getValue();

            Iterator<Product> batchIterator = batches.iterator();
            while (batchIterator.hasNext()) {
                Product batch = batchIterator.next();
                if (batch.isExpired(currentDate)) {
                    System.out.println("🗑️ Утилизирован просроченный товар: " +
                            batch.getName() + " (партия: " + batch.getBatchId() + ")");
                    batchIterator.remove();
                    removed++;
                }
            }

            if (batches.isEmpty()) {
                iterator.remove();
            }
        }
        return removed;
    }

    public void applyDiscountToProductById(String productId, double discount) {
        List<Product> batches = productsByBatch.get(productId);
        if (batches != null) {
            for (Product batch : batches) {
                batch.setDiscount(discount);
            }
        }
    }

    public void applyDiscountToBatch(String productId, String batchId, double discount) {
        List<Product> batches = productsByBatch.get(productId);
        if (batches != null) {
            for (Product batch : batches) {
                if (batch.getBatchId().equals(batchId)) {
                    batch.setDiscount(discount);
                    break;
                }
            }
        }
    }

    public int applyExpiringDiscounts(LocalDate currentDate) {
        int discountCount = 0;
        for (List<Product> batches : productsByBatch.values()) {
            for (Product batch : batches) {
                if (batch.expiresSoon(currentDate) && batch.getDiscount() < SupermarketConfig.EXPIRING_DISCOUNT) {
                    batch.setDiscount(SupermarketConfig.EXPIRING_DISCOUNT);
                    discountCount++;
                    System.out.println("   🏷️ Скидка на товар с истекающим сроком: " +
                            batch.getName() + " (партия: " + batch.getBatchId() + ")");
                }
            }
        }
        return discountCount;
    }

    public void applyRandomDiscounts() {
        Random random = new Random();
        int discountCount = 0;
        for (List<Product> batches : productsByBatch.values()) {
            for (Product batch : batches) {
                if (random.nextDouble() < 0.15) {
                    double discount = SupermarketConfig.RANDOM_DISCOUNT_MIN +
                            random.nextDouble() * (SupermarketConfig.RANDOM_DISCOUNT_MAX - SupermarketConfig.RANDOM_DISCOUNT_MIN);
                    batch.setDiscount(discount);
                    discountCount++;
                }
            }
        }
        if (discountCount > 0) {
            System.out.println("🏷️ Установлены случайные скидки на " + discountCount + " партий");
        }
    }

    public void removeAllDiscounts() {
        for (List<Product> batches : productsByBatch.values()) {
            for (Product batch : batches) {
                batch.setDiscount(0.0);
            }
        }
    }

    public List<String> getLowStockProductIds() {
        List<String> lowStockIds = new ArrayList<>();
        for (String productId : productsByBatch.keySet()) {
            double totalAmount = getTotalAmount(productId);
            double minStock = getMinStockForProduct(productId);

            if (totalAmount < minStock && totalAmount > 0) {
                lowStockIds.add(productId);
            }
        }
        return lowStockIds;
    }


    public Map<String, List<Product>> getAllProducts() {
        Map<String, List<Product>> copy = new HashMap<>();
        for (Map.Entry<String, List<Product>> entry : productsByBatch.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    public List<Product> getProductsList() {
        List<Product> allProducts = new ArrayList<>();
        for (List<Product> batches : productsByBatch.values()) {
            allProducts.addAll(batches);
        }
        return allProducts;
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

    public Product getProduct(String productId) {
        List<Product> batches = productsByBatch.get(productId);
        return (batches != null && !batches.isEmpty()) ? batches.get(0) : null;
    }

    public List<Product> getBatchesForProduct(String productId) {
        return new ArrayList<>(productsByBatch.getOrDefault(productId, new ArrayList<>()));
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

    public void removeProduct(String productId) {
        productsByBatch.remove(productId);
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
        return productsByBatch.isEmpty();
    }

}