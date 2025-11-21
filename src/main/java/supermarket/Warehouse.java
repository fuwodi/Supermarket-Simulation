package supermarket;

import supermarket.product.CountableProduct;
import supermarket.product.Product;
import supermarket.product.ProductType;
import supermarket.product.WeightableProduct;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Warehouse {
    private Map<String, Product> products;

    public Warehouse() {
        this.products = new HashMap<>();
    }

    public boolean addProduct(Product product, LocalDate currentDate) {
        if (product.isExpired(currentDate)) {
            System.out.println(" Товар " + product.getName() + " просрочен и не принят на склад");
            return false;
        }
        products.put(product.getId(), product);
        return true;
    }

    public Product getProduct(String productId) {
        return products.get(productId);
    }

    public void removeProduct(String productId) {
        products.remove(productId);
    }


    public Product transferProduct(String productId, int quantity, LocalDate currentDate) {
        Product product = products.get(productId);
        if (product instanceof CountableProduct) {
            CountableProduct countable = (CountableProduct) product;
            if (countable.getQuantity() >= quantity && !countable.isExpired(currentDate)) {
                CountableProduct forHall = new CountableProduct(
                        productId, countable.getBatchId(), countable.getName(),
                        countable.getType(), countable.getPrice(),
                        countable.getProductionDate(), countable.getShelfLifeDays(),
                        quantity
                );
                forHall.setDiscount(countable.getDiscount());

                countable.setQuantity(countable.getQuantity() - quantity);

                if (countable.getQuantity() == 0) {
                    products.remove(productId);
                }

                return forHall;
            }
        }
        return null;
    }

    public Product transferProduct(String productId, double weight, LocalDate currentDate) {
        Product product = products.get(productId);
        if (product instanceof WeightableProduct) {
            WeightableProduct weightable = (WeightableProduct) product;
            if (weightable.getWeight() >= weight && !weightable.isExpired(currentDate)) {
                WeightableProduct forHall = new WeightableProduct(
                        productId, weightable.getBatchId(), weightable.getName(),
                        weightable.getType(), weightable.getPrice(),
                        weightable.getProductionDate(), weightable.getShelfLifeDays(),
                        weight
                );
                forHall.setDiscount(weightable.getDiscount());

                weightable.setWeight(weightable.getWeight() - weight);

                if (weightable.getWeight() == 0) {
                    products.remove(productId);
                }

                return forHall;
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
                System.out.println(" Утилизирован просроченный товар со склада: " + entry.getValue().getName());
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }


    public List<ProductType> getProductsToReorder() {
        Map<ProductType, Integer> typeCounts = new HashMap<>();

        for (Product product : products.values()) {
            if (product instanceof CountableProduct) {
                int quantity = ((CountableProduct) product).getQuantity();
                typeCounts.merge(product.getType(), quantity, Integer::sum);
            }
        }

        return typeCounts.entrySet().stream()
                .filter(entry -> entry.getValue() <= SupermarketConfig.REORDER_THRESHOLD)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    public List<Product> autoRestockProducts(SalesHall salesHall, LocalDate currentDate) {
        List<Product> restockedProducts = new ArrayList<>();

        for (Product hallProduct : salesHall.getProductsList()) {
            boolean needsRestock = false;
            String productId = hallProduct.getId();

            if (hallProduct instanceof CountableProduct) {
                CountableProduct countable = (CountableProduct) hallProduct;
                if (countable.getQuantity() <= SupermarketConfig.LOW_STOCK_THRESHOLD) {
                    needsRestock = true;
                }
            } else if (hallProduct instanceof WeightableProduct) {
                WeightableProduct weightable = (WeightableProduct) hallProduct;
                if (weightable.getWeight() <= SupermarketConfig.WEIGHT_LOW_STOCK) {
                    needsRestock = true;
                }
            }

            if (needsRestock) {
                Product transferred = transferProductForRestock(productId, currentDate);
                if (transferred != null) {
                    restockedProducts.add(transferred);
                }
            }
        }

        return restockedProducts;
    }

    private Product transferProductForRestock(String productId, LocalDate currentDate) {
        Product product = products.get(productId);
        if (product instanceof CountableProduct) {
            CountableProduct countable = (CountableProduct) product;
            int transferAmount = Math.min(countable.getQuantity(), 10);
            if (transferAmount > 0) {
                return transferProduct(productId, transferAmount, currentDate);
            }
        } else if (product instanceof WeightableProduct) {
            WeightableProduct weightable = (WeightableProduct) product;
            double transferAmount = Math.min(weightable.getWeight(), 5.0);
            if (transferAmount > 0) {
                return transferProduct(productId, transferAmount, currentDate);
            }
        }
        return null;
    }

    public Map<String, Product> getAllProducts() {
        return new HashMap<>(products);
    }

    public int getTotalProducts() {
        return products.size();
    }

    public Collection<Product> getProductsCollection() {
        return products.values();
    }
}