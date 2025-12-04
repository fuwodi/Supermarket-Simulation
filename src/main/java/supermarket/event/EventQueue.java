package supermarket.event;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class EventQueue {
    private final Queue<Event> events;
    private final Random random;
    private LocalDate currentDate;

    public EventQueue(LocalDate startDate) {
        this.events = new LinkedList<>();
        this.random = new Random();
        this.currentDate = startDate;
    }

    public void generateDailyEvents(int customerCount) {
        addEvent(new Event(EventType.REMOVE_EXPIRED, currentDate,
                "Ежедневная утилизация просроченных товаров"));

        addEvent(new Event(EventType.SET_DISCOUNT, currentDate,
                "Проверка скидок на товары с истекающим сроком"));

        if (random.nextDouble() < 0.7) {
            addEvent(new Event(EventType.DELIVERY, currentDate,
                    "Завоз новых товаров на склад"));
        }

        addEvent(new Event(EventType.TRANSFER_TO_HALL, currentDate,
                "Товаровед перемещает товары в торговый зал"));

        for (int i = 0; i < customerCount; i++) {
            addEvent(new Event(EventType.PURCHASE, currentDate,
                    "Покупка покупателя #" + (i + 1)));
        }

        addEvent(new Event(EventType.CHECK_STOCK, currentDate,
                "Товаровед проверяет остатки товаров"));

        addEvent(new Event(EventType.AUTO_RESTOCK, currentDate,
                "Автоматическое пополнение торгового зала"));
    }

    public void addRandomEvents() {
        if (random.nextDouble() < 0.25) {
            addEvent(new Event(EventType.SET_DISCOUNT, currentDate,
                    "Случайные акционные скидки"));
        }

        if (random.nextDouble() < 0.2) {
            addEvent(new Event(EventType.DELIVERY, currentDate,
                    "Специальный завоз товаров"));
        }
    }

    public void advanceDay() {
        currentDate = currentDate.plusDays(1);

        int customerCount = 2 + random.nextInt(5);
        generateDailyEvents(customerCount);
        addRandomEvents();
    }

    public void addEvent(Event event) {
        events.offer(event);
    }

    public Event getNextEvent() {
        return events.poll();
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }

    public int getQueueSize() {
        return events.size();
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }
}