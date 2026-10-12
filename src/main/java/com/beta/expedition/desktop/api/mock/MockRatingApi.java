package com.beta.expedition.desktop.api.mock;

import com.beta.expedition.desktop.api.RatingApi;
import com.beta.expedition.desktop.model.ContractDto;
import com.beta.expedition.desktop.model.ContractKind;
import com.beta.expedition.desktop.model.ContractStatus;
import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.OrderStatus;
import com.beta.expedition.desktop.model.RatingDto;
import com.beta.expedition.desktop.model.RatingSummaryDto;
import com.beta.expedition.desktop.model.RatingsOverview;
import com.beta.expedition.desktop.model.Role;
import com.beta.expedition.desktop.model.UserDto;
import com.beta.expedition.desktop.model.request.RatingRequest;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

final class MockRatingApi implements RatingApi {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    private static final int MAX_COMMENT_LENGTH = 500;

    private final MockStore store;
    private final MockSession session;

    MockRatingApi(MockStore store, MockSession session) {
        this.store = store;
        this.session = session;
    }

    @Override
    public RatingDto rate(long orderId, RatingRequest request) {
        UserDto actor = session.require(Role.CUSTOMER, Role.FORWARDER, Role.ADMIN);
        OrderDto order = store.order(orderId);
        long toUserId;
        if (actor.role() == Role.CUSTOMER) {
            if (!order.customerId().equals(actor.id())) {
                throw MockErrors.notFound("заявка", orderId);
            }
            ContractDto contract = executedContract(ContractKind.CUSTOMER, orderId).orElseThrow(
                    () -> MockErrors.conflict("По заявке #" + orderId + " нет договора с экспедитором"));
            toUserId = contract.forwarderId();
        } else {
            ContractDto contract = executedContract(ContractKind.CARRIER, orderId).orElseThrow(
                    () -> MockErrors.conflict("По заявке #" + orderId + " нет договора с перевозчиком"));
            toUserId = contract.counterpartyId();
        }
        if (order.status() != OrderStatus.DELIVERED) {
            throw MockErrors.conflict("Оценить можно только доставленную заявку (сейчас «"
                    + order.status() + "»)");
        }
        if (request == null || request.score() < MIN_SCORE || request.score() > MAX_SCORE) {
            throw MockErrors.invalid("Оценка должна быть от " + MIN_SCORE + " до " + MAX_SCORE);
        }
        String comment = request.comment() == null || request.comment().isBlank() ? null
                : request.comment().trim();
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw MockErrors.invalid("Комментарий не длиннее " + MAX_COMMENT_LENGTH + " символов");
        }
        if (isRated(orderId, actor.id())) {
            throw MockErrors.conflict("Вы уже оценили заявку #" + orderId);
        }
        return store.addRating(orderId, actor.id(), toUserId, request.score(), comment, OffsetDateTime.now());
    }

    @Override
    public List<OrderDto> unratedOrders() {
        UserDto actor = session.require(Role.CUSTOMER, Role.FORWARDER, Role.ADMIN);
        return store.orders.values().stream()
                .filter(order -> order.status() == OrderStatus.DELIVERED)
                .filter(order -> actor.role() == Role.CUSTOMER
                        ? order.customerId().equals(actor.id())
                                && executedContract(ContractKind.CUSTOMER, order.id()).isPresent()
                        : executedContract(ContractKind.CARRIER, order.id()).isPresent())
                .filter(order -> !isRated(order.id(), actor.id()))
                .toList();
    }

    @Override
    public RatingsOverview mine() {
        UserDto actor = session.current();
        List<RatingDto> received = store.ratings.values().stream()
                .filter(r -> r.toUserId().equals(actor.id()))
                .sorted(Comparator.comparing(RatingDto::id).reversed())
                .toList();
        return new RatingsOverview(summary(received), received);
    }

    @Override
    public RatingSummaryDto overall() {
        session.current();
        return summary(List.copyOf(store.ratings.values()));
    }

    private boolean isRated(long orderId, long fromUserId) {
        return store.ratings.values().stream()
                .anyMatch(r -> r.orderId() == orderId && r.fromUserId() == fromUserId);
    }

    private Optional<ContractDto> executedContract(ContractKind kind, long orderId) {
        return store.contracts.get(kind).values().stream()
                .filter(c -> c.orderId() == orderId)
                .filter(c -> c.status() == ContractStatus.ACTIVE || c.status() == ContractStatus.COMPLETED)
                .findFirst();
    }

    private static RatingSummaryDto summary(List<RatingDto> ratings) {
        double average = ratings.stream().mapToInt(RatingDto::score).average().orElse(0);
        return new RatingSummaryDto(ratings.size(), average);
    }
}
