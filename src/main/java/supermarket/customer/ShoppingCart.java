package supermarket.customer;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private final Map<String, CartItem> items;

    public ShoppingCart() {
        this.items = new HashMap<>();
    }

    public void addItem(String productId, int quantity, double weight, String batchId) {
        items.put(productId, new CartItem(productId, quantity, weight, batchId));
    }

    public void removeItem(String productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public Map<String, CartItem> getItems() {
        return new HashMap<>(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public static class CartItem {
        private final String productId;
        private final int quantity;
        private final double weight;
        private final String batchId;

        public CartItem(String productId, int quantity, double weight, String batchId) {
            this.productId = productId;
            this.quantity = quantity;
            this.weight = weight;
            this.batchId = batchId;
        }

        public String getProductId() { return productId; }
        public int getQuantity() { return quantity; }
        public double getWeight() { return weight; }
        public String getBatchId() { return batchId; }
    }
}