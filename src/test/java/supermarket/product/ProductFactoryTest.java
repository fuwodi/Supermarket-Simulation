package supermarket.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ProductFactoryTest {

    @Test
    void testCreateSpecificCountableProduct() {
        Product product = ProductFactory.createSpecificProduct("Молоко", ProductType.DAIRY);

        assertNotNull(product);
        assertTrue(product instanceof CountableProduct);
        assertEquals("Молоко", product.getName());
        assertEquals(25, ((CountableProduct) product).getQuantity());
        assertEquals(ProductType.DAIRY, product.getType());
    }

    @Test
    void testCreateSpecificWeightableProduct() {
        Product product = ProductFactory.createSpecificProduct("Картофель", ProductType.VEGETABLES);

        assertNotNull(product);
        assertTrue(product instanceof WeightableProduct);
        assertEquals("Картофель", product.getName());
        assertEquals(8.0, ((WeightableProduct) product).getWeight(), 0.01);
        assertEquals(ProductType.VEGETABLES, product.getType());
    }

    @Test
    void testCreateProductWithNonExistingName() {
        Product product = ProductFactory.createSpecificProduct("Несуществующий товар", ProductType.DAIRY);

        assertNotNull(product);
        assertNotNull(product.getName());
        assertNotEquals("Несуществующий товар", product.getName());
    }

    @Test
    void testProductionDateIsSet() {
        Product product = ProductFactory.createRandomProduct(ProductType.BAKERY);

        assertNotNull(product.getProductionDate());
        assertFalse(product.getProductionDate().isAfter(LocalDate.now()));
    }

    @Test
    void testShelfLifeForDifferentTypes() {
        Product dairy = ProductFactory.createRandomProduct(ProductType.DAIRY);
        Product bakery = ProductFactory.createRandomProduct(ProductType.BAKERY);
        Product chemicals = ProductFactory.createRandomProduct(ProductType.CHEMICALS);

        assertEquals(ProductType.DAIRY.getShelfLifeDays(), dairy.getShelfLifeDays());
        assertEquals(ProductType.BAKERY.getShelfLifeDays(), bakery.getShelfLifeDays());
        assertEquals(ProductType.CHEMICALS.getShelfLifeDays(), chemicals.getShelfLifeDays());
    }

    @Test
    void testRandomProductBasicValidity() {
        Product product = ProductFactory.createRandomProduct(ProductType.DAIRY);

        assertNotNull(product);
        assertNotNull(product.getId());
        assertNotNull(product.getName());
        assertTrue(product.getPrice() > 0);
        assertTrue(product.getShelfLifeDays() > 0);
        assertNotNull(product.getProductionDate());
    }
}