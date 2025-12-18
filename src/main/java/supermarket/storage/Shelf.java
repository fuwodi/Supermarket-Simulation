package supermarket.storage;

import supermarket.product.*;
import java.util.*;

public class Shelf {
    private final String productId;
    private final double maxCapacity;
    private Map<String, Product> batchesByBatchId;
    private double currentAmount;
    private String productName;

    public Shelf(String productId, double maxCapacity) {
        this.productId = productId;
        this.maxCapacity = maxCapacity;
        this.batchesByBatchId = new HashMap<>();
        this.currentAmount = 0.0;
        this.productName = ProductCatalog.getProductNameById(productId);
        if (this.productName == null) {
            this.productName = productId;
        }
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getMaxCapacity() {
        return maxCapacity;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public double getAvailableSpace() {
        return Math.max(0, maxCapacity - currentAmount);
    }

    public double getFillPercentage() {
        return maxCapacity > 0 ? (currentAmount / maxCapacity) * 100 : 0;
    }

    public boolean needsRestocking() {
        return getFillPercentage() < 30.0;
    }

    public boolean isEmpty() {
        return batchesByBatchId.isEmpty();
    }

    public List<Product> getAllBatches() {
        // Возвращаем все партии
        return new ArrayList<>(batchesByBatchId.values());
    }

    public Product getBatch(String batchId) {
        return batchesByBatchId.get(batchId);
    }

    // Основной метод добавления товара на полку
    public double addProduct(Product product) {
        if (!productId.equals(product.getId())) {
            return 0.0;
        }

        String batchId = product.getBatchId();
        double amountToAdd = getAmountFromProduct(product);

        if (amountToAdd <= 0) {
            return 0.0;
        }

        double availableSpace = getAvailableSpace();

        if (availableSpace <= 0.001) {
            return 0.0;
        }

        double amountCanAdd = Math.min(amountToAdd, availableSpace);

        // Проверяем, есть ли уже такая партия на полке
        Product existingBatch = batchesByBatchId.get(batchId);

        if (existingBatch != null) {
            // Объединяем с существующей партией
            return mergeWithExistingBatch(existingBatch, product, amountCanAdd);
        } else {
            // Создаем новую партию на полке
            return createNewBatch(product, amountCanAdd);
        }
    }

    // Удалить товар с полки
    public boolean removeProduct(Product product) {
        String batchId = product.getBatchId();
        Product batchOnShelf = batchesByBatchId.get(batchId);

        if (batchOnShelf != null) {
            double amountToRemove = getAmountFromProduct(product);
            double batchAmount = getAmountFromProduct(batchOnShelf);

            if (batchAmount >= amountToRemove) {
                // Уменьшаем количество в партии
                if (batchOnShelf instanceof CountableProduct && product instanceof CountableProduct) {
                    CountableProduct shelfCountable = (CountableProduct) batchOnShelf;
                    CountableProduct removeCountable = (CountableProduct) product;
                    shelfCountable.setQuantity(shelfCountable.getQuantity() - removeCountable.getQuantity());
                } else if (batchOnShelf instanceof WeightableProduct && product instanceof WeightableProduct) {
                    WeightableProduct shelfWeightable = (WeightableProduct) batchOnShelf;
                    WeightableProduct removeWeightable = (WeightableProduct) product;
                    shelfWeightable.setWeight(shelfWeightable.getWeight() - removeWeightable.getWeight());
                }

                currentAmount -= amountToRemove;

                // Если после удаления товара на полке не осталось
                if (getAmountFromProduct(batchOnShelf) <= 0.001) {
                    batchesByBatchId.remove(batchId);
                }

                return true;
            }
        }

        return false;
    }

    // Приватные вспомогательные методы
    private double mergeWithExistingBatch(Product existingBatch, Product newProduct, double amountCanAdd) {
        if (existingBatch instanceof CountableProduct && newProduct instanceof CountableProduct) {
            CountableProduct existingCountable = (CountableProduct) existingBatch;
            CountableProduct newCountable = (CountableProduct) newProduct;

            int addQuantity = (int) Math.min(newCountable.getQuantity(), amountCanAdd);
            existingCountable.setQuantity(existingCountable.getQuantity() + addQuantity);
            currentAmount += addQuantity;
            return addQuantity;

        } else if (existingBatch instanceof WeightableProduct && newProduct instanceof WeightableProduct) {
            WeightableProduct existingWeightable = (WeightableProduct) existingBatch;
            WeightableProduct newWeightable = (WeightableProduct) newProduct;

            double addWeight = Math.min(newWeightable.getWeight(), amountCanAdd);
            existingWeightable.setWeight(existingWeightable.getWeight() + addWeight);
            currentAmount += addWeight;
            return addWeight;
        }

        return 0.0;
    }

    private double createNewBatch(Product product, double amountCanAdd) {
        String batchId = product.getBatchId();

        // Создаем новый продукт с нужным количеством
        Product batchForShelf = createPartialProduct(product, amountCanAdd);
        batchesByBatchId.put(batchId, batchForShelf);
        currentAmount += amountCanAdd;

        return amountCanAdd;
    }

    private Product createPartialProduct(Product original, double partialAmount) {
        // Используем ProductFactory для создания копии
        Product partialProduct = ProductFactory.createCopy(original, partialAmount);

        // Сохраняем скидку от оригинала
        if (partialProduct != null) {
            partialProduct.setDiscount(original.getDiscount());
        }

        return partialProduct;
    }

    private double getAmountFromProduct(Product product) {
        if (product instanceof CountableProduct) {
            return ((CountableProduct) product).getQuantity();
        } else if (product instanceof WeightableProduct) {
            return ((WeightableProduct) product).getWeight();
        }
        return 0.0;
    }
}