package supermarket.customer;

import supermarket.product.ProductCatalog;
import java.util.*;

public class CustomerPreferences {
    public enum PreferenceType {
        HEALTHY("Здоровое питание 🥗"),
        FAMILY("Семейные покупки 👨‍👩‍👧‍👦"),
        BUDGET("Экономные покупки 💰"),
        GOURMET("Гурманы 🍷"),
        STUDENT("Студент 🎓"),
        VEGETARIAN("Вегетарианцы 🌿");

        private final String description;

        PreferenceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final PreferenceType preferenceType;
    private final Random random;
    private final Set<String> favoriteProductIds;

    public CustomerPreferences(PreferenceType preferenceType) {
        this.preferenceType = preferenceType;
        this.random = new Random();
        this.favoriteProductIds = new HashSet<>();

        initializeFavoriteProducts();
    }

    private void initializeFavoriteProducts() {
        switch (preferenceType) {
            case HEALTHY:
                // Здоровое питание: йогурты, творог, фрукты, курица
                addFavoriteProducts("YOGURT", "COTTAGE_CHEESE", "APPLE", "BANANA",
                        "CARROT", "CHICKEN", "TURKEY", "KEFIR", "RYAZHENKA");
                break;

            case FAMILY:
                // Семейные покупки: молоко, хлеб, курица, картошка, макароны
                addFavoriteProducts("MILK", "WHITE_BREAD", "CHICKEN", "POTATO",
                        "PASTA", "RICE", "EGGS", "SOUR_CREAM", "CUCUMBER");
                break;

            case BUDGET:
                // Экономные: дешевые товары (НО НЕ ПОДСОЛНЕЧНОЕ МАСЛО - ОНО ДОРОГОЕ!)
                addFavoriteProducts("PASTA", "RICE", "POTATO", "ONION",
                        "FLOUR", "SALT", "WHITE_BREAD", "BUCKWHEAT",
                        "MILK", "CARROT");
                break;

            case GOURMET:
                // Гурманы: дорогие и вкусные товары
                addFavoriteProducts("CHEESE", "BEEF", "RED_WINE", "WHISKEY",
                        "BACON", "CREAM", "BAGUETTE", "CHAMPAGNE",
                        "TOMATO", "COGNAC", "LIQUEUR");
                break;

            case STUDENT:
                addFavoriteProducts("PASTA", "INSTANT_NOODLES", "BEER", "SAUSAGES",
                        "WHITE_BREAD", "EGGS", "MILK", "VODKA",
                        "CHICKEN", "POTATO", "RICE");
                break;

            case VEGETARIAN:
                addFavoriteProducts("APPLE", "BANANA", "ORANGE", "TOMATO",
                        "CUCUMBER", "CARROT", "POTATO", "ONION",
                        "PASTA", "RICE", "BUCKWHEAT", "FLOUR");
                break;
        }
    }

    private void addFavoriteProducts(String... productIds) {
        for (String productId : productIds) {
            if (ProductCatalog.findProductById(productId) != null) {
                favoriteProductIds.add(productId);
            }
        }
    }

    public boolean isFavoriteProduct(String productId) {
        return favoriteProductIds.contains(productId);
    }

    public Set<String> getFavoriteProductIds() {
        return new HashSet<>(favoriteProductIds);
    }

    public PreferenceType getPreferenceType() {
        return preferenceType;
    }

    public String getDescription() {
        return preferenceType.getDescription();
    }

    @Override
    public String toString() {
        return preferenceType.getDescription();
    }
}