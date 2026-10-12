package com.beta.expedition.desktop.api;

import com.beta.expedition.desktop.model.OrderDto;
import com.beta.expedition.desktop.model.RatingDto;
import com.beta.expedition.desktop.model.RatingSummaryDto;
import com.beta.expedition.desktop.model.RatingsOverview;
import com.beta.expedition.desktop.model.request.RatingRequest;
import java.util.List;

public interface RatingApi {

    RatingDto rate(long orderId, RatingRequest request);

    List<OrderDto> unratedOrders();

    RatingsOverview mine();

    RatingSummaryDto overall();
}
