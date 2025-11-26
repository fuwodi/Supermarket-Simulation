package supermarket.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ProductFactoryTest {

    @Test
    void testCreateCountableProductById() {
        Product product = ProductFactory.createProductById("MILK");

        assertNotNull(product);
        assertTrue(product instanceof CountableProduct);
        assertEquals("Молоко", product.getName());
        assertEquals(25, ((CountableProduct) product).getQuantity());
        assertEquals(ProductType.DAIRY, product.getType());
        assertTrue(product.getPrice() > 0);
    }

    @Test
    void testCreateWeightableProductById() {
        Product product = ProductFactory.createProductById("POTATO");

        assertNotNull(product);
        assertTrue(product instanceof WeightableProduct);
        assertEquals("Картофель", product.getName());
        assertEquals(8.0, ((WeightableProduct) product).getWeight(), 0.01);
        assertEquals(ProductType.VEGETABLES, product.getType());
        assertTrue(product.getPrice() > 0);
    }

    @Test
    void testCreateProductWithNonExistingIdThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ProductFactory.createProductById("NON_EXISTING_ID");
        });

        assertEquals("Продукт не найден по ID: NON_EXISTING_ID", exception.getMessage());
    }

    @Test
    void testProductionDateIsSet() {
        Product product = ProductFactory.createProductById("WHITE_BREAD");

        assertNotNull(product.getProductionDate());
        assertFalse(product.getProductionDate().isAfter(LocalDate.now()));
    }

    @Test
    void testShelfLifeForDifferentTypes() {
        Product dairy = ProductFactory.createProductById("MILK");
        Product bakery = ProductFactory.createProductById("WHITE_BREAD");
        Product chemicals = ProductFactory.createProductById("SOAP");

        assertEquals(ProductType.DAIRY.getShelfLifeDays(), dairy.getShelfLifeDays());
        assertEquals(ProductType.BAKERY.getShelfLifeDays(), bakery.getShelfLifeDays());
        assertEquals(ProductType.CHEMICALS.getShelfLifeDays(), chemicals.getShelfLifeDays());
    }

    @Test
    void testProductBasicValidity() {
        Product product = ProductFactory.createProductById("CHEESE");

        assertNotNull(product);
        assertNotNull(product.getId());
        assertEquals("CHEESE", product.getId());
        assertNotNull(product.getName());
        assertTrue(product.getPrice() > 0);
        assertTrue(product.getShelfLifeDays() > 0);
        assertNotNull(product.getProductionDate());
        assertNotNull(product.getBatchId());
        assertTrue(product.getBatchId().startsWith("BATCH_"));
    }

    @Test
    void testCreateRandomProduct() {
        Product dairyProduct = ProductFactory.createRandomProduct(ProductType.DAIRY);

        assertNotNull(dairyProduct);
        assertNotNull(dairyProduct.getId());
        assertTrue(dairyProduct.getId().matches("MILK|YOGURT|SOUR_CREAM|COTTAGE_CHEESE|CHEESE|KEFIR|RYAZHENKA|CREAM"));
        assertNotNull(dairyProduct.getName());
        assertTrue(dairyProduct.getPrice() > 0);
        assertTrue(dairyProduct.getShelfLifeDays() > 0);
        assertNotNull(dairyProduct.getProductionDate());

        assertTrue(dairyProduct instanceof CountableProduct);
        int quantity = ((CountableProduct) dairyProduct).getQuantity();
        assertTrue(quantity >= 20 && quantity <= 50);

        Product vegetableProduct = ProductFactory.createRandomProduct(ProductType.VEGETABLES);

        assertNotNull(vegetableProduct);
        assertNotNull(vegetableProduct.getId());
        assertTrue(vegetableProduct.getId().matches("POTATO|CARROT|TOMATO|CUCUMBER|APPLE|BANANA|ORANGE|ONION"));
        assertNotNull(vegetableProduct.getName());
        assertTrue(vegetableProduct.getPrice() > 0);
        assertTrue(vegetableProduct.getShelfLifeDays() > 0);
        assertNotNull(vegetableProduct.getProductionDate());

        assertTrue(vegetableProduct instanceof WeightableProduct);
        double weight = ((WeightableProduct) vegetableProduct).getWeight();
        assertTrue(weight >= 5.0 && weight <= 15.0);

        assertEquals(ProductType.DAIRY, dairyProduct.getType());
        assertEquals(ProductType.VEGETABLES, vegetableProduct.getType());
        assertNotEquals(dairyProduct.getType(), vegetableProduct.getType());

        assertNotEquals(dairyProduct.getBatchId(), vegetableProduct.getBatchId());
    }

    @Test
    void testProductIdIsConstant() {
        Product product1 = ProductFactory.createProductById("YOGURT");
        Product product2 = ProductFactory.createProductById("YOGURT");

        assertEquals("YOGURT", product1.getId());
        assertEquals("YOGURT", product2.getId());
        assertEquals(product1.getId(), product2.getId());

        assertNotEquals(product1.getBatchId(), product2.getBatchId());
    }

    @Test
    void testDefaultDiscountIsZero() {
        Product product = ProductFactory.createProductById("MILK");

        assertEquals(0.0, product.getDiscount());
        assertEquals(product.getPrice(), product.getFinalPrice());
    }

    @Test
    void testCreatingProductsFromDifferentCategories() {
        Product dairy = ProductFactory.createProductById("MILK");
        Product meat = ProductFactory.createProductById("BEEF");
        Product grocery = ProductFactory.createProductById("RICE");
        Product alcohol = ProductFactory.createProductById("BEER");

        assertEquals(ProductType.DAIRY, dairy.getType());
        assertEquals(ProductType.MEAT, meat.getType());
        assertEquals(ProductType.GROCERIES, grocery.getType());
        assertEquals(ProductType.ALCOHOL, alcohol.getType());
    }

    @Test
    void testBatchIdGeneration() {
        Product product1 = ProductFactory.createProductById("MILK");
        Product product2 = ProductFactory.createProductById("MILK");

        assertNotNull(product1.getBatchId());
        assertNotNull(product2.getBatchId());
        assertTrue(product1.getBatchId().startsWith("BATCH_"));
        assertTrue(product2.getBatchId().startsWith("BATCH_"));

        assertNotEquals(product1.getBatchId(), product2.getBatchId());
    }
}