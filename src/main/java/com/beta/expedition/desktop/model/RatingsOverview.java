package com.beta.expedition.desktop.model;

import java.util.List;

public record RatingsOverview(RatingSummaryDto summary, List<RatingDto> ratings) {
}
