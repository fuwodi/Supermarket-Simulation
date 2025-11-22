package supermarket;

public class SupermarketConfig {

    public static final int LOW_STOCK_THRESHOLD = 5;
    public static final int REORDER_THRESHOLD = 2;
    public static final double WEIGHT_LOW_STOCK = 2.0;

    public static final double EXPIRING_DISCOUNT = 0.3;
    public static final double RANDOM_DISCOUNT_MIN = 0.1;
    public static final double RANDOM_DISCOUNT_MAX = 0.2;

    public static final int SHELF_LIFE_DAIRY = 7;
    public static final int SHELF_LIFE_BAKERY = 3;
    public static final int SHELF_LIFE_MEAT = 5;
    public static final int SHELF_LIFE_VEGETABLES = 10;
    public static final int SHELF_LIFE_GROCERIES = 30;
    public static final int SHELF_LIFE_CHEMICALS = 180;
    public static final int SHELF_LIFE_ALCOHOL = 365;

    public static final int DAYS_FOR_DISCOUNT = 2;

    public static final int SALES_HALL_MIN_COUNTABLE = 10;
    public static final int WAREHOUSE_MIN_COUNTABLE = 25;
    public static final int SALES_HALL_MAX_COUNTABLE = 50;
    public static final int WAREHOUSE_MAX_COUNTABLE = 100;

    public static final double SALES_HALL_MIN_WEIGHTABLE = 5.0;
    public static final double WAREHOUSE_MIN_WEIGHTABLE = 15.0;
    public static final double SALES_HALL_MAX_WEIGHTABLE = 20.0;
    public static final double WAREHOUSE_MAX_WEIGHTABLE = 50.0;

    public static final int RESTOCK_COUNTABLE_AMOUNT = 15;
    public static final double RESTOCK_WEIGHTABLE_AMOUNT = 8.0;
}