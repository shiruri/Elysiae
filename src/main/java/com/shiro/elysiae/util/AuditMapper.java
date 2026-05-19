package com.shiro.elysiae.util;

import com.shiro.elysiae.dto.response.audit.AuditResponse;
import com.shiro.elysiae.model.audit.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditResponse toAuditResponse(AuditLog audit);

}
