package supermarket.product;

import supermarket.SupermarketConfig;

public enum ProductType {
    DAIRY(SupermarketConfig.SHELF_LIFE_DAIRY),
    BAKERY(SupermarketConfig.SHELF_LIFE_BAKERY),
    MEAT(SupermarketConfig.SHELF_LIFE_MEAT),
    VEGETABLES(SupermarketConfig.SHELF_LIFE_VEGETABLES),
    GROCERIES(SupermarketConfig.SHELF_LIFE_GROCERIES),
    CHEMICALS(SupermarketConfig.SHELF_LIFE_CHEMICALS),
    ALCOHOL(SupermarketConfig.SHELF_LIFE_ALCOHOL);

    private final int shelfLifeDays;

    ProductType(int shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
    }

    public int getShelfLifeDays() {
        return shelfLifeDays;
    }
}