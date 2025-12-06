package supermarket;

import supermarket.customer.Customer;
import supermarket.customer.CustomerFactory;
import supermarket.event.Event;
import supermarket.event.EventQueue;
import supermarket.product.Product;
import supermarket.product.ProductFactory;
import supermarket.product.ProductType;
import supermarket.storage.ProductManager;
import supermarket.storage.SalesHall;
import supermarket.storage.Warehouse;

import java.time.LocalDate;
import java.util.*;

public class Supermarket {
    private Warehouse warehouse;
    private SalesHall salesHall;
    private EventQueue eventQueue;
    private ProductManager productManager;
    private List<Customer> customers;
    private LocalDate currentDate;
    private Random random;
    private int dayNumber;
    private double totalRevenue;
    private int dailyPurchasesCount;

    public Supermarket(LocalDate startDate) {
        this.warehouse = new Warehouse();
        this.salesHall = new SalesHall();
        this.eventQueue = new EventQueue(startDate);
        this.productManager = new ProductManager(warehouse, salesHall);
        this.customers = new ArrayList<>();
        this.currentDate = startDate;
        this.random = new Random();
        this.dayNumber = 1;
        this.totalRevenue = 0.0;
        this.dailyPurchasesCount = 0;

        initializeWithProducts();
        initializeCustomers();

        System.out.println("🔄 Первоначальное заполнение торгового зала...");
        productManager.transferProductsToHall();

        eventQueue.generateDailyEvents(3);
    }

    private void initializeWithProducts() {
        for (int i = 0; i < 15; i++) {
            ProductType randomType = ProductType.values()[random.nextInt(ProductType.values().length)];
            Product product = ProductFactory.createRandomProduct(randomType);
            boolean added = warehouse.addProduct(product, currentDate);
            if (added) {
                System.out.println("📦 На склад добавлен: " + product.getName());
            }
        }
        System.out.println("🏪 Магазин готов к работе!");
    }

    private void initializeCustomers() {
        this.customers = CustomerFactory.createCustomers(8);
        System.out.println("👥 Создано " + customers.size() + " покупателей");
    }

    public void runDay() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📅 День " + dayNumber + " (" + currentDate + ")");
        System.out.println("=".repeat(50));

        double dailyRevenue = 0.0;
        dailyPurchasesCount = 0;
        double revenueAtStart = totalRevenue;

        while (eventQueue.hasEvents()) {
            Event event = eventQueue.getNextEvent();
            processEvent(event);
        }

        dailyRevenue = totalRevenue - revenueAtStart;

        dayNumber++;
        currentDate = currentDate.plusDays(1);
        eventQueue.advanceDay();

        printDailySummary(dailyRevenue, dailyPurchasesCount);
    }

    private void processEvent(Event event) {
        System.out.println("\n⚡ " + event.getDescription());

        switch (event.getType()) {
            case DELIVERY:
                productManager.generateDelivery();
                break;
            case TRANSFER_TO_HALL:
                productManager.transferProductsToHall();
                break;
            case REMOVE_EXPIRED:
                removeExpiredProducts();
                break;
            case PURCHASE:
                if (handleCustomerPurchase()) {
                    dailyPurchasesCount++;
                }
                break;
            case SET_DISCOUNT:
                handleDiscounts();
                break;
            case CHECK_STOCK:
                productManager.checkAndRestockAll();
                break;
            case AUTO_RESTOCK:
                productManager.checkAndRestockAll();
                break;
        }
    }

    private void removeExpiredProducts() {
        int removedFromWarehouse = warehouse.removeExpiredProducts(currentDate);
        int removedFromHall = salesHall.removeExpiredProducts(currentDate);

        if (removedFromWarehouse > 0 || removedFromHall > 0) {
            System.out.println("🗑️ Утилизировано товаров: " + removedFromWarehouse + " со склада, " +
                    removedFromHall + " из зала");
        } else {
            System.out.println("✅ Просроченных товаров не обнаружено");
        }
    }

    private boolean handleCustomerPurchase() {
        if (customers.isEmpty()) return false;

        Customer customer = customers.get(random.nextInt(customers.size()));
        double purchaseAmount = customer.makePurchase(salesHall);

        if (purchaseAmount > 0) {
            totalRevenue += purchaseAmount;
            return true;
        }
        return false;
    }

    private void handleDiscounts() {
        int expiringDiscounts = salesHall.applyExpiringDiscounts(currentDate);

        if (random.nextDouble() < 0.3) {
            salesHall.applyRandomDiscounts();
        }

        if (expiringDiscounts > 0) {
            System.out.println("🏷️ Установлены скидки на " + expiringDiscounts + " товаров с истекающим сроком");
        }
    }

    private void printDailySummary(double dailyRevenue, int purchasesCount) {
        System.out.println("\n📊 Итоги дня:");
        System.out.println("   💰 Выручка за день: " + String.format("%.2f", dailyRevenue) + " руб.");
        System.out.println("   🛒 Совершено покупок: " + purchasesCount);
        System.out.println("   📦 Товаров на складе: " + warehouse.getTotalProducts() +
                " (" + warehouse.getTotalBatches() + " партий)" +
                (warehouse.needsRestocking() ? " ⚠️ МАЛО!" : " ✅"));
        System.out.println("   🏪 Товаров в зале: " + salesHall.getTotalProducts() +
                " (" + salesHall.getTotalBatches() + " партий)");
        System.out.println("   💵 Общая выручка: " + String.format("%.2f", totalRevenue) + " руб.");

        if (warehouse.needsRestocking()) {
            System.out.println("   🚨 ВНИМАНИЕ: Склад нуждается в срочном пополнении!");
        }
    }

    public void runSimulation(int days) {
        System.out.println("\n🎮 ЗАПУСК СИМУЛЯЦИИ НА " + days + " ДНЕЙ");

        for (int i = 0; i < days; i++) {
            runDay();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        printFinalSummary();
    }

    private void printFinalSummary() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🎯 ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ СИМУЛЯЦИИ");
        System.out.println("=".repeat(50));
        System.out.println("📅 Продолжительность: " + (dayNumber - 1) + " дней");
        System.out.println("💰 Общая выручка: " + String.format("%.2f", totalRevenue) + " руб.");
        System.out.println("📦 Остаток на складе: " + warehouse.getTotalProducts() + " товаров");
        System.out.println("🏪 Остаток в зале: " + salesHall.getTotalProducts() + " товаров");
    }

    public Warehouse getWarehouse() { return warehouse; }
    public SalesHall getSalesHall() { return salesHall; }
    public double getTotalRevenue() { return totalRevenue; }
    public int getDailyPurchasesCount() { return dailyPurchasesCount; }
}