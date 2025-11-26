package supermarket.product;

import org.junit.jupiter.api.Test;
import supermarket.SupermarketConfig;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testCountableProductCreation() {
        CountableProduct product = new CountableProduct(
                "CP001", "BATCH001", "Молоко", ProductType.DAIRY, 80.0,
                LocalDate.of(2024, 1, 1), 7, 10
        );

        assertEquals("CP001", product.getId());
        assertEquals("BATCH001", product.getBatchId());
        assertEquals("Молоко", product.getName());
        assertEquals(ProductType.DAIRY, product.getType());
        assertEquals(80.0, product.getPrice());
        assertEquals(10, product.getQuantity());
        assertEquals(0.0, product.getDiscount());
    }

    @Test
    void testWeightableProductCreation() {
        WeightableProduct product = new WeightableProduct(
                "WP001", "BATCH002", "Яблоки", ProductType.VEGETABLES, 100.0,
                LocalDate.of(2024, 1, 1), 10, 5.5
        );

        assertEquals("WP001", product.getId());
        assertEquals("BATCH002", product.getBatchId());
        assertEquals("Яблоки", product.getName());
        assertEquals(ProductType.VEGETABLES, product.getType());
        assertEquals(100.0, product.getPrice());
        assertEquals(5.5, product.getWeight());
    }

    @Test
    void testIsExpired() {
        CountableProduct expiredProduct = new CountableProduct(
                "CP002", "BATCH003", "Йогурт", ProductType.DAIRY, 60.0,
                LocalDate.of(2024, 1, 1), 3, 5
        );

        LocalDate currentDate = LocalDate.of(2024, 1, 5);
        assertTrue(expiredProduct.isExpired(currentDate));

        LocalDate freshDate = LocalDate.of(2024, 1, 3);
        assertFalse(expiredProduct.isExpired(freshDate));
    }

    @Test
    void testExpiresSoon() {
        CountableProduct product = new CountableProduct(
                "CP003", "BATCH004", "Молоко", ProductType.DAIRY, 80.0,
                LocalDate.of(2024, 1, 1), 5, 10
        );

        // За 2 дня до истечения срока (должен быть true)
        LocalDate nearExpiry = LocalDate.of(2024, 1, 4);
        assertTrue(product.expiresSoon(nearExpiry));

        // За 3 дня до истечения срока (должен быть false)
        LocalDate notNearExpiry = LocalDate.of(2024, 1, 3);
        assertFalse(product.expiresSoon(notNearExpiry));
    }

    @Test
    void testGetFinalPrice() {
        CountableProduct product = new CountableProduct(
                "CP004", "BATCH005", "Хлеб", ProductType.BAKERY, 50.0,
                LocalDate.now(), 3, 5
        );

        assertEquals(50.0, product.getFinalPrice());

        product.setDiscount(0.2);
        assertEquals(40.0, product.getFinalPrice());
    }

    @Test
    void testGetExpiryDate() {
        CountableProduct product = new CountableProduct(
                "CP005", "BATCH006", "Сыр", ProductType.DAIRY, 300.0,
                LocalDate.of(2024, 1, 1), 10, 3
        );

        LocalDate expectedExpiry = LocalDate.of(2024, 1, 11);
        assertEquals(expectedExpiry, product.getExpiryDate());
    }

    @Test
    void testCountableProductDecreaseQuantity() {
        CountableProduct product = new CountableProduct(
                "CP006", "BATCH007", "Кефир", ProductType.DAIRY, 70.0,
                LocalDate.now(), 5, 10
        );

        product.decreaseQuantity(3);
        assertEquals(7, product.getQuantity());

        product.decreaseQuantity(10);
        assertEquals(0, product.getQuantity());
    }

    @Test
    void testWeightableProductDecreaseWeight() {
        WeightableProduct product = new WeightableProduct(
                "WP003", "BATCH008", "Картофель", ProductType.VEGETABLES, 40.0,
                LocalDate.now(), 14, 10.0
        );

        product.decreaseWeight(3.5);
        assertEquals(6.5, product.getWeight());
    }

    @Test
    void testToString() {
        CountableProduct product = new CountableProduct(
                "CP007", "BATCH009", "Творог", ProductType.DAIRY, 120.0,
                LocalDate.now(), 7, 8
        );

        product.setDiscount(0.1);
        String result = product.toString();

        assertTrue(result.contains("Творог"));
        assertTrue(result.contains("CP007"));
        assertTrue(result.contains("108.00"));
        assertTrue(result.contains("скидка: 10%"));
    }

    @Test
    void testSetQuantity() {
        CountableProduct product = new CountableProduct(
                "CP008", "BATCH010", "Йогурт", ProductType.DAIRY, 60.0,
                LocalDate.now(), 7, 5
        );

        product.setQuantity(15);
        assertEquals(15, product.getQuantity());
    }

    @Test
    void testSetWeight() {
        WeightableProduct product = new WeightableProduct(
                "WP004", "BATCH011", "Морковь", ProductType.VEGETABLES, 50.0,
                LocalDate.now(), 10, 8.0
        );

        product.setWeight(12.5);
        assertEquals(12.5, product.getWeight());
    }
}