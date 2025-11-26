package supermarket;

public enum EventType {
    DELIVERY,           // Завоз товаров на склад
    TRANSFER_TO_HALL,   // Перемещение товаров в торговый зал
    REMOVE_EXPIRED,     // Утилизация просроченных товаров
    PURCHASE,           // Покупка товаров
    SET_DISCOUNT,       // Установка скидок
    CHECK_STOCK,        // Проверка запасов и пополнение
    AUTO_RESTOCK        // Автоматическое пополнение зала
}
