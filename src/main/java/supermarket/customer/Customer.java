package supermarket.customer;

import supermarket.product.CountableProduct;
import supermarket.product.Product;
import supermarket.product.WeightableProduct;
import supermarket.storage.SalesHall;

import java.util.*;

public class Customer {
    private final String id;
    private final String name;
    private double budget;
    private final Random random;

    public Customer(String id, String name, double budget) {
        this.id = id;
        this.name = name;
        this.budget = budget;
        this.random = new Random();
    }

    public double makePurchase(SalesHall salesHall) {
        ShoppingCart cart = createShoppingCart(salesHall);
        if (cart.getItems().isEmpty()) {
            return 0.0;
        }

        double totalCost = calculateTotal(cart, salesHall);
        if (totalCost <= budget && totalCost > 0) {
            if (processPurchase(cart, salesHall)) {
                budget -= totalCost;
                System.out.println("   🛒 " + name + " купил(а) товаров на " + String.format("%.2f", totalCost) + " руб.");
                return totalCost;
            }
        }
        return 0.0;
    }

    private ShoppingCart createShoppingCart(SalesHall salesHall) {
        ShoppingCart cart = new ShoppingCart();
        List<Product> availableProducts = salesHall.getProductsList();

        if (availableProducts.isEmpty()) {
            return cart;
        }

        int itemsToBuy = 1 + random.nextInt(3);
        Collections.shuffle(availableProducts);

        for (int i = 0; i < Math.min(itemsToBuy, availableProducts.size()); i++) {
            Product product = availableProducts.get(i);

            if (product instanceof CountableProduct) {
                CountableProduct countable = (CountableProduct) product;
                int maxQuantity = countable.getQuantity();
                if (maxQuantity > 0) {
                    int quantity = Math.min(1 + random.nextInt(2), maxQuantity);
                    double itemCost = product.getFinalPrice() * quantity;
                    if (itemCost <= budget * 0.5) {
                        cart.addItem(product.getId(), quantity, 0, product.getBatchId());
                    }
                }
            } else if (product instanceof WeightableProduct) {
                WeightableProduct weightable = (WeightableProduct) product;
                double maxWeight = weightable.getWeight();
                if (maxWeight > 0.05) {
                    double weight = Math.min(0.1 + random.nextDouble() * 0.9, maxWeight);
                    double itemCost = product.getFinalPrice() * weight;
                    if (itemCost <= budget * 0.5) {
                        cart.addItem(product.getId(), 0, weight, product.getBatchId());
                    }
                }
            }
        }

        return cart;
    }

    private double calculateTotal(ShoppingCart cart, SalesHall salesHall) {
        double total = 0.0;

        for (ShoppingCart.CartItem item : cart.getItems().values()) {
            List<Product> products = salesHall.findProductsById(item.getProductId());
            if (!products.isEmpty()) {
                Product product = products.get(0);
                if (item.getQuantity() > 0) {
                    total += product.getFinalPrice() * item.getQuantity();
                } else if (item.getWeight() > 0) {
                    total += product.getFinalPrice() * item.getWeight();
                }
            }
        }

        return total;
    }

    private boolean processPurchase(ShoppingCart cart, SalesHall salesHall) {
        for (ShoppingCart.CartItem item : cart.getItems().values()) {
            if (!isItemAvailable(item, salesHall)) {
                return false;
            }
        }

        for (ShoppingCart.CartItem item : cart.getItems().values()) {
            removePurchasedItem(item, salesHall);
        }

        return true;
    }

    private boolean isItemAvailable(ShoppingCart.CartItem item, SalesHall salesHall) {
        List<Product> batches = salesHall.findProductsById(item.getProductId());
        for (Product batch : batches) {
            if (batch.getBatchId().equals(item.getBatchId())) {
                if (item.getQuantity() > 0 && batch instanceof CountableProduct) {
                    return ((CountableProduct) batch).getQuantity() >= item.getQuantity();
                } else if (item.getWeight() > 0 && batch instanceof WeightableProduct) {
                    return ((WeightableProduct) batch).getWeight() >= item.getWeight();
                }
            }
        }
        return false;
    }

    private void removePurchasedItem(ShoppingCart.CartItem item, SalesHall salesHall) {
        List<Product> batches = salesHall.findProductsById(item.getProductId());
        for (Product batch : batches) {
            if (batch.getBatchId().equals(item.getBatchId())) {
                if (item.getQuantity() > 0 && batch instanceof CountableProduct) {
                    CountableProduct countable = (CountableProduct) batch;
                    countable.setQuantity(countable.getQuantity() - item.getQuantity());
                    System.out.println("     - " + batch.getName() + ": " + item.getQuantity() + " шт.");
                } else if (item.getWeight() > 0 && batch instanceof WeightableProduct) {
                    WeightableProduct weightable = (WeightableProduct) batch;
                    weightable.setWeight(weightable.getWeight() - item.getWeight());
                    System.out.println("     - " + batch.getName() + ": " + String.format("%.2f", item.getWeight()) + " кг");
                }
                break;
            }
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getBudget() { return budget; }
}