package supermarket.storage;

import supermarket.product.Product;
import supermarket.product.ProductType;

import java.time.LocalDate;
import java.util.*;

public interface ProductStorage {
    boolean addProduct(Product product, LocalDate currentDate);

    List<Product> findProductsById(String productId);

    int removeExpiredProducts(LocalDate currentDate);

    Product getProduct(String productId);

    int getTotalProducts();
}