package supermarket;

import supermarket.customer.Customer;
import supermarket.customer.PredefinedCustomers;
import supermarket.event.Event;
import supermarket.event.EventQueue;
import supermarket.product.Product;
import supermarket.product.ProductFactory;
import supermarket.product.ProductType;
import supermarket.storage.ProductManager;
import supermarket.storage.SalesHall;
import supermarket.storage.Shelf;
import supermarket.storage.Warehouse;

import java.time.LocalDate;
import java.util.*;

public class Supermarket {
    private Warehouse warehouse;
    private SalesHall salesHall;
    private EventQueue eventQueue;
    private ProductManager productManager;
    private List<Customer> customerPool; // Пул постоянных покупателей
    private List<Customer> dailyCustomers; // Покупатели на сегодня
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

        // Используем готовый список покупателей вместо фабрики
        this.customerPool = PredefinedCustomers.getPredefinedCustomers();

        this.dailyCustomers = new ArrayList<>();
        this.currentDate = startDate;
        this.random = new Random();
        this.dayNumber = 1;
        this.totalRevenue = 0.0;
        this.dailyPurchasesCount = 0;

        initializeWithProducts();
        selectDailyCustomers();   // Выбираем покупателей на первый день

        System.out.println("\n🔄 Первоначальное заполнение торгового зала...");
        productManager.transferProductsToHall();

        // Генерируем события на первый день
        eventQueue.generateDailyEvents(dailyCustomers.size());
    }

    private void initializeWithProducts() {
        System.out.println("\n📦 Заполнение склада начальными товарами...");
        for (int i = 0; i < 20; i++) {
            ProductType randomType = ProductType.values()[random.nextInt(ProductType.values().length)];
            Product product = ProductFactory.createRandomProduct(randomType);
            boolean added = warehouse.addProduct(product, currentDate);
            if (added) {
                System.out.println("   📦 " + product.getName());
            }
        }
        System.out.println("🏪 Склад готов к работе!");
    }

    // Выбираем покупателей на день
    private void selectDailyCustomers() {
        this.dailyCustomers.clear();

        // Случайно выбираем 2-4 покупателя из пула
        List<Customer> available = new ArrayList<>(customerPool);
        Collections.shuffle(available);

        int count = 2 + random.nextInt(3); // 2, 3 или 4
        count = Math.min(count, available.size());

        for (int i = 0; i < count; i++) {
            dailyCustomers.add(available.get(i));
        }
    }

    // Восстанавливаем бюджет всем покупателям в пуле
    private void restoreBudgets() {
        for (Customer customer : customerPool) {
            customer.restoreBudget(); // используем новый метод
        }
    }

    public void runDay() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📅 День " + dayNumber + " (" + currentDate + ")");
        System.out.println("=".repeat(60));

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

        restoreBudgets();
        selectDailyCustomers();
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
                handleCustomerPurchase();
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

    private void handleCustomerPurchase() {
        if (dailyCustomers.isEmpty()) return;

        // Сначала показываем товары со скидками
        System.out.println("\n" + "=".repeat(60));
        displayProductsWithDiscounts();

        Customer customer = dailyCustomers.get(random.nextInt(dailyCustomers.size()));

        System.out.println("\n" + "=".repeat(50));
        System.out.println("🛒 ПОКУПАТЕЛЬ ЗАХОДИТ В МАГАЗИН");

        double purchaseAmount = customer.makePurchase(salesHall);

        if (purchaseAmount > 0) {
            totalRevenue += purchaseAmount;
            dailyPurchasesCount++;
        }
    }

    // Новый метод для отображения товаров со скидками
    private void displayProductsWithDiscounts() {
        System.out.println("\n🏪 ТОВАРЫ В ЗАЛЕ (🎫 = скидка для владельцев карт):");
        System.out.println("-".repeat(60));

        boolean hasDiscountedProducts = false;
        Map<String, Shelf> shelves = salesHall.getAllShelves();

        for (Map.Entry<String, Shelf> entry : shelves.entrySet()) {
            Shelf shelf = entry.getValue();
            String productId = entry.getKey();
            String productName = salesHall.getProductName(productId);

            if (productName == null) continue;

            double totalAmount = shelf.getCurrentAmount();
            List<supermarket.product.Product> batches = shelf.getAllBatches();

            if (batches.isEmpty()) continue;

            supermarket.product.Product sampleProduct = batches.get(0);
            System.out.print(String.format("   %-25s", productName));

            if (sampleProduct instanceof supermarket.product.CountableProduct) {
                System.out.print(String.format(" %3.0f шт.", totalAmount));
            } else {
                System.out.print(String.format(" %5.1f кг", totalAmount));
            }

            System.out.print(String.format(" | %7.2f руб.", sampleProduct.getPrice()));

            if (sampleProduct.getDiscount() > 0) {
                hasDiscountedProducts = true;
                System.out.print(String.format(" → %7.2f руб.", sampleProduct.getFinalPrice()));
                System.out.print(" 🎫 -" + (int)(sampleProduct.getDiscount() * 100) + "%");
            }
            System.out.println();
        }

        if (hasDiscountedProducts) {
            System.out.println("   ⚠️  Скидки доступны только для владельцев карт!");
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

    private void handleDiscounts() {
        int expiringDiscounts = salesHall.applyExpiringDiscounts(currentDate);

        if (random.nextDouble() < 0.3) {
            salesHall.applyRandomDiscounts();
        }

        if (expiringDiscounts > 0) {
            System.out.println("   🏷️ Установлены скидки на " + expiringDiscounts + " товаров с истекающим сроком");
        }
    }

    private void printDailySummary(double dailyRevenue, int purchasesCount) {
        System.out.println("\n📊 ИТОГИ ДНЯ:");
        System.out.println("=".repeat(50));

        System.out.println(String.format("💰 Выручка: %s руб. | 🛒 Покупок: %d",
                String.format("%.2f", dailyRevenue), purchasesCount));

        System.out.println(String.format("📦 Склад: %d товаров, %d партий%s",
                warehouse.getTotalProducts(), warehouse.getTotalBatches(),
                warehouse.needsRestocking() ? " ⚠️" : ""));

        System.out.println(String.format("🏪 Зал: %d полок, %d партий",
                salesHall.getTotalProducts(), salesHall.getTotalBatches()));

        System.out.println(String.format("💵 Общая выручка: %s руб.",
                String.format("%.2f", totalRevenue)));

        // Показываем критические полки
        System.out.println("\n📊 ЗАПОЛНЕННОСТЬ ПОЛОК:");
        salesHall.displayCriticalShelves();

        if (warehouse.needsRestocking()) {
            System.out.println("\n🚨 ВНИМАНИЕ: Склад нуждается в пополнении!");
        }
    }

    public void runSimulation(int days) {
        System.out.println("\n🎮 ЗАПУСК СИМУЛЯЦИИ НА " + days + " ДНЕЙ");
        System.out.println("=".repeat(50));

        for (int i = 0; i < days; i++) {
            runDay();

            try {
                Thread.sleep(1000); // Пауза между днями
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        printFinalSummary();
    }

    private void printFinalSummary() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🎯 ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ (" + (dayNumber - 1) + " дней)");
        System.out.println("=".repeat(50));

        System.out.println(String.format("💰 Общая выручка: %s руб.",
                String.format("%.2f", totalRevenue)));

        System.out.println(String.format("📦 Склад: %d товаров, %d партий",
                warehouse.getTotalProducts(), warehouse.getTotalBatches()));

        System.out.println(String.format("🏪 Зал: %d полок, %d партий",
                salesHall.getTotalProducts(), salesHall.getTotalBatches()));

        // Информация о покупателях
        System.out.println("\n👥 СТАТИСТИКА ПОКУПАТЕЛЕЙ:");
        System.out.println("• Всего постоянных покупателей: " + customerPool.size());
    }

    public Warehouse getWarehouse() { return warehouse; }
    public SalesHall getSalesHall() { return salesHall; }
    public double getTotalRevenue() { return totalRevenue; }
    public int getDailyPurchasesCount() { return dailyPurchasesCount; }
    public List<Customer> getCustomerPool() { return new ArrayList<>(customerPool); }
    public List<Customer> getDailyCustomers() { return new ArrayList<>(dailyCustomers); }
}