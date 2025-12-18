package supermarket.customer;

import java.util.*;

public class PredefinedCustomers {

    public static List<Customer> getPredefinedCustomers() {
        List<Customer> customers = new ArrayList<>();

        // 1. Бюджетные (всегда с картами)
        customers.add(new Customer("CUST001", "Анна Иванова",
                CustomerPreferences.PreferenceType.BUDGET,
                new DiscountCard("CARD-001")));
        customers.add(new Customer("CUST002", "Сергей Петров",
                CustomerPreferences.PreferenceType.BUDGET,
                new DiscountCard("CARD-002")));
        customers.add(new Customer("CUST003", "Ольга Смирнова",
                CustomerPreferences.PreferenceType.BUDGET,
                new DiscountCard("CARD-003")));

        // 2. Здоровое питание
        customers.add(new Customer("CUST004", "Иван Козлов",
                CustomerPreferences.PreferenceType.HEALTHY,
                new DiscountCard("CARD-004")));
        customers.add(new Customer("CUST005", "Елена Васнецова",
                CustomerPreferences.PreferenceType.HEALTHY,
                null));

        // 3. Семейные
        customers.add(new Customer("CUST006", "Дмитрий Орлов",
                CustomerPreferences.PreferenceType.FAMILY,
                null));
        customers.add(new Customer("CUST007", "Мария Сидорова",
                CustomerPreferences.PreferenceType.FAMILY,
                new DiscountCard("CARD-007")));

        // 4. Гурманы
        customers.add(new Customer("CUST008", "Александр Волков",
                CustomerPreferences.PreferenceType.GOURMET,
                new DiscountCard("CARD-008")));
        customers.add(new Customer("CUST009", "Виктория Зайцева",
                CustomerPreferences.PreferenceType.GOURMET,
                new DiscountCard("CARD-009")));

        // 5. Студенты
        customers.add(new Customer("CUST010", "Роман Морозов",
                CustomerPreferences.PreferenceType.STUDENT,
                new DiscountCard("CARD-010")));
        customers.add(new Customer("CUST011", "Татьяна Лебедева",
                CustomerPreferences.PreferenceType.STUDENT,
                null));

        // 6. Вегетарианцы (добавляем в CustomerPreferences.PreferenceType)
        customers.add(new Customer("CUST012", "Андрей Гусев",
                CustomerPreferences.PreferenceType.VEGETARIAN,
                new DiscountCard("CARD-012")));
        customers.add(new Customer("CUST013", "Ксения Воробьева",
                CustomerPreferences.PreferenceType.VEGETARIAN,
                null));

        // 7. Остальные
        customers.add(new Customer("CUST014", "Павел Соколов",
                CustomerPreferences.PreferenceType.FAMILY,
                new DiscountCard("CARD-014")));
        customers.add(new Customer("CUST015", "Юлия Попова",
                CustomerPreferences.PreferenceType.HEALTHY,
                null));
        customers.add(new Customer("CUST016", "Николай Федоров",
                CustomerPreferences.PreferenceType.BUDGET,
                new DiscountCard("CARD-016")));
        customers.add(new Customer("CUST017", "Екатерина Михайлова",
                CustomerPreferences.PreferenceType.FAMILY,
                null));
        customers.add(new Customer("CUST018", "Георгий Новиков",
                CustomerPreferences.PreferenceType.GOURMET,
                new DiscountCard("CARD-018")));
        customers.add(new Customer("CUST019", "София Козлова",
                CustomerPreferences.PreferenceType.VEGETARIAN,
                null));
        customers.add(new Customer("CUST020", "Артем Иванов",
                CustomerPreferences.PreferenceType.STUDENT,
                new DiscountCard("CARD-020")));

        System.out.println("\n👥 Создан пул из " + customers.size() + " покупателей:");
        for (Customer customer : customers) {
            System.out.println("   • " + customer.toString());
        }

        return customers;
    }
}