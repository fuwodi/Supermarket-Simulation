package supermarket.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import supermarket.product.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseTest {

    private Warehouse warehouse;
    private LocalDate currentDate;
    private CountableProduct countableProduct;
    private WeightableProduct weightableProduct;

    @BeforeEach
    void setUp() {
        warehouse = new Warehouse();
        currentDate = LocalDate.now();

        countableProduct = new CountableProduct(
                "MILK", "BATCH_001", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 20
        );

        weightableProduct = new WeightableProduct(
                "POTATO", "BATCH_002", "Картофель", ProductType.VEGETABLES,
                40.0, currentDate, 10, 10.0
        );
    }

    @Test
    void testAddProduct() {
        boolean added = warehouse.addProduct(countableProduct, currentDate);

        assertTrue(added);
        assertEquals(1, warehouse.getTotalProducts());
        assertNotNull(warehouse.getProduct("MILK"));
        assertEquals("Молоко", warehouse.getProduct("MILK").getName());
    }

    @Test
    void testRejectExpiredProduct() {
        LocalDate expiredDate = currentDate.minusDays(20);
        CountableProduct expiredProduct = new CountableProduct(
                "EXPIRED", "BATCH_EXP", "Просроченный товар", ProductType.DAIRY,
                80.0, expiredDate, 7, 5
        );

        boolean added = warehouse.addProduct(expiredProduct, currentDate);

        assertFalse(added);
        assertEquals(0, warehouse.getTotalProducts());
        assertNull(warehouse.getProduct("EXPIRED"));
    }

    @Test
    void testRemoveExpiredProducts() {
        CountableProduct soonToExpire = new CountableProduct(
                "SOON_EXP", "BATCH_SOON", "Скоропортящийся товар", ProductType.DAIRY,
                80.0, currentDate, 3, 5
        );

        CountableProduct freshProduct = new CountableProduct(
                "FRESH", "BATCH_FRESH", "Свежий товар", ProductType.DAIRY,
                80.0, currentDate, 10, 5
        );

        warehouse.addProduct(soonToExpire, currentDate);
        warehouse.addProduct(freshProduct, currentDate);
        assertEquals(2, warehouse.getTotalProducts());

        LocalDate futureDate = currentDate.plusDays(4);
        int removed = warehouse.removeExpiredProducts(futureDate);

        assertEquals(1, removed);
        assertEquals(1, warehouse.getTotalProducts());
        assertNull(warehouse.getProduct("SOON_EXP"));
        assertNotNull(warehouse.getProduct("FRESH"));
    }

    @Test
    void testRemoveExpiredProductsWhenNone() {
        warehouse.addProduct(countableProduct, currentDate);
        assertEquals(1, warehouse.getTotalProducts());

        int removed = warehouse.removeExpiredProducts(currentDate);

        assertEquals(0, removed);
        assertEquals(1, warehouse.getTotalProducts());
    }

    @Test
    void testFindProductsById() {
        CountableProduct milk1 = new CountableProduct(
                "MILK", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct milk2 = new CountableProduct(
                "MILK", "BATCH_2", "Молоко", ProductType.DAIRY,
                85.0, currentDate, 7, 15
        );

        warehouse.addProduct(milk1, currentDate);
        warehouse.addProduct(milk2, currentDate);

        List<Product> milkProducts = warehouse.findProductsById("MILK");
        List<Product> nonExistent = warehouse.findProductsById("NON_EXISTENT");

        assertEquals(2, milkProducts.size());
        assertTrue(nonExistent.isEmpty());
    }

    @Test
    void testGetBatchesForProduct() {
        CountableProduct milk1 = new CountableProduct(
                "MILK", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct milk2 = new CountableProduct(
                "MILK", "BATCH_2", "Молоко", ProductType.DAIRY,
                85.0, currentDate, 7, 15
        );

        warehouse.addProduct(milk1, currentDate);
        warehouse.addProduct(milk2, currentDate);

        List<Product> batches = warehouse.getBatchesForProduct("MILK");

        assertEquals(2, batches.size());
    }

    @Test
    void testRemoveProduct() {
        warehouse.addProduct(countableProduct, currentDate);
        assertEquals(1, warehouse.getTotalProducts());

        warehouse.removeProduct("MILK");

        assertNull(warehouse.getProduct("MILK"));
        assertEquals(0, warehouse.getTotalProducts());
    }

    @Test
    void testRemoveBatch() {
        CountableProduct milk1 = new CountableProduct(
                "MILK", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct milk2 = new CountableProduct(
                "MILK", "BATCH_2", "Молоко", ProductType.DAIRY,
                85.0, currentDate, 7, 15
        );

        warehouse.addProduct(milk1, currentDate);
        warehouse.addProduct(milk2, currentDate);

        warehouse.removeBatch("MILK", "BATCH_1");

        List<Product> batches = warehouse.findProductsById("MILK");
        assertEquals(1, batches.size());
        assertEquals("BATCH_2", batches.get(0).getBatchId());
    }

    @Test
    void testGetAllProducts() {
        warehouse.addProduct(countableProduct, currentDate);
        warehouse.addProduct(weightableProduct, currentDate);

        var allProducts = warehouse.getAllProducts();

        assertEquals(2, allProducts.size());
        assertTrue(allProducts.containsKey("MILK"));
        assertTrue(allProducts.containsKey("POTATO"));
    }

    @Test
    void testGetTotalAmount() {
        CountableProduct milk1 = new CountableProduct(
                "MILK", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct milk2 = new CountableProduct(
                "MILK", "BATCH_2", "Молоко", ProductType.DAIRY,
                85.0, currentDate, 7, 15
        );

        warehouse.addProduct(milk1, currentDate);
        warehouse.addProduct(milk2, currentDate);

        double totalAmount = warehouse.getTotalAmount("MILK");
        assertEquals(25.0, totalAmount);
    }

    @Test
    void testNeedsRestocking() {
        assertTrue(warehouse.isEmpty());
        assertTrue(warehouse.needsRestocking());

        String[] productIds = {"MILK", "YOGURT", "CHEESE", "KEFIR", "SOUR_CREAM"};
        for (String productId : productIds) {
            Product product = ProductFactory.createProductById(productId);
            warehouse.addProduct(product, currentDate);
        }

        assertEquals(5, warehouse.getTotalProducts());
        assertTrue(warehouse.needsRestocking());

        String[] moreProductIds = { "CREAM", "RYAZHENKA", "COTTAGE_CHEESE", "WHITE_BREAD"};
        for (String productId : moreProductIds) {
            Product product = ProductFactory.createProductById(productId);
            warehouse.addProduct(product, currentDate);
        }

        assertEquals(9, warehouse.getTotalProducts());
        assertFalse(warehouse.needsRestocking());
    }

    @Test
    void testIsEmpty() {
        assertTrue(warehouse.isEmpty());

        warehouse.addProduct(countableProduct, currentDate);

        assertFalse(warehouse.isEmpty());
    }

    @Test
    void testGetLowStockProductIds() {
        CountableProduct lowStockMilk = new CountableProduct(
                "MILK", "BATCH_LOW", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct highStockCheese = new CountableProduct(
                "CHEESE", "BATCH_HIGH", "Сыр", ProductType.DAIRY,
                300.0, currentDate, 7, 30
        );

        warehouse.addProduct(lowStockMilk, currentDate);
        warehouse.addProduct(highStockCheese, currentDate);

        List<String> lowStockIds = warehouse.getLowStockProductIds();

        assertEquals(1, lowStockIds.size());
        assertTrue(lowStockIds.contains("MILK"));
        assertFalse(lowStockIds.contains("CHEESE"));
    }

    @Test
    void testGetTotalBatches() {
        CountableProduct milk1 = new CountableProduct(
                "MILK", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct milk2 = new CountableProduct(
                "MILK", "BATCH_2", "Молоко", ProductType.DAIRY,
                85.0, currentDate, 7, 15
        );

        warehouse.addProduct(milk1, currentDate);
        warehouse.addProduct(milk2, currentDate);

        int totalBatches = warehouse.getTotalBatches();
        assertEquals(2, totalBatches);
    }
}