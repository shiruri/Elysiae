package com.shiro.elysiae.dto.response.billing;

import java.util.List;

public record ServiceRateSummary
    (
            List<ServiceRateDetails> serviceRateDetails
    ) {
}
