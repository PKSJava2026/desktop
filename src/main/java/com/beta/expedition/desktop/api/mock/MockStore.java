package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.model.ChangeRequestDto;
import com.beta.expedition.desktop.model.ChangeRequestStatus;
import com.beta.expedition.desktop.model.ChangeRequestType;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.ContractStatus;
import com.beta.expedition.desktop.model.CooperationRequestDto;
import com.beta.expedition.desktop.model.CooperationStatus;
import com.beta.expedition.desktop.model.NotificationDto;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.RatingDto;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

final class MockStore {

    private static final Pattern NICKNAME = Pattern.compile("[\\p{L}\\p{N}_.-]{3,30}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_BYTES = 72;

    private final Map<Object, Long> sequences = new IdentityHashMap<>();

    final Map<Long, UserDto> users = new LinkedHashMap<>();
    final Map<Long, String> passwords = new HashMap<>();
    final Map<Long, OrderDto> orders = new LinkedHashMap<>();
    final Map<ContractKind, Map<Long, ContractDto>> contracts = new EnumMap<>(ContractKind.class);
    final Map<Long, Long> carrierContractCooperation = new HashMap<>();
    final Map<Long, ChangeRequestDto> changeRequests = new LinkedHashMap<>();
    final Map<Long, CooperationRequestDto> cooperationRequests = new LinkedHashMap<>();
    final Map<Long, List<NotificationDto>> notifications = new HashMap<>();
    final Map<Long, RatingDto> ratings = new LinkedHashMap<>();

    MockStore() {
        for (ContractKind kind : ContractKind.values()) {
            contracts.put(kind, new LinkedHashMap<>());
        }
    }

    static boolean canTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case NEW -> to == OrderStatus.CONTRACTED || to == OrderStatus.CANCELLED;
            case CONTRACTED -> to == OrderStatus.IN_TRANSIT || to == OrderStatus.CANCELLED;
            case IN_TRANSIT -> to == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    UserDto user(long id) {
        UserDto user = users.get(id);
        if (user == null) {
            throw MockErrors.notFound("пользователь", id);
        }
        return user;
    }

    String nickname(long id) {
        return user(id).nickname();
    }

    OrderDto order(long id) {
        OrderDto order = orders.get(id);
        if (order == null) {
            throw MockErrors.notFound("заявка", id);
        }
        return order;
    }

    ContractDto contract(ContractKind kind, long id) {
        ContractDto contract = contracts.get(kind).get(id);
        if (contract == null) {
            throw MockErrors.notFound("договор", id);
        }
        return contract;
    }

    CooperationRequestDto cooperationRequest(long id) {
        CooperationRequestDto request = cooperationRequests.get(id);
        if (request == null) {
            throw MockErrors.notFound("заявка о сотрудничестве", id);
        }
        return request;
    }

    UserDto registerUser(String nickname, String email, String password, Role role) {
        String cleanNickname = nickname == null ? "" : nickname.trim();
        String cleanEmail = email == null ? "" : email.trim();
        String cleanPassword = password == null ? "" : password;
        if (!NICKNAME.matcher(cleanNickname).matches()) {
            throw MockErrors.invalid("Никнейм: 3-30 символов (буквы, цифры, '_', '.', '-')");
        }
        if (cleanEmail.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(cleanEmail).matches()) {
            throw MockErrors.invalid("Некорректный email");
        }
        if (cleanPassword.length() < MIN_PASSWORD_LENGTH
                || cleanPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw MockErrors.invalid("Пароль: от " + MIN_PASSWORD_LENGTH + " символов, не длиннее "
                    + MAX_PASSWORD_BYTES + " байт");
        }
        if (findByNickname(cleanNickname) != null) {
            throw MockErrors.conflict("Никнейм '" + cleanNickname + "' уже занят");
        }
        boolean emailTaken = users.values().stream()
                .anyMatch(user -> user.email().equalsIgnoreCase(cleanEmail));
        if (emailTaken) {
            throw MockErrors.conflict("Email '" + cleanEmail + "' уже зарегистрирован");
        }
        return addUser(cleanNickname, cleanEmail, cleanPassword, role, OffsetDateTime.now());
    }

    UserDto addUser(String nickname, String email, String password, Role role, OffsetDateTime createdAt) {
        UserDto user = new UserDto(nextId(users), nickname, email, role, true, createdAt);
        users.put(user.id(), user);
        passwords.put(user.id(), password);
        return user;
    }

    UserDto findByNickname(String nickname) {
        String key = nickname.trim().toLowerCase(Locale.ROOT);
        return users.values().stream()
                .filter(user -> user.nickname().toLowerCase(Locale.ROOT).equals(key))
                .findFirst()
                .orElse(null);
    }

    OrderDto addOrder(long customerId, String cargo, BigDecimal weightKg, BigDecimal volumeM3, String origin,
                      String destination, LocalDate desiredDate, OrderStatus status, OffsetDateTime createdAt) {
        OrderDto order = new OrderDto(nextId(orders), customerId, nickname(customerId), cargo, weightKg, volumeM3,
                origin, destination, desiredDate, status, createdAt);
        orders.put(order.id(), order);
        return order;
    }

    ContractDto addContract(ContractKind kind, long orderId, long counterpartyId, long forwarderId,
                            BigDecimal price, String terms, LocalDate startDate, LocalDate endDate,
                            ContractStatus status, OffsetDateTime createdAt) {
        Map<Long, ContractDto> table = contracts.get(kind);
        ContractDto contract = new ContractDto(nextId(table), kind, orderId, counterpartyId,
                nickname(counterpartyId), forwarderId, nickname(forwarderId), price, terms, startDate, endDate,
                status, createdAt);
        table.put(contract.id(), contract);
        return contract;
    }

    ChangeRequestDto addChangeRequest(ContractKind kind, long contractId, ChangeRequestType type, long initiatedBy,
                                      BigDecimal price, String terms, LocalDate startDate, LocalDate endDate) {
        ChangeRequestDto request = new ChangeRequestDto(nextId(changeRequests), kind, contractId, type,
                initiatedBy, nickname(initiatedBy), price, terms, startDate, endDate,
                ChangeRequestStatus.PENDING, OffsetDateTime.now(), null);
        changeRequests.put(request.id(), request);
        return request;
    }

    CooperationRequestDto addCooperationRequest(long carrierId, String description, String vehicleInfo,
                                                CooperationStatus status, OffsetDateTime createdAt) {
        CooperationRequestDto request = new CooperationRequestDto(nextId(cooperationRequests), carrierId,
                nickname(carrierId), description, vehicleInfo, status, createdAt);
        cooperationRequests.put(request.id(), request);
        return request;
    }

    RatingDto addRating(long orderId, long fromUserId, long toUserId, int score, String comment,
                        OffsetDateTime createdAt) {
        RatingDto rating = new RatingDto(nextId(ratings), orderId, fromUserId, nickname(fromUserId), toUserId,
                score, comment, createdAt);
        ratings.put(rating.id(), rating);
        return rating;
    }

    void notify(long userId, String message, Long changeRequestId) {
        List<NotificationDto> list = notifications.computeIfAbsent(userId, id -> new ArrayList<>());
        long id = sequences.merge(notifications, 1L, Long::sum);
        list.add(new NotificationDto(id, message, changeRequestId, false, OffsetDateTime.now()));
    }

    void setOrderStatus(OrderDto order, OrderStatus next) {
        if (!canTransition(order.status(), next)) {
            throw MockErrors.conflict("Нельзя перевести заявку из статуса «" + order.status() + "» в «"
                    + next + "»");
        }
        orders.put(order.id(), MockCopies.withStatus(order, next));
    }

    private long nextId(Map<Long, ?> table) {
        return sequences.merge(table, 1L, Long::sum);
    }
}
