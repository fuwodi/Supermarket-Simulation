package supermarket.customer;

import java.util.*;

public class CustomerFactory {
    private static final String[] NAMES = {
            "Иван Иванов", "Мария Петрова", "Алексей Сидоров", "Елена Козлова",
            "Дмитрий Новиков", "Ольга Морозова", "Сергей Волков", "Наталья Лебедева",
            "Андрей Соловьев", "Татьяна Кузнецова", "Павел Васильев", "Юлия Павлова",
            "Анна Семенова", "Михаил Федоров", "Екатерина Никитина",
            "Артем Орлов", "Светлана Зайцева", "Владимир Медведев", "Ирина Егорова",
            "Константин Тихонов"
    };

    private static final Random random = new Random();

    private static final CustomerPreferences.PreferenceType[] PREFERENCE_TYPES = {
            CustomerPreferences.PreferenceType.FAMILY,
            CustomerPreferences.PreferenceType.FAMILY,
            CustomerPreferences.PreferenceType.BUDGET,
            CustomerPreferences.PreferenceType.BUDGET,
            CustomerPreferences.PreferenceType.HEALTHY,
            CustomerPreferences.PreferenceType.GOURMET,
            CustomerPreferences.PreferenceType.STUDENT,
            CustomerPreferences.PreferenceType.STUDENT
    };

    // Только создание пула покупателей
    public static List<Customer> createCustomerPool() {
        List<Customer> customers = new ArrayList<>();
        List<String> availableNames = new ArrayList<>(Arrays.asList(NAMES));
        Collections.shuffle(availableNames);

        for (int i = 0; i < 20; i++) {
            String name = availableNames.get(i % availableNames.size()) + " #" + (i+1);
            double baseBudget = 1200 + random.nextDouble() * 1800;

            CustomerPreferences.PreferenceType prefType = PREFERENCE_TYPES[
                    random.nextInt(PREFERENCE_TYPES.length)
                    ];

            CustomerPreferences preferences = new CustomerPreferences(prefType);
            Customer customer = new Customer("CUST_" + (i + 1), name, baseBudget, preferences);

            customers.add(customer);
        }

        return customers;
    }
}