package supermarket;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Запуск супермаркета...");

        Supermarket supermarket = new Supermarket(LocalDate.now());

        System.out.println("✅ Супермаркет успешно создан!");
        System.out.println("📅 Дата: " + LocalDate.now());


        System.out.println("\n🎉 Программа работает!");
    }
}