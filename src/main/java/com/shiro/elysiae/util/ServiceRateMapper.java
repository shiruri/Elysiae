package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.billing.ServiceRateDetails;
import com.shiro.elysiae.dto.response.billing.ServiceRateSummary;
import com.shiro.elysiae.model.billing.ServiceRate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceRateMapper {

    ServiceRateDetails toDetails(ServiceRate serviceRate);
}
