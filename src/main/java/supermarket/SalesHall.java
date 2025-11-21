package supermarket;

import supermarket.product.CountableProduct;
import supermarket.product.Product;
import supermarket.product.WeightableProduct;

import java.time.LocalDate;
import java.util.*;

public class SalesHall {
    private Map<String, Product> products;

    public SalesHall() {
        this.products = new HashMap<>();
    }


    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    public void updateProduct(Product product) {
        Product existing = products.get(product.getId());
        if (existing != null) {
            if (existing instanceof CountableProduct && product instanceof CountableProduct) {
                CountableProduct existingCountable = (CountableProduct) existing;
                CountableProduct newCountable = (CountableProduct) product;
                existingCountable.setQuantity(existingCountable.getQuantity() + newCountable.getQuantity());
            } else if (existing instanceof WeightableProduct && product instanceof WeightableProduct) {
                WeightableProduct existingWeightable = (WeightableProduct) existing;
                WeightableProduct newWeightable = (WeightableProduct) product;
                existingWeightable.setWeight(existingWeightable.getWeight() + newWeightable.getWeight());
            }
        } else {
            products.put(product.getId(), product);
        }
    }

    public Product purchaseProduct(String productId, int quantity) {
        Product product = products.get(productId);
        if (product instanceof CountableProduct) {
            CountableProduct countable = (CountableProduct) product;
            if (countable.getQuantity() >= quantity) {
                countable.decreaseQuantity(quantity);

                CountableProduct purchased = new CountableProduct(
                        productId, countable.getBatchId(), countable.getName(),
                        countable.getType(), countable.getPrice(),
                        countable.getProductionDate(), countable.getShelfLifeDays(),
                        quantity
                );
                purchased.setDiscount(countable.getDiscount());

                if (countable.getQuantity() == 0) {
                    products.remove(productId);
                }

                return purchased;
            }
        }
        return null;
    }

    public Product purchaseProduct(String productId, double weight) {
        Product product = products.get(productId);
        if (product instanceof WeightableProduct) {
            WeightableProduct weightable = (WeightableProduct) product;
            if (weightable.getWeight() >= weight) {
                weightable.decreaseWeight(weight);

                WeightableProduct purchased = new WeightableProduct(
                        productId, weightable.getBatchId(), weightable.getName(),
                        weightable.getType(), weightable.getPrice(),
                        weightable.getProductionDate(), weightable.getShelfLifeDays(),
                        weight
                );
                purchased.setDiscount(weightable.getDiscount());

                if (weightable.getWeight() == 0) {
                    products.remove(productId);
                }

                return purchased;
            }
        }
        return null;
    }

    public int removeExpiredProducts(LocalDate currentDate) {
        int removed = 0;
        Iterator<Map.Entry<String, Product>> iterator = products.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Product> entry = iterator.next();
            if (entry.getValue().isExpired(currentDate)) {
                System.out.println("🗑️ Утилизирован просроченный товар: " + entry.getValue().getName());
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    public void applyExpiringDiscounts(LocalDate currentDate) {
        int discountCount = 0;
        for (Product product : products.values()) {  // Исправлено: используем values()
            if (product.expiresSoon(currentDate) && product.getDiscount() < SupermarketConfig.EXPIRING_DISCOUNT) {
                product.setDiscount(SupermarketConfig.EXPIRING_DISCOUNT);
                discountCount++;
            }
        }
        if (discountCount > 0) {
            System.out.println("🏷️ Установлены скидки на " + discountCount + " товаров с истекающим сроком");
        }
    }

    public void applyRandomDiscounts() {
        Random random = new Random();
        int discountCount = 0;
        for (Product product : products.values()) {  // Исправлено: используем values()
            if (random.nextDouble() < 0.15) {
                double discount = SupermarketConfig.RANDOM_DISCOUNT_MIN +
                        random.nextDouble() * (SupermarketConfig.RANDOM_DISCOUNT_MAX - SupermarketConfig.RANDOM_DISCOUNT_MIN);
                product.setDiscount(discount);
                discountCount++;
            }
        }
        if (discountCount > 0) {
            System.out.println(" Установлены случайные скидки на " + discountCount + " товаров");
        }
    }

    public Map<String, Product> getAllProducts() {
        return new HashMap<>(products);
    }

    public List<Product> getProductsList() {
        return new ArrayList<>(products.values());
    } // Исправленный метод

    public int getTotalProducts() {
        return products.size();
    }

    public Collection<Product> getProductsCollection() {
        return products.values();
    }
}