import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class MeterData {
    String meterId;
    int previousDay;
    int previousNight;
    double totalCost;

    MeterData(String meterId, int previousDay, int previousNight) {
        this.meterId = meterId;
        this.previousDay = previousDay;
        this.previousNight = previousNight;
        this.totalCost = 0.0;
    }
}

public class EnergyBilling {
    // Константи тарифів та накрутки
    static final double DAY_TARIFF = 1.50;
    static final double NIGHT_TARIFF = 0.90;
    static final int FAKE_DAY_KWH = 100;
    static final int FAKE_NIGHT_KWH = 80;

    // База даних лічильників
    static final Map<String, MeterData> meterDatabase = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Система обліку електроенергії ===");

        // Додавання початкових лічильників
        System.out.println("Введіть кількість початкових лічильників:");
        int count = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < count; i++) {
            System.out.println("Введіть номер лічильника:");
            String meterId = scanner.nextLine();

            System.out.println("Введіть попередній показник (день):");
            int prevDay = Integer.parseInt(scanner.nextLine());

            System.out.println("Введіть попередній показник (ніч):");
            int prevNight = Integer.parseInt(scanner.nextLine());

            meterDatabase.put(meterId, new MeterData(meterId, prevDay, prevNight));
        }

        // Основний цикл для введення нових показників
        while (true) {
            System.out.println("\nВведіть номер лічильника або 'exit' для завершення:");
            String meterId = scanner.nextLine();
            if (meterId.equalsIgnoreCase("exit")) break;

            System.out.println("Введіть новий показник (день):");
            int newDay = Integer.parseInt(scanner.nextLine());

            System.out.println("Введіть новий показник (ніч):");
            int newNight = Integer.parseInt(scanner.nextLine());

            processNewReadings(meterId, newDay, newNight);
        }

        printMeterData();
        scanner.close();
    }

    static void processNewReadings(String meterId, int newDay, int newNight) {
        MeterData data = meterDatabase.getOrDefault(meterId, new MeterData(meterId, newDay, newNight));

        // Перевірка на зменшення показників
        if (newDay < data.previousDay || newNight < data.previousNight) {
            System.out.println("Попередження: Отримано менші показники для лічильника " + meterId + ", виконується накрутка!");
            newDay = data.previousDay + FAKE_DAY_KWH;
            newNight = data.previousNight + FAKE_NIGHT_KWH;
        }

        // Розрахунок спожитої електроенергії
        int consumedDay = newDay - data.previousDay;
        int consumedNight = newNight - data.previousNight;
        double cost = (consumedDay * DAY_TARIFF) + (consumedNight * NIGHT_TARIFF);

        // Оновлення даних у базі
        data.previousDay = newDay;
        data.previousNight = newNight;
        data.totalCost = cost;

        meterDatabase.put(meterId, data);
    }

    private static void printMeterData() {
        System.out.println("\n=== Оновлені рахунки за електроенергію ===");
        for (MeterData data : meterDatabase.values()) {
            System.out.println("Лічильник: " + data.meterId +
                    " | Вартість: " + String.format("%.2f", data.totalCost) + " грн");
        }
    }
}
