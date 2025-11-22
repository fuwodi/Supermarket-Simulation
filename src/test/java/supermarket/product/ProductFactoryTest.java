package supermarket.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ProductFactoryTest {

    @Test
    void testCreateCountableProductByName() {
        Product product = ProductFactory.createProductByName("Молоко");

        assertNotNull(product);
        assertTrue(product instanceof CountableProduct);
        assertEquals("Молоко", product.getName());
        assertEquals(25, ((CountableProduct) product).getQuantity());
        assertEquals(ProductType.DAIRY, product.getType());
        assertTrue(product.getPrice() > 0);
    }

    @Test
    void testCreateWeightableProductByName() {
        Product product = ProductFactory.createProductByName("Картофель");

        assertNotNull(product);
        assertTrue(product instanceof WeightableProduct);
        assertEquals("Картофель", product.getName());
        assertEquals(8.0, ((WeightableProduct) product).getWeight(), 0.01);
        assertEquals(ProductType.VEGETABLES, product.getType());
        assertTrue(product.getPrice() > 0);
    }

    @Test
    void testCreateProductWithNonExistingNameThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ProductFactory.createProductByName("Несуществующий товар");
        });

        assertEquals("Продукт не найден: Несуществующий товар", exception.getMessage());
    }

    @Test
    void testProductionDateIsSet() {
        Product product = ProductFactory.createProductByName("Хлеб белый");

        assertNotNull(product.getProductionDate());
        assertFalse(product.getProductionDate().isAfter(LocalDate.now()));
    }

    @Test
    void testShelfLifeForDifferentTypes() {
        Product dairy = ProductFactory.createProductByName("Молоко");
        Product bakery = ProductFactory.createProductByName("Хлеб белый");
        Product chemicals = ProductFactory.createProductByName("Мыло");

        assertEquals(ProductType.DAIRY.getShelfLifeDays(), dairy.getShelfLifeDays());
        assertEquals(ProductType.BAKERY.getShelfLifeDays(), bakery.getShelfLifeDays());
        assertEquals(ProductType.CHEMICALS.getShelfLifeDays(), chemicals.getShelfLifeDays());
    }

    @Test
    void testProductBasicValidity() {
        Product product = ProductFactory.createProductByName("Сыр");

        assertNotNull(product);
        assertNotNull(product.getId());
        assertNotNull(product.getName());
        assertTrue(product.getPrice() > 0);
        assertTrue(product.getShelfLifeDays() > 0);
        assertNotNull(product.getProductionDate());
        assertNotNull(product.getBatchId());
        assertTrue(product.getBatchId().startsWith("BATCH_"));
    }

    @Test
    void testCreateRandomProduct() {
        Product product = ProductFactory.createRandomProduct(ProductType.DAIRY);

        assertNotNull(product);
        assertNotNull(product.getId());
        assertNotNull(product.getName());
        assertTrue(product.getPrice() > 0);
        assertTrue(product.getShelfLifeDays() > 0);
        assertNotNull(product.getProductionDate());

        if (product instanceof CountableProduct) {
            int quantity = ((CountableProduct) product).getQuantity();
            assertTrue(quantity >= 20 && quantity <= 50);
        } else if (product instanceof WeightableProduct) {
            double weight = ((WeightableProduct) product).getWeight();
            assertTrue(weight >= 5.0 && weight <= 15.0);
        }
    }

    @Test
    void testProductIdGeneration() {
        Product product = ProductFactory.createProductByName("Йогурт");

        assertNotNull(product.getId());
        assertTrue(product.getId().startsWith("DAI_"));
        assertTrue(product.getId().contains("_"));
    }

    @Test
    void testDefaultDiscountIsZero() {
        Product product = ProductFactory.createProductByName("Молоко");

        assertEquals(0.0, product.getDiscount());
        assertEquals(product.getPrice(), product.getFinalPrice());
    }

    @Test
    void testCreatingProductsFromDifferentCategories() {
        Product dairy = ProductFactory.createProductByName("Молоко");
        Product meat = ProductFactory.createProductByName("Говядина");
        Product grocery = ProductFactory.createProductByName("Рис");
        Product alcohol = ProductFactory.createProductByName("Пиво");

        assertEquals(ProductType.DAIRY, dairy.getType());
        assertEquals(ProductType.MEAT, meat.getType());
        assertEquals(ProductType.GROCERIES, grocery.getType());
        assertEquals(ProductType.ALCOHOL, alcohol.getType());
    }
}