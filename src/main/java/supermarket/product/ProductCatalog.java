package supermarket.product;

import java.util.*;

public class ProductCatalog {
    private static final Map<String, ProductInfo> productsById = new HashMap<>();
    private static final Map<ProductType, List<ProductInfo>> productsByType = new HashMap<>();

    static {
        addProducts(ProductType.DAIRY, Arrays.asList(
                new ProductInfo("MILK", "Молоко", 80.0),
                new ProductInfo("YOGURT", "Йогурт", 60.0),
                new ProductInfo("SOUR_CREAM", "Сметана", 90.0),
                new ProductInfo("COTTAGE_CHEESE", "Творог", 120.0),
                new ProductInfo("CHEESE", "Сыр", 300.0),
                new ProductInfo("KEFIR", "Кефир", 70.0),
                new ProductInfo("RYAZHENKA", "Ряженка", 65.0),
                new ProductInfo("CREAM", "Сливки", 110.0)
        ));

        addProducts(ProductType.BAKERY, Arrays.asList(
                new ProductInfo("WHITE_BREAD", "Хлеб белый", 50.0),
                new ProductInfo("BLACK_BREAD", "Хлеб черный", 55.0),
                new ProductInfo("BATON", "Батон", 45.0),
                new ProductInfo("SWEET_BUN", "Булочка сдобная", 35.0),
                new ProductInfo("CROISSANT", "Круассан", 60.0),
                new ProductInfo("PIROZHOK", "Пирожок", 40.0),
                new ProductInfo("BAGUETTE", "Багет", 70.0),
                new ProductInfo("CRACKERS", "Сухари", 80.0)
        ));

        addProducts(ProductType.MEAT, Arrays.asList(
                new ProductInfo("BEEF", "Говядина", 400.0),
                new ProductInfo("PORK", "Свинина", 350.0),
                new ProductInfo("CHICKEN", "Курица", 250.0),
                new ProductInfo("TURKEY", "Индейка", 300.0),
                new ProductInfo("SAUSAGE", "Колбаса", 280.0),
                new ProductInfo("SAUSAGES", "Сосиски", 200.0),
                new ProductInfo("MINCED_MEAT", "Фарш", 320.0),
                new ProductInfo("BACON", "Бекон", 450.0)
        ));

        addProducts(ProductType.VEGETABLES, Arrays.asList(
                new ProductInfo("POTATO", "Картофель", 40.0),
                new ProductInfo("CARROT", "Морковь", 50.0),
                new ProductInfo("TOMATO", "Помидоры", 150.0),
                new ProductInfo("CUCUMBER", "Огурцы", 120.0),
                new ProductInfo("APPLE", "Яблоки", 80.0),
                new ProductInfo("BANANA", "Бананы", 90.0),
                new ProductInfo("ORANGE", "Апельсины", 110.0),
                new ProductInfo("ONION", "Лук", 30.0)
        ));

        addProducts(ProductType.GROCERIES, Arrays.asList(
                new ProductInfo("PASTA", "Макароны", 60.0),
                new ProductInfo("RICE", "Рис", 80.0),
                new ProductInfo("BUCKWHEAT", "Гречка", 70.0),
                new ProductInfo("FLOUR", "Мука", 50.0),
                new ProductInfo("SUGAR", "Сахар", 45.0),
                new ProductInfo("SALT", "Соль", 20.0),
                new ProductInfo("SUNFLOWER_OIL", "Масло подсолнечное", 100.0),
                new ProductInfo("TEA", "Чай", 120.0)
        ));

        addProducts(ProductType.CHEMICALS, Arrays.asList(
                new ProductInfo("DETERGENT", "Стиральный порошок", 200.0),
                new ProductInfo("SOAP", "Мыло", 40.0),
                new ProductInfo("SHAMPOO", "Шампунь", 180.0),
                new ProductInfo("TOOTHPASTE", "Зубная паста", 90.0),
                new ProductInfo("DISH_SOAP", "Средство для мытья посуды", 80.0),
                new ProductInfo("AIR_FRESHENER", "Освежитель воздуха", 120.0),
                new ProductInfo("STAIN_REMOVER", "Пятновыводитель", 150.0),
                new ProductInfo("FABRIC_SOFTENER", "Кондиционер для белья", 160.0)
        ));

        addProducts(ProductType.ALCOHOL, Arrays.asList(
                new ProductInfo("BEER", "Пиво", 120.0),
                new ProductInfo("RED_WINE", "Вино красное", 400.0),
                new ProductInfo("WHITE_WINE", "Вино белое", 380.0),
                new ProductInfo("VODKA", "Водка", 500.0),
                new ProductInfo("WHISKEY", "Виски", 800.0),
                new ProductInfo("COGNAC", "Коньяк", 700.0),
                new ProductInfo("CHAMPAGNE", "Шампанское", 450.0),
                new ProductInfo("LIQUEUR", "Ликер", 350.0)
        ));
    }

    private static void addProducts(ProductType type, List<ProductInfo> products) {
        productsByType.put(type, products);
        for (ProductInfo product : products) {
            product.setType(type);
            productsById.put(product.getId(), product);
        }
    }

    public static ProductInfo findProductById(String id) {
        return productsById.get(id);
    }

    public static String getProductNameById(String id) {
        ProductInfo product = findProductById(id);
        return product != null ? product.getName() : null;
    }

    public static Double getProductPriceById(String id) {
        ProductInfo product = findProductById(id);
        return product != null ? product.getBasePrice() : null;
    }


    public static ProductType getProductTypeById(String id) {
        ProductInfo product = findProductById(id);
        return product != null ? product.getType() : null;
    }


    public static ProductInfo getRandomProductInfo(ProductType type) {
        List<ProductInfo> products = productsByType.get(type);
        if (products == null || products.isEmpty()) {
            return new ProductInfo("PRODUCT", "Товар", 100.0);
        }
        return products.get(new Random().nextInt(products.size()));
    }

    public static List<ProductInfo> getAllProductsForType(ProductType type) {
        return new ArrayList<>(productsByType.getOrDefault(type, new ArrayList<>()));
    }


    public static List<ProductInfo> getAllProducts() {
        return new ArrayList<>(productsById.values());
    }


    public static boolean isCountableType(ProductType type) {
        return type != ProductType.VEGETABLES && type != ProductType.MEAT;
    }


    public static boolean isCountableProduct(String productId) {
        ProductType type = getProductTypeById(productId);
        return type != null && isCountableType(type);
    }

    public static class ProductInfo {
        private final String id;
        private final String productName;
        private final double basePrice;
        private ProductType type;

        public ProductInfo(String id, String productName, double basePrice) {
            this.id = id;
            this.productName = productName;
            this.basePrice = basePrice;
        }

        public void setType(ProductType type) {
            this.type = type;
        }

        public String getId() { return id; }
        public String getName() { return productName; }
        public double getBasePrice() { return basePrice; }
        public ProductType getType() { return type; }
    }
}