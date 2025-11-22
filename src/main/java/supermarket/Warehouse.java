package supermarket;

import supermarket.product.*;

import java.time.LocalDate;
import java.util.*;

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

    public List<Product> findProductsByName(String productName) {
        List<Product> result = new ArrayList<>();
        for (Product product : products.values()) {
            if (product.getName().equalsIgnoreCase(productName)) {
                result.add(product);
            }
        }
        return result;
    }

    public List<Product> findProductsByNameAndType(String name, ProductType type) {
        List<Product> result = new ArrayList<>();
        for (Product product : products.values()) {
            if (product.getName().equalsIgnoreCase(name) && product.getType() == type) {
                result.add(product);
            }
        }
        return result;
    }

    public void removeProduct(String productId) {
        products.remove(productId);
    }

    public Product transferProduct(String productId, int requestedQuantity, LocalDate currentDate) {
        Product product = products.get(productId);
        if (product instanceof CountableProduct) {
            CountableProduct countable = (CountableProduct) product;

            if (!countable.isExpired(currentDate)) {
                int availableQuantity = countable.getQuantity();
                int transferQuantity = Math.min(requestedQuantity, availableQuantity);

                if (transferQuantity > 0) {
                    CountableProduct forHall = new CountableProduct(
                            productId, countable.getBatchId(), countable.getName(),
                            countable.getType(), countable.getPrice(),
                            countable.getProductionDate(), countable.getShelfLifeDays(),
                            transferQuantity
                    );

                    countable.setQuantity(availableQuantity - transferQuantity);

                    if (countable.getQuantity() == 0) {
                        products.remove(productId);
                    }

                   //подумать
                    if (transferQuantity < requestedQuantity) {
                        int missingQuantity = requestedQuantity - transferQuantity;
                        System.out.println("⚠️ Внимание: запрошено " + requestedQuantity +
                                " шт. '" + countable.getName() + "', но доступно только " +
                                transferQuantity + " шт. Не хватает: " + missingQuantity + " шт. Требуется заказ!");
                    }

                    return forHall;
                }
            }
        }
        return null;
    }

    public Product transferProduct(String productId, double requestedWeight, LocalDate currentDate) {
        Product product = products.get(productId);
        if (product instanceof WeightableProduct) {
            WeightableProduct weightable = (WeightableProduct) product;

            if (!weightable.isExpired(currentDate)) {
                double availableWeight = weightable.getWeight();
                double transferWeight = Math.min(requestedWeight, availableWeight);

                if (transferWeight > 0) {
                    WeightableProduct forHall = new WeightableProduct(
                            productId, weightable.getBatchId(), weightable.getName(),
                            weightable.getType(), weightable.getPrice(),
                            weightable.getProductionDate(), weightable.getShelfLifeDays(),
                            transferWeight
                    );

                    weightable.setWeight(availableWeight - transferWeight);

                    if (weightable.getWeight() == 0) {
                        products.remove(productId);
                    }


                    if (transferWeight < requestedWeight) {
                        double missingWeight = requestedWeight - transferWeight;
                        System.out.println("⚠️ Внимание: запрошено " + requestedWeight +
                                " кг '" + weightable.getName() + "', но доступно только " +
                                transferWeight + " кг. Не хватает: " + missingWeight + " кг. Требуется заказ!");
                    }

                    return forHall;
                }
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