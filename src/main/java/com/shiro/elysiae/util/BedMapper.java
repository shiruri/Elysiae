package com.shiro.elysiae.util;


import com.shiro.elysiae.dto.response.wardsandbeds.BedDetails;
import com.shiro.elysiae.dto.response.wardsandbeds.BedSummary;
import com.shiro.elysiae.model.wardsbedsadmission.Bed;
import com.shiro.elysiae.util.WardMapper;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {
                WardMapper.class
        }
)
public interface BedMapper {

    BedSummary toSummaryResponse(Bed bed);

    BedDetails toDetailsResponse(Bed bed);

}