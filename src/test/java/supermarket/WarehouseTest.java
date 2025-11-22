package supermarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import supermarket.product.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Collection;

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
        boolean added = warehouse.addProduct(countableProduct, currentDate);

        assertTrue(added);
        assertEquals(1, warehouse.getTotalProducts());
        assertNotNull(warehouse.getProduct("DAI_123"));
        assertEquals("Молоко", warehouse.getProduct("DAI_123").getName());
    }

    @Test
    void testRejectExpiredProduct() {
        LocalDate expiredDate = currentDate.minusDays(20);
        CountableProduct expiredProduct = new CountableProduct(
                "EXP_001", "BATCH_EXP", "Просроченный товар", ProductType.DAIRY,
                80.0, expiredDate, 7, 5
        );

        boolean added = warehouse.addProduct(expiredProduct, currentDate);

        assertFalse(added);
        assertEquals(0, warehouse.getTotalProducts());
        assertNull(warehouse.getProduct("EXP_001"));
    }

    @Test
    void testTransferAllCountableProduct() {
        warehouse.addProduct(countableProduct, currentDate);

        Product transferred = warehouse.transferProduct("DAI_123", 20, currentDate);

        assertNotNull(transferred);
        assertTrue(transferred instanceof CountableProduct);
        assertEquals(20, ((CountableProduct) transferred).getQuantity());
        assertEquals("Молоко", transferred.getName());
        assertEquals("BATCH_001", ((CountableProduct) transferred).getBatchId());
        assertNull(warehouse.getProduct("DAI_123"));
    }

    @Test
    void testTransferPartialCountableProduct() {
        warehouse.addProduct(countableProduct, currentDate);

        Product transferred = warehouse.transferProduct("DAI_123", 5, currentDate);

        assertNotNull(transferred);
        assertEquals(5, ((CountableProduct) transferred).getQuantity());

        Product remaining = warehouse.getProduct("DAI_123");
        assertNotNull(remaining);
        assertEquals(15, ((CountableProduct) remaining).getQuantity());
    }

    @Test
    void testTransferPartialQuantityWhenInsufficient() {
        warehouse.addProduct(countableProduct, currentDate);

        Product transferred = warehouse.transferProduct("DAI_123", 25, currentDate);

        assertNotNull(transferred, "Должны получить то, что есть в наличии");
        assertEquals(20, ((CountableProduct) transferred).getQuantity(), "Должны отдать все 20 шт.");
        assertNull(warehouse.getProduct("DAI_123"), "Товар должен быть удален со склада после полного перемещения");
    }

    @Test
    void testTransferAllWeightableProduct() {
        warehouse.addProduct(weightableProduct, currentDate);

        Product transferred = warehouse.transferProduct("VEG_456", 10.0, currentDate);

        assertNotNull(transferred);
        assertTrue(transferred instanceof WeightableProduct);
        assertEquals(10.0, ((WeightableProduct) transferred).getWeight(), 0.01);
        assertEquals("Картофель", transferred.getName());
        assertEquals("BATCH_002", ((WeightableProduct) transferred).getBatchId());
        assertNull(warehouse.getProduct("VEG_456"));
    }

    @Test
    void testTransferPartialWeightableProduct() {
        warehouse.addProduct(weightableProduct, currentDate);

        Product transferred = warehouse.transferProduct("VEG_456", 3.0, currentDate);

        assertNotNull(transferred);
        assertEquals(3.0, ((WeightableProduct) transferred).getWeight(), 0.01);

        Product remaining = warehouse.getProduct("VEG_456");
        assertNotNull(remaining);
        assertEquals(7.0, ((WeightableProduct) remaining).getWeight(), 0.01);
    }

    @Test
    void testTransferPartialWeightWhenInsufficient() {
        warehouse.addProduct(weightableProduct, currentDate);

        Product transferred = warehouse.transferProduct("VEG_456", 15.0, currentDate);

        assertNotNull(transferred, "Должны получить то, что есть в наличии");
        assertEquals(10.0, ((WeightableProduct) transferred).getWeight(), 0.01, "Должны отдать все 10.0 кг");
        assertNull(warehouse.getProduct("VEG_456"), "Товар должен быть удален со склада после полного перемещения");
    }

    @Test
    void testTransferNonExistentProduct() {
        Product transferredCountable = warehouse.transferProduct("NON_EXISTENT", 5, currentDate);
        Product transferredWeightable = warehouse.transferProduct("NON_EXISTENT", 2.0, currentDate);

        assertNull(transferredCountable);
        assertNull(transferredWeightable);
    }

    @Test
    void testTransferProductThatBecameExpired() {
        CountableProduct soonToExpire = new CountableProduct(
                "SOON_EXP", "BATCH_SOON", "Скоропортящийся товар", ProductType.DAIRY,
                80.0, currentDate, 2, 10
        );

        warehouse.addProduct(soonToExpire, currentDate);
        assertNotNull(warehouse.getProduct("SOON_EXP"));

        LocalDate futureDate = currentDate.plusDays(3);

        Product transferred = warehouse.transferProduct("SOON_EXP", 5, futureDate);

        assertNull(transferred, "Не должны перемещать просроченный товар");
        assertNotNull(warehouse.getProduct("SOON_EXP"), "Товар должен остаться на складе");
    }

    @Test
    void testTransferZeroQuantity() {
        warehouse.addProduct(countableProduct, currentDate);

        Product transferred = warehouse.transferProduct("DAI_123", 0, currentDate);

        assertNull(transferred);
        assertNotNull(warehouse.getProduct("DAI_123"));
    }

    @Test
    void testTransferNegativeQuantity() {
        warehouse.addProduct(countableProduct, currentDate);

        Product transferred = warehouse.transferProduct("DAI_123", -5, currentDate);

        assertNull(transferred);
        assertNotNull(warehouse.getProduct("DAI_123"));
    }

    @Test
    void testRemoveExpiredProducts() {
        CountableProduct soonToExpire = new CountableProduct(
                "EXP_001", "BATCH_EXP", "Скоропортящийся товар", ProductType.DAIRY,
                80.0, currentDate, 3, 5
        );

        CountableProduct freshProduct = new CountableProduct(
                "FRESH_001", "BATCH_FRESH", "Свежий товар", ProductType.DAIRY,
                80.0, currentDate, 10, 5
        );

        warehouse.addProduct(soonToExpire, currentDate);
        warehouse.addProduct(freshProduct, currentDate);
        assertEquals(2, warehouse.getTotalProducts());

        LocalDate futureDate = currentDate.plusDays(4);
        int removed = warehouse.removeExpiredProducts(futureDate);

        assertEquals(1, removed);
        assertEquals(1, warehouse.getTotalProducts());
        assertNull(warehouse.getProduct("EXP_001"));
        assertNotNull(warehouse.getProduct("FRESH_001"));
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
    void testFindProductsByName() {
        CountableProduct milk1 = new CountableProduct(
                "DAI_1", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );
        CountableProduct milk2 = new CountableProduct(
                "DAI_2", "BATCH_2", "Молоко", ProductType.DAIRY,
                85.0, currentDate, 7, 15
        );
        CountableProduct cheese = new CountableProduct(
                "DAI_3", "BATCH_3", "Сыр", ProductType.DAIRY,
                300.0, currentDate, 7, 5
        );

        warehouse.addProduct(milk1, currentDate);
        warehouse.addProduct(milk2, currentDate);
        warehouse.addProduct(cheese, currentDate);

        List<Product> milkProducts = warehouse.findProductsByName("Молоко");
        List<Product> cheeseProducts = warehouse.findProductsByName("Сыр");
        List<Product> breadProducts = warehouse.findProductsByName("Хлеб");

        assertEquals(2, milkProducts.size());
        assertEquals(1, cheeseProducts.size());
        assertTrue(breadProducts.isEmpty());

        assertEquals("Молоко", milkProducts.get(0).getName());
        assertEquals("Молоко", milkProducts.get(1).getName());
    }

    @Test
    void testFindProductsByNameAndType() {
        CountableProduct milk = new CountableProduct(
                "DAI_1", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );
        WeightableProduct potatoes = new WeightableProduct(
                "VEG_1", "BATCH_2", "Картофель", ProductType.VEGETABLES,
                40.0, currentDate, 10, 5.0
        );

        warehouse.addProduct(milk, currentDate);
        warehouse.addProduct(potatoes, currentDate);

        List<Product> milkProducts = warehouse.findProductsByNameAndType("Молоко", ProductType.DAIRY);
        List<Product> wrongTypeProducts = warehouse.findProductsByNameAndType("Молоко", ProductType.VEGETABLES);
        List<Product> nonExistentProducts = warehouse.findProductsByNameAndType("Несуществующий", ProductType.DAIRY);

        assertEquals(1, milkProducts.size());
        assertTrue(wrongTypeProducts.isEmpty());
        assertTrue(nonExistentProducts.isEmpty());
    }

    @Test
    void testFindProductsByNameCaseInsensitive() {
        warehouse.addProduct(countableProduct, currentDate);

        List<Product> lowerCase = warehouse.findProductsByName("молоко");
        List<Product> upperCase = warehouse.findProductsByName("МОЛОКО");
        List<Product> mixedCase = warehouse.findProductsByName("МоЛоКо");

        assertEquals(1, lowerCase.size());
        assertEquals(1, upperCase.size());
        assertEquals(1, mixedCase.size());
    }

    @Test
    void testRemoveProduct() {
        warehouse.addProduct(countableProduct, currentDate);
        assertEquals(1, warehouse.getTotalProducts());

        warehouse.removeProduct("DAI_123");

        assertNull(warehouse.getProduct("DAI_123"));
        assertEquals(0, warehouse.getTotalProducts());
    }

    @Test
    void testRemoveNonExistentProduct() {
        warehouse.addProduct(countableProduct, currentDate);
        assertEquals(1, warehouse.getTotalProducts());

        warehouse.removeProduct("NON_EXISTENT");

        assertEquals(1, warehouse.getTotalProducts());
    }

    @Test
    void testGetNonExistentProduct() {
        Product product = warehouse.getProduct("NON_EXISTENT");
        assertNull(product);
    }

    @Test
    void testEmptyWarehouseOnCreation() {
        assertEquals(0, warehouse.getTotalProducts());
        assertTrue(warehouse.getAllProducts().isEmpty());
        assertTrue(warehouse.getProductsCollection().isEmpty());
    }

    @Test
    void testFindProductsByNameInEmptyWarehouse() {
        List<Product> products = warehouse.findProductsByName("Молоко");
        assertTrue(products.isEmpty());
    }


    @Test
    void testTransferBetweenDifferentBatches() {
        CountableProduct milkBatch1 = new CountableProduct(
                "DAI_1", "BATCH_1", "Молоко", ProductType.DAIRY,
                80.0, currentDate, 7, 10
        );
        CountableProduct milkBatch2 = new CountableProduct(
                "DAI_2", "BATCH_2", "Молоко", ProductType.DAIRY,
                85.0, currentDate, 7, 15
        );

        warehouse.addProduct(milkBatch1, currentDate);
        warehouse.addProduct(milkBatch2, currentDate);

        Product transferred1 = warehouse.transferProduct("DAI_1", 5, currentDate);
        assertNotNull(transferred1);
        assertEquals("BATCH_1", ((CountableProduct) transferred1).getBatchId());

        Product transferred2 = warehouse.transferProduct("DAI_2", 10, currentDate);
        assertNotNull(transferred2);
        assertEquals("BATCH_2", ((CountableProduct) transferred2).getBatchId());

        assertEquals(5, ((CountableProduct) warehouse.getProduct("DAI_1")).getQuantity());
        assertEquals(5, ((CountableProduct) warehouse.getProduct("DAI_2")).getQuantity());
    }

    @Test
    void testMultipleTransfers() {
        warehouse.addProduct(countableProduct, currentDate);

        Product transferred1 = warehouse.transferProduct("DAI_123", 5, currentDate);
        assertNotNull(transferred1);
        assertEquals(5, ((CountableProduct) transferred1).getQuantity());
        assertEquals(15, ((CountableProduct) warehouse.getProduct("DAI_123")).getQuantity());

        Product transferred2 = warehouse.transferProduct("DAI_123", 10, currentDate);
        assertNotNull(transferred2);
        assertEquals(10, ((CountableProduct) transferred2).getQuantity());
        assertEquals(5, ((CountableProduct) warehouse.getProduct("DAI_123")).getQuantity());

        Product transferred3 = warehouse.transferProduct("DAI_123", 8, currentDate);
        assertNotNull(transferred3);
        assertEquals(5, ((CountableProduct) transferred3).getQuantity());
        assertNull(warehouse.getProduct("DAI_123"));
    }
}