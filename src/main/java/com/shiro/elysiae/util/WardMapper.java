package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.wardsandbeds.WardsDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.WardsSummary;
import com.shiro.elysiae.model.wardsbedsadmission.Ward;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WardMapper {

    WardsSummary toSummaryResponse(Ward ward);

    WardsDetails toDetailsResponse(Ward ward);

}