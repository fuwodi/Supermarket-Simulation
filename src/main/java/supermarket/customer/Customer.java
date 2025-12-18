package supermarket.customer;

import supermarket.product.Product;
import supermarket.storage.SalesHall;
import java.util.*;

public class Customer {
    private String id;
    private String name;
    private double budget;
    private CustomerPreferences preferences;
    private DiscountCard discountCard;
    private Random random;
    private ShoppingCart shoppingCart;
    private double baseBudget;

    // Конструктор для существующего кода
    public Customer(String id, String name, double baseBudget, CustomerPreferences preferences) {
        this.id = id;
        this.name = name;
        this.baseBudget = baseBudget;
        this.budget = baseBudget * (0.7 + Math.random() * 0.6); // 70-130% от базового
        this.preferences = preferences;
        this.random = new Random();
        this.shoppingCart = new ShoppingCart();
        this.discountCard = null;
    }

    // Конструктор для PredefinedCustomers (с картой)
    public Customer(String id, String name, CustomerPreferences.PreferenceType preferenceType, DiscountCard discountCard) {
        this.id = id;
        this.name = name;
        this.preferences = new CustomerPreferences(preferenceType);
        this.random = new Random();
        this.shoppingCart = new ShoppingCart();
        this.baseBudget = 1200 + random.nextDouble() * 600; // примерный бюджет
        this.budget = this.baseBudget * (0.7 + random.nextDouble() * 0.6);
        this.discountCard = discountCard;
    }

    public boolean hasDiscountCard() {
        return discountCard != null;
    }

    public DiscountCard getDiscountCard() {
        return discountCard;
    }

    public String getName() { return name; }
    public double getBudget() { return budget; }
    public double getBaseBudget() { return baseBudget; }
    public CustomerPreferences getPreferences() { return preferences; }
    public String getId() { return id; }
    public ShoppingCart getShoppingCart() { return shoppingCart; }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    // Восстановление бюджета
    public void restoreBudget() {
        this.budget = 500 + random.nextDouble() * 500;
    }

    // Метод для выбора товаров
    public List<Product> selectProducts(SalesHall salesHall) {
        List<Product> allProducts = salesHall.getProductsList();
        List<Product> selected = new ArrayList<>();
        List<Product> preferred = new ArrayList<>();

        // Ищем любимые товары
        for (Product product : allProducts) {
            if (preferences.isFavoriteProduct(product.getId()) && product.getPrice() <= budget) {
                preferred.add(product);
            }
        }

        // Если есть любимые, берем до 3
        if (!preferred.isEmpty()) {
            Collections.shuffle(preferred);
            int maxProducts = Math.min(3, preferred.size());
            for (int i = 0; i < maxProducts; i++) {
                Product product = preferred.get(i);
                if (product.getPrice() <= budget) {
                    selected.add(product);
                    // budget -= product.getPrice(); // Списание будет в makePurchase
                }
            }
        } else {
            // Если нет любимых, берем случайные
            List<Product> affordable = new ArrayList<>();
            for (Product product : allProducts) {
                if (product.getPrice() <= budget * 0.3) { // Не дороже 30% бюджета
                    affordable.add(product);
                }
            }

            if (!affordable.isEmpty()) {
                Collections.shuffle(affordable);
                int maxProducts = Math.min(2, affordable.size());
                for (int i = 0; i < maxProducts; i++) {
                    selected.add(affordable.get(i));
                }
            }
        }

        return selected;
    }

    // Метод покупки с учетом скидочной карты
    public double makePurchase(SalesHall salesHall) {
        System.out.println("\n👤 " + name + " (" + preferences.getDescription() +
                ") | Бюджет: " + String.format("%.2f", budget) + " руб.");

        if (hasDiscountCard()) {
            System.out.println("   🎫 " + discountCard.toString());
        }

        List<Product> selectedProducts = selectProducts(salesHall);

        if (selectedProducts.isEmpty()) {
            System.out.println("   ❌ Не нашел подходящих товаров");
            return 0;
        }

        double total = 0;

        // Показываем и считаем товары
        for (Product product : selectedProducts) {
            double price = product.getPrice();

            if (hasDiscountCard() && product.getDiscount() > 0) {
                double discountedPrice = product.getFinalPrice();
                System.out.println("   🛒 " + product.getName() +
                        " - " + String.format("%.2f", price) + " руб." +
                        " → " + String.format("%.2f", discountedPrice) + " руб. 🎫" +
                        " (-" + (int)(product.getDiscount() * 100) + "%)");
                total += discountedPrice;
            } else {
                System.out.println("   🛒 " + product.getName() +
                        " - " + String.format("%.2f", price) + " руб.");
                total += price;

                if (product.getDiscount() > 0 && !hasDiscountCard()) {
                    System.out.println("      ⚠️  Скидка " + (int)(product.getDiscount() * 100) +
                            "% только для владельцев карт!");
                }
            }
        }

        // Списание баллов
        double pointsDiscount = 0;
        if (hasDiscountCard()) {
            pointsDiscount = discountCard.usePoints(total);
            if (pointsDiscount > 0) {
                System.out.println("   💳 Списано баллов: " + (int)pointsDiscount);
                total -= pointsDiscount;
                if (total < 0) total = 0;
            }
        }

        // Накопление баллов
        if (hasDiscountCard()) {
            discountCard.addPoints(total);
            System.out.println("   💰 Итог: " + String.format("%.2f", total) +
                    " руб. | Баланс: " + discountCard.getPoints() + " баллов");
        } else {
            System.out.println("   💰 Итог: " + String.format("%.2f", total) + " руб.");
        }

        // Удаляем товары из зала
        for (Product product : selectedProducts) {
            salesHall.removeBatch(product.getId(), product.getBatchId());
        }

        budget -= total;
        return total;
    }

    @Override
    public String toString() {
        return name + " (" + preferences.getDescription() + ") - " +
                String.format("%.2f", budget) + " руб." +
                (hasDiscountCard() ? " 🎫" : "");
    }
}