package supermarket.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import supermarket.product.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductManagerTest {

    private Warehouse warehouse;
    private SalesHall salesHall;
    private ProductManager productManager;
    private LocalDate currentDate;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        salesHall = new SalesHall();
        productManager = new ProductManager(warehouse, salesHall);
        currentDate = LocalDate.now();
    }

    @Test
    @DisplayName("Проверка пополнения всех запасов")
    void testCheckAndRestockAll() {
        Product milk = ProductFactory.createProductById("MILK");
        Product potatoes = ProductFactory.createProductById("POTATO");

        warehouse.addProduct(milk, currentDate);
        warehouse.addProduct(potatoes, currentDate);

        assertEquals(0, salesHall.getTotalProducts());

        productManager.checkAndRestockAll();

        assertTrue(salesHall.getTotalProducts() > 0);
    }

    @Test
    @DisplayName("Проверка пополнения склада когда он почти пуст")
    void testCheckAndRestockWarehouseWhenEmpty() {
        assertTrue(warehouse.isEmpty());

        productManager.checkAndRestockWarehouse();

        assertFalse(warehouse.isEmpty());
        assertTrue(warehouse.getTotalProducts() > 0);
    }

    @Test
    @DisplayName("Проверка пополнения склада когда мало товаров")
    void testCheckAndRestockWarehouseWhenLowStock() {
        warehouse.addProduct(ProductFactory.createProductById("MILK"), currentDate);
        warehouse.addProduct(ProductFactory.createProductById("YOGURT"), currentDate);
        warehouse.addProduct(ProductFactory.createProductById("CHEESE"), currentDate);

        assertTrue(warehouse.needsRestocking());

        productManager.checkAndRestockWarehouse();

        assertTrue(warehouse.getTotalProducts() >= 3);
    }

    @Test
    @DisplayName("Перемещение товаров из склада в торговый зал")
    void testTransferProductsToHall() {
        Product milk = ProductFactory.createProductById("MILK");
        Product bread = ProductFactory.createProductById("WHITE_BREAD");

        warehouse.addProduct(milk, currentDate);
        warehouse.addProduct(bread, currentDate);

        productManager.transferProductsToHall();

        assertTrue(salesHall.getTotalProducts() > 0);
        assertFalse(salesHall.findProductsById("MILK").isEmpty());
        assertFalse(salesHall.findProductsById("WHITE_BREAD").isEmpty());
    }

    @Test
    @DisplayName("Пополнение конкретного товара в торговом зале")
    void testRestockProduct() {
        Product milk = ProductFactory.createProductById("MILK");
        warehouse.addProduct(milk, currentDate);

        productManager.restockProduct("MILK");

        assertFalse(salesHall.findProductsById("MILK").isEmpty());

        assertTrue(warehouse.findProductsById("MILK").isEmpty());
    }

    @Test
    @DisplayName("Пополнение несуществующего товара")
    void testRestockNonExistentProduct() {
        productManager.restockProduct("NON_EXISTENT");

        assertTrue(salesHall.findProductsById("NON_EXISTENT").isEmpty());
    }

    @Test
    @DisplayName("Генерация доставки на пустой склад")
    void testGenerateDeliveryForEmptyWarehouse() {
        assertTrue(warehouse.isEmpty());

        productManager.generateDelivery();

        assertFalse(warehouse.isEmpty());
        assertTrue(warehouse.getTotalProducts() > 0);
    }

    @Test
    @DisplayName("Генерация доставки для склада с низким запасом")
    void testGenerateDeliveryForLowStockWarehouse() {
        warehouse.addProduct(ProductFactory.createProductById("MILK"), currentDate);
        warehouse.addProduct(ProductFactory.createProductById("WHITE_BREAD"), currentDate);

        assertTrue(warehouse.needsRestocking());

        productManager.generateDelivery();

        assertTrue(warehouse.getTotalProducts() > 2);
    }

    @Test
    @DisplayName("Генерация регулярной доставки")
    void testGenerateRegularDelivery() {
        String[] productIds = {"MILK", "YOGURT", "CHEESE", "WHITE_BREAD", "BLACK_BREAD",
                "BEEF", "PORK", "CHICKEN", "POTATO", "CARROT"};

        for (String productId : productIds) {
            Product product = ProductFactory.createProductById(productId);
            warehouse.addProduct(product, currentDate);
        }

        assertFalse(warehouse.needsRestocking());

        productManager.generateDelivery();

        assertTrue(warehouse.getTotalProducts() >= 10);
    }

    @Test
    @DisplayName("Проверка и пополнение торгового зала")
    void testCheckAndRestockSalesHall() {
        Product milk = ProductFactory.createProductById("MILK");
        warehouse.addProduct(milk, currentDate);

        productManager.checkAndRestockSalesHall();

        assertFalse(salesHall.findProductsById("MILK").isEmpty());
    }

    @Test
    @DisplayName("Пополнение товаров с низким запасом на складе")
    void testRestockLowWarehouseItems() {
        CountableProduct lowStockMilk = new CountableProduct(
                "MILK", "BATCH_LOW", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        warehouse.addProduct(lowStockMilk, currentDate);

        productManager.checkAndRestockWarehouse();

        double totalMilk = warehouse.getTotalAmount("MILK");
        assertTrue(totalMilk >= 10);
    }

    @Test
    @DisplayName("Работа с полностью пустым торговым залом")
    void testWithEmptySalesHall() {
        assertEquals(0, salesHall.getTotalProducts());

        warehouse.addProduct(ProductFactory.createProductById("MILK"), currentDate);
        warehouse.addProduct(ProductFactory.createProductById("WHITE_BREAD"), currentDate);

        productManager.checkAndRestockAll();

        assertTrue(salesHall.getTotalProducts() > 0);
    }

    @Test
    @DisplayName("Обработка ситуации когда на складе нет товаров для пополнения")
    void testRestockWhenWarehouseIsEmpty() {
        assertTrue(warehouse.isEmpty());

        productManager.checkAndRestockSalesHall();

        assertEquals(0, salesHall.getTotalProducts());
    }

    @Test
    @DisplayName("Проверка ограничения на максимальное количество перемещений")
    void testTransferLimit() {
        String[] productIds = {"MILK", "YOGURT", "CHEESE", "WHITE_BREAD", "BLACK_BREAD",
                "BEEF", "PORK", "CHICKEN", "POTATO", "CARROT",
                "TOMATO", "CUCUMBER", "PASTA", "RICE", "SUGAR"};

        for (String productId : productIds) {
            Product product = ProductFactory.createProductById(productId);
            warehouse.addProduct(product, currentDate);
        }

        int initialWarehouseCount = warehouse.getTotalProducts();
        assertEquals(15, initialWarehouseCount);

        Product existingProduct = ProductFactory.createProductById("SOAP");
        salesHall.addProduct(existingProduct, currentDate);

        productManager.transferProductsToHall();

        assertTrue(salesHall.getTotalProducts() <= 11);
        assertTrue(warehouse.getTotalProducts() >= initialWarehouseCount - 10);
    }

    @Test
    @DisplayName("Интеграционный тест полного цикла")
    void testFullCycleIntegration() {
        assertTrue(warehouse.isEmpty());
        assertTrue(salesHall.isEmpty());

        productManager.generateDelivery();
        assertFalse(warehouse.isEmpty());

        productManager.transferProductsToHall();
        assertFalse(salesHall.isEmpty());

        productManager.checkAndRestockAll();

        assertTrue(warehouse.getTotalProducts() > 0 || salesHall.getTotalProducts() > 0);
    }

    @Test
    @DisplayName("Проверка работы с разными типами товаров")
    void testWithDifferentProductTypes() {
        Product countable = ProductFactory.createProductById("MILK"); // Счетный
        Product weightable = ProductFactory.createProductById("POTATO"); // Весовой

        warehouse.addProduct(countable, currentDate);
        warehouse.addProduct(weightable, currentDate);

        productManager.transferProductsToHall();

        assertFalse(salesHall.findProductsById("MILK").isEmpty());
        assertFalse(salesHall.findProductsById("POTATO").isEmpty());

        List<Product> milkProducts = salesHall.findProductsById("MILK");
        List<Product> potatoProducts = salesHall.findProductsById("POTATO");

        assertTrue(milkProducts.get(0) instanceof CountableProduct);
        assertTrue(potatoProducts.get(0) instanceof WeightableProduct);
    }
}