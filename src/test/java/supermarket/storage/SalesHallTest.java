/*package supermarket.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import supermarket.SupermarketConfig;
import supermarket.product.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SalesHallTest {

    private SalesHall salesHall;
    private LocalDate currentDate;
    private CountableProduct countableProduct;
    private WeightableProduct weightableProduct;

    @BeforeEach
    void setUp() {
        salesHall = new SalesHall();
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
        boolean added = salesHall.addProduct(countableProduct, currentDate);

        assertTrue(added);
        assertEquals(1, salesHall.getTotalProducts());
        assertNotNull(salesHall.getProduct("MILK"));
        assertEquals("Молоко", salesHall.getProduct("MILK").getName());
    }

    @Test
    void testAddExpiredProduct() {
        CountableProduct expiredProduct = new CountableProduct(
                "EXPIRED", "BATCH_EXP", "Просроченный товар", ProductType.DAIRY,
                80.0, currentDate.minusDays(10), 5, 10
        );

        boolean added = salesHall.addProduct(expiredProduct, currentDate);

        assertFalse(added);
        assertEquals(0, salesHall.getTotalProducts());
    }

    @Test
    void testAddProductWithSameBatchId() {
        CountableProduct product1 = new CountableProduct(
                "MILK", "BATCH_SAME", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct product2 = new CountableProduct(
                "MILK", "BATCH_SAME", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 15
        );

        salesHall.addProduct(product1, currentDate);
        salesHall.addProduct(product2, currentDate);

        List<Product> batches = salesHall.findProductsById("MILK");
        assertEquals(1, batches.size()); // Должны объединиться
        assertEquals(25, ((CountableProduct) batches.get(0)).getQuantity());
    }

    @Test
    void testFindProductsById() {
        salesHall.addProduct(countableProduct, currentDate);
        salesHall.addProduct(weightableProduct, currentDate);

        List<Product> milkProducts = salesHall.findProductsById("MILK");
        List<Product> potatoProducts = salesHall.findProductsById("POTATO");
        List<Product> nonExistent = salesHall.findProductsById("NON_EXISTENT");

        assertEquals(1, milkProducts.size());
        assertEquals(1, potatoProducts.size());
        assertTrue(nonExistent.isEmpty());
    }

    @Test
    void testRemoveExpiredProducts() {
        CountableProduct shortLifeProduct = new CountableProduct(
                "SHORT_LIFE", "BATCH_SHORT", "Товар с коротким сроком", ProductType.BAKERY,
                50.0, currentDate, 3, 10
        );

        // Добавляем оба товара
        salesHall.addProduct(shortLifeProduct, currentDate);
        salesHall.addProduct(countableProduct, currentDate);

        assertEquals(2, salesHall.getTotalProducts());

        LocalDate futureDate = currentDate.plusDays(4);

        assertTrue(shortLifeProduct.isExpired(futureDate));
        assertFalse(countableProduct.isExpired(futureDate));

        int removed = salesHall.removeExpiredProducts(futureDate);

        assertEquals(1, removed);
        assertEquals(1, salesHall.getTotalProducts());
        assertTrue(salesHall.findProductsById("SHORT_LIFE").isEmpty());
        assertFalse(salesHall.findProductsById("MILK").isEmpty());
    }

    @Test
    void testApplyDiscountToProductById() {
        salesHall.addProduct(countableProduct, currentDate);

        salesHall.applyDiscountToProductById("MILK", 0.2);

        Product product = salesHall.getProduct("MILK");
        assertEquals(0.2, product.getDiscount());
        assertEquals(64.0, product.getFinalPrice(), 0.01);
    }

    @Test
    void testApplyDiscountToBatch() {
        CountableProduct product1 = new CountableProduct(
                "MILK", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct product2 = new CountableProduct(
                "MILK", "BATCH_2", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 15
        );

        salesHall.addProduct(product1, currentDate);
        salesHall.addProduct(product2, currentDate);

        salesHall.applyDiscountToBatch("MILK", "BATCH_1", 0.3);

        List<Product> batches = salesHall.findProductsById("MILK");
        Product batch1 = batches.stream()
                .filter(p -> p.getBatchId().equals("BATCH_1"))
                .findFirst()
                .orElse(null);
        Product batch2 = batches.stream()
                .filter(p -> p.getBatchId().equals("BATCH_2"))
                .findFirst()
                .orElse(null);

        assertNotNull(batch1);
        assertNotNull(batch2);
        assertEquals(0.3, batch1.getDiscount());
        assertEquals(0.0, batch2.getDiscount());
    }

    @Test
    void testApplyExpiringDiscounts() {
        CountableProduct soonToExpire = new CountableProduct(
                "SOON_EXP", "BATCH_SOON", "Скоропортящийся товар", ProductType.DAIRY,
                100.0, currentDate, 2, 10
        );

        salesHall.addProduct(soonToExpire, currentDate);

        LocalDate nearExpiryDate = currentDate.plusDays(1);
        int discountCount = salesHall.applyExpiringDiscounts(nearExpiryDate);

        assertEquals(1, discountCount);
        Product product = salesHall.getProduct("SOON_EXP");
        assertEquals(SupermarketConfig.EXPIRING_DISCOUNT, product.getDiscount());
    }

    @Test
    void testApplyRandomDiscounts() {
        salesHall.addProduct(countableProduct, currentDate);
        salesHall.addProduct(weightableProduct, currentDate);

        salesHall.applyRandomDiscounts();

        assertTrue(salesHall.getTotalProducts() > 0);
    }

    @Test
    void testRemoveAllDiscounts() {
        countableProduct.setDiscount(0.2);
        weightableProduct.setDiscount(0.15);
        salesHall.addProduct(countableProduct, currentDate);
        salesHall.addProduct(weightableProduct, currentDate);

        salesHall.removeAllDiscounts();

        for (List<Product> batches : salesHall.getAllProducts().values()) {
            for (Product product : batches) {
                assertEquals(0.0, product.getDiscount());
            }
        }
    }

    @Test
    void testGetLowStockProductIds() {
        CountableProduct lowStockMilk = new CountableProduct(
                "MILK", "BATCH_LOW", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 3  // Меньше порога 10
        );

        CountableProduct highStockCheese = new CountableProduct(
                "CHEESE", "BATCH_HIGH", "Сыр", ProductType.DAIRY,
                300.0, currentDate, 7, 20  // Больше порога 10
        );

        WeightableProduct lowStockPotatoes = new WeightableProduct(
                "POTATO", "BATCH_LOW", "Картофель", ProductType.VEGETABLES,
                40.0, currentDate, 10, 3.0  // Меньше порога 5.0
        );

        salesHall.addProduct(lowStockMilk, currentDate);
        salesHall.addProduct(highStockCheese, currentDate);
        salesHall.addProduct(lowStockPotatoes, currentDate);

        List<String> lowStockIds = salesHall.getLowStockProductIds();

        assertEquals(2, lowStockIds.size());
        assertTrue(lowStockIds.contains("MILK"));
        assertTrue(lowStockIds.contains("POTATO"));
        assertFalse(lowStockIds.contains("CHEESE"));
    }

    @Test
    void testGetAllProducts() {
        salesHall.addProduct(countableProduct, currentDate);
        salesHall.addProduct(weightableProduct, currentDate);

        Map<String, List<Product>> allProducts = salesHall.getAllProducts();

        assertEquals(2, allProducts.size());
        assertTrue(allProducts.containsKey("MILK"));
        assertTrue(allProducts.containsKey("POTATO"));
    }

    @Test
    void testGetProductsList() {
        salesHall.addProduct(countableProduct, currentDate);
        salesHall.addProduct(weightableProduct, currentDate);

        List<Product> productsList = salesHall.getProductsList();

        assertEquals(2, productsList.size());
    }

    @Test
    void testGetTotalAmount() {
        CountableProduct milk1 = new CountableProduct(
                "MILK", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct milk2 = new CountableProduct(
                "MILK", "BATCH_2", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 15
        );

        salesHall.addProduct(milk1, currentDate);
        salesHall.addProduct(milk2, currentDate);

        double totalAmount = salesHall.getTotalAmount("MILK");
        assertEquals(25.0, totalAmount);
    }

    @Test
    void testRemoveBatch() {
        CountableProduct product1 = new CountableProduct(
                "MILK", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        CountableProduct product2 = new CountableProduct(
                "MILK", "BATCH_2", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 15
        );

        salesHall.addProduct(product1, currentDate);
        salesHall.addProduct(product2, currentDate);

        salesHall.removeBatch("MILK", "BATCH_1");

        List<Product> batches = salesHall.findProductsById("MILK");
        assertEquals(1, batches.size());
        assertEquals("BATCH_2", batches.get(0).getBatchId());
    }

    @Test
    void testRemoveProduct() {
        salesHall.addProduct(countableProduct, currentDate);

        salesHall.removeProduct("MILK");

        assertNull(salesHall.getProduct("MILK"));
        assertEquals(0, salesHall.getTotalProducts());
    }
}*/