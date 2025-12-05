package supermarket;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Запуск симуляции супермаркета...");

        Supermarket supermarket = new Supermarket(LocalDate.now());

        supermarket.runSimulation(8);

        System.out.println("\n✅ Симуляция завершена!");
    }
}