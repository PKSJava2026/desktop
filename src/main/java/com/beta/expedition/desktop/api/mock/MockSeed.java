package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.model.ChangeRequestType;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.ContractStatus;
import com.beta.expedition.desktop.model.CooperationRequestDto;
import com.beta.expedition.desktop.model.CooperationStatus;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.Role;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

final class MockSeed {

    private MockSeed() {
    }

    static void populate(MockStore store) {
        OffsetDateTime now = OffsetDateTime.now();
        LocalDate today = LocalDate.now();

        long admin = store.addUser("admin", "admin@expedition.local", "admin", Role.ADMIN, now.minusDays(90)).id();
        long forwarder = store.addUser("forwarder", "forwarder@expedition.local", "forwarder", Role.FORWARDER,
                now.minusDays(80)).id();
        store.addUser("forwarder2", "forwarder2@expedition.local", "forwarder2", Role.FORWARDER, now.minusDays(70));
        long customer = store.addUser("customer", "customer@expedition.local", "customer", Role.CUSTOMER,
                now.minusDays(60)).id();
        long customer2 = store.addUser("customer2", "customer2@expedition.local", "customer2", Role.CUSTOMER,
                now.minusDays(55)).id();
        long carrier = store.addUser("carrier", "carrier@expedition.local", "carrier", Role.CARRIER,
                now.minusDays(50)).id();
        long carrier2 = store.addUser("carrier2", "carrier2@expedition.local", "carrier2", Role.CARRIER,
                now.minusDays(45)).id();

        store.addOrder(customer, "Паллеты со строительной плиткой", decimal("1200"),
                decimal("4.5"), "Москва", "Казань", today.plusDays(10), OrderStatus.NEW, now.minusDays(2));
        OrderDto furniture = store.addOrder(customer, "Офисная мебель", decimal("800"), decimal("12"),
                "Санкт-Петербург", "Великий Новгород", today.plusDays(14), OrderStatus.NEW, now.minusDays(4));
        OrderDto appliances = store.addOrder(customer2, "Бытовая техника", decimal("650"), decimal("9"),
                "Екатеринбург", "Тюмень", today.plusDays(7), OrderStatus.CONTRACTED, now.minusDays(9));
        OrderDto equipment = store.addOrder(customer, "Оборудование для склада", decimal("2300"), decimal("18"),
                "Москва", "Нижний Новгород", today.plusDays(1), OrderStatus.IN_TRANSIT, now.minusDays(15));
        OrderDto food = store.addOrder(customer2, "Охлаждённые продукты", decimal("1500"), decimal("20"),
                "Краснодар", "Ростов-на-Дону", today.minusDays(5), OrderStatus.DELIVERED, now.minusDays(25));
        OrderDto books = store.addOrder(customer, "Книги и канцелярия", decimal("300"), decimal("3"),
                "Казань", "Самара", today.minusDays(12), OrderStatus.DELIVERED, now.minusDays(30));
        OrderDto machine = store.addOrder(customer, "Токарный станок", decimal("3000"), decimal("6"),
                "Москва", "Тула", today.minusDays(20), OrderStatus.CANCELLED, now.minusDays(40));
        OrderDto mixes = store.addOrder(customer2, "Строительные смеси", decimal("5000"), decimal("8"),
                "Тверь", "Москва", today.plusDays(5), OrderStatus.CONTRACTED, now.minusDays(6));

        store.addContract(ContractKind.CUSTOMER, furniture.id(), customer, forwarder, decimal("85000.00"),
                "Доставка до двери, разгрузка силами перевозчика", today.plusDays(14), today.plusDays(16),
                ContractStatus.PENDING, now.minusDays(1));
        ContractDto appliancesCustomer = store.addContract(ContractKind.CUSTOMER, appliances.id(), customer2,
                forwarder, decimal("54000.00"), "Страхование груза включено", today.plusDays(7),
                today.plusDays(9), ContractStatus.ACTIVE, now.minusDays(8));
        store.addContract(ContractKind.CUSTOMER, equipment.id(), customer, forwarder, decimal("148000.00"),
                "Сопровождение груза, подъём на склад", today.minusDays(3), today.plusDays(1),
                ContractStatus.ACTIVE, now.minusDays(14));
        store.addContract(ContractKind.CUSTOMER, food.id(), customer2, forwarder, decimal("96000.00"),
                "Температурный режим 2-6 °C", today.minusDays(9), today.minusDays(5), ContractStatus.ACTIVE,
                now.minusDays(24));
        store.addContract(ContractKind.CUSTOMER, books.id(), customer, forwarder, decimal("21500.00"), null,
                today.minusDays(15), today.minusDays(12), ContractStatus.ACTIVE, now.minusDays(29));
        store.addContract(ContractKind.CUSTOMER, machine.id(), customer, forwarder, decimal("240000.00"),
                "Перевозка негабаритного груза", today.minusDays(25), today.minusDays(20),
                ContractStatus.TERMINATED, now.minusDays(38));
        store.addContract(ContractKind.CUSTOMER, mixes.id(), customer2, forwarder, decimal("60000.00"),
                "Погрузка паллет краном", today.plusDays(5), today.plusDays(6), ContractStatus.ACTIVE,
                now.minusDays(5));

        store.addContract(ContractKind.CARRIER, appliances.id(), carrier, forwarder, decimal("38000.00"),
                "Тент, 10 т", today.plusDays(7), today.plusDays(9), ContractStatus.PENDING, now.minusDays(1));
        ContractDto equipmentCarrier = store.addContract(ContractKind.CARRIER, equipment.id(), carrier, forwarder,
                decimal("105000.00"), "Фура 20 т, два водителя", today.minusDays(3), today.plusDays(1),
                ContractStatus.ACTIVE, now.minusDays(12));
        store.addContract(ContractKind.CARRIER, food.id(), carrier2, forwarder, decimal("70000.00"),
                "Рефрижератор", today.minusDays(9), today.minusDays(5), ContractStatus.ACTIVE, now.minusDays(22));
        store.addContract(ContractKind.CARRIER, books.id(), carrier, forwarder, decimal("15000.00"), null,
                today.minusDays(15), today.minusDays(12), ContractStatus.ACTIVE, now.minusDays(27));

        store.addChangeRequest(ContractKind.CARRIER, equipmentCarrier.id(), ChangeRequestType.AMEND, carrier,
                decimal("112000.00"), "Добавлена платная дорога", null, null);
        store.addChangeRequest(ContractKind.CUSTOMER, appliancesCustomer.id(), ChangeRequestType.TERMINATE,
                customer2, null, null, null, null);

        CooperationRequestDto processed = store.addCooperationRequest(carrier, "Работаем 8 лет, свой парк, "
                + "опытные водители", "Фура 20 т, тент", CooperationStatus.PROCESSED, now.minusDays(48));
        store.addCooperationRequest(carrier2, "Рефрижераторные перевозки по ЮФО",
                "Рефрижератор 20 т, 3 единицы", CooperationStatus.NEW, now.minusDays(2));
        store.carrierContractCooperation.put(equipmentCarrier.id(), processed.id());

        store.addRating(food.id(), customer2, forwarder, 5, "Всё доставили вовремя, спасибо", now.minusDays(4));
        store.addRating(food.id(), forwarder, carrier2, 4, "Небольшая задержка при погрузке", now.minusDays(4));

        store.notify(customer, "Экспедитор forwarder предлагает договор #1 по заявке #" + furniture.id()
                + ", цена 85000.00. Подтвердите или отклоните его в разделе договоров", null);
        store.notify(carrier, "Экспедитор forwarder предлагает договор #1 по заявке #" + appliances.id()
                + ", цена 38000.00. Подтвердите или отклоните его в разделе договоров", null);
        store.notify(forwarder, "Перевозчик carrier предлагает изменить договор #" + equipmentCarrier.id()
                + " (запрос #1)", 1L);
        store.notify(forwarder, "Заказчик customer2 предлагает расторгнуть договор #" + appliancesCustomer.id()
                + " (запрос #2)", 2L);
        store.notify(admin, "Добавлена новая заявка о сотрудничестве от перевозчика carrier2", null);
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
