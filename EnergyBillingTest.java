import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnergyBillingTest {
    private static final String EXISTING_METER_ID = "METER-001";
    private static final String NEW_METER_ID = "METER-002";
    private EnergyBilling energyBilling;

    @BeforeEach
    void setUp() {
        EnergyBilling energyBilling = new EnergyBilling();
        // Додаємо тестовий лічильник до бази даних
        EnergyBilling.meterDatabase.put(EXISTING_METER_ID,
                new MeterData(EXISTING_METER_ID, 1000, 500));
    }

    @Test
    void testUpdateExistingMeter() {
        // Оновлення показників існуючого лічильника
        EnergyBilling.processNewReadings(EXISTING_METER_ID, 1050, 550);

        MeterData data = EnergyBilling.meterDatabase.get(EXISTING_METER_ID);
        assertEquals(1050, data.previousDay);
        assertEquals(550, data.previousNight);
        assertEquals((50 * EnergyBilling.DAY_TARIFF) + (50 * EnergyBilling.NIGHT_TARIFF), data.totalCost);
    }

    @Test
    void testNewMeter() {
        // Отримання показників від нового лічильника
        EnergyBilling.processNewReadings(NEW_METER_ID, 200, 100);

        MeterData data = EnergyBilling.meterDatabase.get(NEW_METER_ID);
        assertNotNull(data);
        assertEquals(200, data.previousDay);
        assertEquals(100, data.previousNight);
        assertEquals(0.0, data.totalCost); // Для нового лічильника вартість має бути 0
    }

    @Test
    void testLowerNightReadings() {
        // Отримання показників з заниженими нічними показниками
        int initialDay = EnergyBilling.meterDatabase.get(EXISTING_METER_ID).previousDay;
        int initialNight = EnergyBilling.meterDatabase.get(EXISTING_METER_ID).previousNight;

        EnergyBilling.processNewReadings(EXISTING_METER_ID, initialDay + 50, initialNight - 10);

        MeterData data = EnergyBilling.meterDatabase.get(EXISTING_METER_ID);
        assertEquals(initialDay + EnergyBilling.FAKE_DAY_KWH, data.previousDay);  // 1100
        assertEquals(initialNight + EnergyBilling.FAKE_NIGHT_KWH, data.previousNight);  // 580 (замість 530 + 80)
    }

    @Test
    void testLowerDayReadings() {
        int initialDay = EnergyBilling.meterDatabase.get(EXISTING_METER_ID).previousDay;
        int initialNight = EnergyBilling.meterDatabase.get(EXISTING_METER_ID).previousNight;

        energyBilling.processNewReadings(EXISTING_METER_ID, initialDay - 5, initialNight + 30);

        MeterData data = EnergyBilling.meterDatabase.get(EXISTING_METER_ID);
        assertEquals(initialDay + EnergyBilling.FAKE_DAY_KWH, data.previousDay); // 1000 + 100 = 1100
        assertEquals(initialNight + EnergyBilling.FAKE_NIGHT_KWH, data.previousNight); // 500 + 80 = 580
    }

    @Test
    void testLowerDayAndNightReadings() {
        // Отримання показників з заниженими денними та нічними показниками
        int initialDay = EnergyBilling.meterDatabase.get(EXISTING_METER_ID).previousDay;
        int initialNight = EnergyBilling.meterDatabase.get(EXISTING_METER_ID).previousNight;

        EnergyBilling.processNewReadings(EXISTING_METER_ID, initialDay - 20, initialNight - 15);

        MeterData data = EnergyBilling.meterDatabase.get(EXISTING_METER_ID);
        assertEquals(initialDay + EnergyBilling.FAKE_DAY_KWH, data.previousDay);
        assertEquals(initialNight + EnergyBilling.FAKE_NIGHT_KWH, data.previousNight);
    }

    @Test
    void testCostCalculation() {
        // Перевірка правильності розрахунку вартості
        EnergyBilling.processNewReadings(EXISTING_METER_ID, 1100, 600);

        MeterData data = EnergyBilling.meterDatabase.get(EXISTING_METER_ID);
        double expectedCost = (100 * EnergyBilling.DAY_TARIFF) + (100 * EnergyBilling.NIGHT_TARIFF);
        assertEquals(expectedCost, data.totalCost);
    }
}