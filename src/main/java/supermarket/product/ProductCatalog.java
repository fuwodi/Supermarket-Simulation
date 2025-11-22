package supermarket.product;

import java.util.*;

public class ProductCatalog {
    private static final Map<String, ProductInfo> productsByName = new HashMap<>();
    private static final Map<ProductType, List<ProductInfo>> productsByType = new HashMap<>();

    static {
        addProducts(ProductType.DAIRY, Arrays.asList(
                new ProductInfo("Молоко", 80.0),
                new ProductInfo("Йогурт", 60.0),
                new ProductInfo("Сметана", 90.0),
                new ProductInfo("Творог", 120.0),
                new ProductInfo("Сыр", 300.0),
                new ProductInfo("Кефир", 70.0),
                new ProductInfo("Ряженка", 65.0),
                new ProductInfo("Сливки", 110.0)
        ));

        addProducts(ProductType.BAKERY, Arrays.asList(
                new ProductInfo("Хлеб белый", 50.0),
                new ProductInfo("Хлеб черный", 55.0),
                new ProductInfo("Батон", 45.0),
                new ProductInfo("Булочка сдобная", 35.0),
                new ProductInfo("Круассан", 60.0),
                new ProductInfo("Пирожок", 40.0),
                new ProductInfo("Багет", 70.0),
                new ProductInfo("Сухари", 80.0)
        ));

        addProducts(ProductType.MEAT, Arrays.asList(
                new ProductInfo("Говядина", 400.0),
                new ProductInfo("Свинина", 350.0),
                new ProductInfo("Курица", 250.0),
                new ProductInfo("Индейка", 300.0),
                new ProductInfo("Колбаса", 280.0),
                new ProductInfo("Сосиски", 200.0),
                new ProductInfo("Фарш", 320.0),
                new ProductInfo("Бекон", 450.0)
        ));

        addProducts(ProductType.VEGETABLES, Arrays.asList(
                new ProductInfo("Картофель", 40.0),
                new ProductInfo("Морковь", 50.0),
                new ProductInfo("Помидоры", 150.0),
                new ProductInfo("Огурцы", 120.0),
                new ProductInfo("Яблоки", 80.0),
                new ProductInfo("Бананы", 90.0),
                new ProductInfo("Апельсины", 110.0),
                new ProductInfo("Лук", 30.0)
        ));

        addProducts(ProductType.GROCERIES, Arrays.asList(
                new ProductInfo("Макароны", 60.0),
                new ProductInfo("Рис", 80.0),
                new ProductInfo("Гречка", 70.0),
                new ProductInfo("Мука", 50.0),
                new ProductInfo("Сахар", 45.0),
                new ProductInfo("Соль", 20.0),
                new ProductInfo("Масло подсолнечное", 100.0),
                new ProductInfo("Чай", 120.0)
        ));

        addProducts(ProductType.CHEMICALS, Arrays.asList(
                new ProductInfo("Стиральный порошок", 200.0),
                new ProductInfo("Мыло", 40.0),
                new ProductInfo("Шампунь", 180.0),
                new ProductInfo("Зубная паста", 90.0),
                new ProductInfo("Средство для мытья посуды", 80.0),
                new ProductInfo("Освежитель воздуха", 120.0),
                new ProductInfo("Пятновыводитель", 150.0),
                new ProductInfo("Кондиционер для белья", 160.0)
        ));

        addProducts(ProductType.ALCOHOL, Arrays.asList(
                new ProductInfo("Пиво", 120.0),
                new ProductInfo("Вино красное", 400.0),
                new ProductInfo("Вино белое", 380.0),
                new ProductInfo("Водка", 500.0),
                new ProductInfo("Виски", 800.0),
                new ProductInfo("Коньяк", 700.0),
                new ProductInfo("Шампанское", 450.0),
                new ProductInfo("Ликер", 350.0)
        ));
    }

    private static void addProducts(ProductType type, List<ProductInfo> products) {
        productsByType.put(type, products);
        for (ProductInfo product : products) {
            productsByName.put(product.getName().toLowerCase(), product);
        }
    }

    public static ProductInfo findProductByName(String name) {
        return productsByName.get(name.toLowerCase());
    }

    public static ProductInfo getRandomProductInfo(ProductType type) {
        List<ProductInfo> products = productsByType.get(type);
        if (products == null || products.isEmpty()) {
            return new ProductInfo("Товар", 100.0);
        }
        return products.get(new Random().nextInt(products.size()));
    }

    public static List<ProductInfo> getAllProductsForType(ProductType type) {
        return new ArrayList<>(productsByType.getOrDefault(type, new ArrayList<>()));
    }

    public static List<ProductInfo> getAllProducts() {
        List<ProductInfo> allProducts = new ArrayList<>();
        for (List<ProductInfo> products : productsByType.values()) {
            allProducts.addAll(products);
        }
        return allProducts;
    }

    public static boolean productExists(String name) {
        return productsByName.containsKey(name.toLowerCase());
    }

    public static ProductType getProductType(String name) {
        ProductInfo productInfo = findProductByName(name);
        if (productInfo != null) {
            for (Map.Entry<ProductType, List<ProductInfo>> entry : productsByType.entrySet()) {
                if (entry.getValue().contains(productInfo)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public static boolean isCountableType(ProductType type) {
        return type != ProductType.VEGETABLES && type != ProductType.MEAT;
    }

    public static class ProductInfo {
        private final String name;
        private final double basePrice;

        public ProductInfo(String name, double basePrice) {
            this.name = name;
            this.basePrice = basePrice;
        }

        public String getName() { return name; }
        public double getBasePrice() { return basePrice; }
    }
}