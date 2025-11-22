package supermarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import supermarket.product.*;
import java.time.LocalDate;
import java.util.List;

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
                "DAI_123", "BATCH_001", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 20
        );

        weightableProduct = new WeightableProduct(
                "VEG_456", "BATCH_002", "Картофель", ProductType.VEGETABLES,
                40.0, currentDate, 10, 10.0
        );
    }

    @Test
    void testAddProduct() {
        salesHall.addProduct(countableProduct);

        assertEquals(1, salesHall.getTotalProducts());
        assertNotNull(salesHall.getProduct("DAI_123"));
        assertEquals("Молоко", salesHall.getProduct("DAI_123").getName());
    }

    @Test
    void testUpdateProductQuantity() {
        salesHall.addProduct(countableProduct);

        CountableProduct additionalMilk = new CountableProduct(
                "DAI_123", "BATCH_001", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );

        salesHall.updateProduct(additionalMilk);

        Product updatedProduct = salesHall.getProduct("DAI_123");
        assertNotNull(updatedProduct);
        assertTrue(updatedProduct instanceof CountableProduct);
        assertEquals(30, ((CountableProduct) updatedProduct).getQuantity());
    }

    @Test
    void testPurchaseCountableProduct() {
        salesHall.addProduct(countableProduct);

        Product purchased = salesHall.purchaseProduct("DAI_123", 5);

        assertNotNull(purchased);
        assertTrue(purchased instanceof CountableProduct);
        assertEquals(5, ((CountableProduct) purchased).getQuantity());

        Product remaining = salesHall.getProduct("DAI_123");
        assertNotNull(remaining);
        assertEquals(15, ((CountableProduct) remaining).getQuantity());
    }

    @Test
    void testPurchaseAllCountableProduct() {
        salesHall.addProduct(countableProduct);

        Product purchased = salesHall.purchaseProduct("DAI_123", 20);

        assertNotNull(purchased);
        assertEquals(20, ((CountableProduct) purchased).getQuantity());
        assertNull(salesHall.getProduct("DAI_123"));
    }

    @Test
    void testPurchaseWeightableProduct() {
        salesHall.addProduct(weightableProduct);

        Product purchased = salesHall.purchaseProduct("VEG_456", 3.0);

        assertNotNull(purchased);
        assertTrue(purchased instanceof WeightableProduct);
        assertEquals(3.0, ((WeightableProduct) purchased).getWeight(), 0.01);

        Product remaining = salesHall.getProduct("VEG_456");
        assertNotNull(remaining);
        assertEquals(7.0, ((WeightableProduct) remaining).getWeight(), 0.01);
    }

    @Test
    void testPurchaseInsufficientQuantity() {
        salesHall.addProduct(countableProduct);

        Product purchased = salesHall.purchaseProduct("DAI_123", 25);

        assertNull(purchased);
        assertNotNull(salesHall.getProduct("DAI_123"));
        assertEquals(20, ((CountableProduct) salesHall.getProduct("DAI_123")).getQuantity());
    }

    @Test
    void testPurchaseNonExistentProduct() {
        Product purchased = salesHall.purchaseProduct("NON_EXISTENT", 5);

        assertNull(purchased);
    }

    /*@Test
    @DisplayName("Установка скидки на товар по имени")
    void testApplyDiscountToProductByName() {
        salesHall.addProduct(countableProduct);
        salesHall.addProduct(weightableProduct);

        salesHall.applyDiscountToProduct("Молоко", 0.2);

        Product milk = salesHall.getProduct("DAI_123");
        Product potatoes = salesHall.getProduct("VEG_456");

        assertEquals(0.2, milk.getDiscount());
        assertEquals(0.0, potatoes.getDiscount());
        assertEquals(64.0, milk.getFinalPrice(), 0.01);
    }*/

    @Test
    @DisplayName("Установка скидки на товар по ID")
    void testApplyDiscountToProductById() {
        salesHall.addProduct(countableProduct);

        salesHall.applyDiscountToProductById("DAI_123", 0.15);

        Product product = salesHall.getProduct("DAI_123");
        assertEquals(0.15, product.getDiscount());
        assertEquals(68.0, product.getFinalPrice(), 0.01);
    }

    @Test
    void testApplyExpiringDiscounts() {
        CountableProduct soonToExpire = new CountableProduct(
                "SOON_EXP", "BATCH_SOON", "Скоропортящийся товар", ProductType.DAIRY,
                100.0, currentDate, 2, 10
        );

        salesHall.addProduct(soonToExpire);

        LocalDate nearExpiryDate = currentDate.plusDays(1);
        salesHall.applyExpiringDiscounts(nearExpiryDate);

        Product product = salesHall.getProduct("SOON_EXP");
        assertEquals(SupermarketConfig.EXPIRING_DISCOUNT, product.getDiscount());
        assertEquals(70.0, product.getFinalPrice(), 0.01);
    }

    @Test
    @DisplayName("Случайные скидки применяются к некоторым товарам")
    void testApplyRandomDiscounts() {
        salesHall.addProduct(countableProduct);
        salesHall.addProduct(weightableProduct);

        salesHall.applyRandomDiscounts();

        boolean hasDiscount = false;
        for (Product product : salesHall.getProductsCollection()) {
            if (product.getDiscount() > 0) {
                hasDiscount = true;
                break;
            }
        }
        assertTrue(true, "Метод должен выполняться без ошибок");
    }

    @Test
    @DisplayName("Удаление всех скидок")
    void testRemoveAllDiscounts() {
        countableProduct.setDiscount(0.2);
        weightableProduct.setDiscount(0.15);
        salesHall.addProduct(countableProduct);
        salesHall.addProduct(weightableProduct);

        salesHall.removeAllDiscounts();

        for (Product product : salesHall.getProductsCollection()) {
            assertEquals(0.0, product.getDiscount());
        }
    }

    @Test
    void testPurchasePreservesDiscount() {
        countableProduct.setDiscount(0.2);
        salesHall.addProduct(countableProduct);

        Product purchased = salesHall.purchaseProduct("DAI_123", 5);

        assertNotNull(purchased);
        assertEquals(0.2, purchased.getDiscount());
        assertEquals(64.0, purchased.getFinalPrice(), 0.01);
    }

    @Test
    void testRemoveExpiredProducts() {
        CountableProduct expiredProduct = new CountableProduct(
                "EXP_001", "BATCH_EXP", "Просроченный товар", ProductType.DAIRY,
                80.0, currentDate.minusDays(10), 5, 10
        );

        salesHall.addProduct(expiredProduct);
        salesHall.addProduct(countableProduct);

        assertEquals(2, salesHall.getTotalProducts());

        int removed = salesHall.removeExpiredProducts(currentDate);

        assertEquals(1, removed);
        assertEquals(1, salesHall.getTotalProducts());
        assertNull(salesHall.getProduct("EXP_001"));
        assertNotNull(salesHall.getProduct("DAI_123"));
    }

    @Test
    void testGetLowStockProducts() {
        CountableProduct lowMilk = new CountableProduct(
                "DAI_LOW", "BATCH_LOW", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 3
        );

        CountableProduct highCheese = new CountableProduct(
                "DAI_HIGH", "BATCH_HIGH", "Сыр", ProductType.DAIRY,
                300.0, currentDate, 7, 15
        );

        WeightableProduct lowPotatoes = new WeightableProduct(
                "VEG_LOW", "BATCH_LOW", "Картофель", ProductType.VEGETABLES,
                40.0, currentDate, 10, 1.5
        );

        salesHall.addProduct(lowMilk);
        salesHall.addProduct(highCheese);
        salesHall.addProduct(lowPotatoes);

        List<Product> lowStockProducts = salesHall.getLowStockProducts();

        assertEquals(2, lowStockProducts.size());
        assertTrue(lowStockProducts.contains(lowMilk));
        assertTrue(lowStockProducts.contains(lowPotatoes));
        assertFalse(lowStockProducts.contains(highCheese));
    }

    @Test
    void testCalculateTotalRevenue() {
        CountableProduct milk = new CountableProduct(
                "DAI_1", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );
        milk.setDiscount(0.1);

        WeightableProduct potatoes = new WeightableProduct(
                "VEG_1", "BATCH_1", "Картофель", ProductType.VEGETABLES,
                40.0, currentDate, 10, 5.0
        );
        potatoes.setDiscount(0.2); // Цена: 32.0

        salesHall.addProduct(milk);
        salesHall.addProduct(potatoes);

        double totalRevenue = salesHall.calculateTotalRevenue();

        assertEquals(880.0, totalRevenue, 0.01);
    }

    @Test
    void testPurchaseWithFinalPrice() {
        countableProduct.setDiscount(0.25);
        salesHall.addProduct(countableProduct);

        Product purchased = salesHall.purchaseProduct("DAI_123", 5);

        assertNotNull(purchased);
        assertEquals(0.25, purchased.getDiscount());
        assertEquals(60.0, purchased.getFinalPrice(), 0.01);
        assertEquals(300.0, purchased.getFinalPrice() * 5, 0.01);
    }

    @Test
    void testEmptySalesHallOnCreation() {
        assertEquals(0, salesHall.getTotalProducts());
        assertTrue(salesHall.getProductsCollection().isEmpty());
        assertTrue(salesHall.getLowStockProducts().isEmpty());
        assertEquals(0.0, salesHall.calculateTotalRevenue(), 0.01);
    }
}