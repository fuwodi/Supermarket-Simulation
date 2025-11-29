package supermarket.customer;

import java.util.*;

public class CustomerFactory {
    private static final String[] NAMES = {
            "Иван Иванов", "Мария Петрова", "Алексей Сидоров", "Елена Козлова",
            "Дмитрий Новиков", "Ольга Морозова", "Сергей Волков", "Наталья Лебедева",
            "Андрей Соловьев", "Татьяна Кузнецова", "Павел Васильев", "Юлия Павлова",
            "Анна Семенова", "Михаил Федоров", "Екатерина Никитина"
    };

    private static final Random random = new Random();

    public static List<Customer> createCustomers(int count) {
        List<Customer> customers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String name = NAMES[random.nextInt(NAMES.length)] + " " + (i + 1);
            double budget = 800 + random.nextDouble() * 1200;
            customers.add(new Customer("CUST_" + (i + 1), name, budget));
        }

        return customers;
    }
}