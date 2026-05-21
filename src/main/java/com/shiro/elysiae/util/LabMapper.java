package com.shiro.elysiae.util;
import com.shiro.elysiae.dto.response.laborotory.LabRequestDetails;
import com.shiro.elysiae.dto.response.laborotory.LabRequestSummary;
import com.shiro.elysiae.dto.response.laborotory.LabResultDetails;
import com.shiro.elysiae.model.laborotory.LabRequest;
import com.shiro.elysiae.model.laborotory.LabResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface LabMapper {

    @Mapping(target = "patientFullName", expression = "java(labRequest.getPatient().getFirstName() + \" \" + labRequest.getPatient().getLastName())")
    LabRequestSummary toSummary(LabRequest labRequest);

    @Mapping(target = "patientId",       source = "patient.id")
    @Mapping(target = "patientFullName", expression = "java(labRequest.getPatient().getFirstName() + \" \" + labRequest.getPatient().getLastName())")
    @Mapping(target = "doctorId",        source = "doctor.id")
    @Mapping(target = "doctorFullName",  expression = "java(\"Dr. \" + labRequest.getDoctor().getFirstName() + \" \" + labRequest.getDoctor().getLastName())")
    @Mapping(target = "result",          expression = "java(null)")
    LabRequestDetails toDetails(LabRequest labRequest);

    @Mapping(target = "id",              source = "labRequest.id")
    @Mapping(target = "patientId",       source = "labRequest.patient.id")
    @Mapping(target = "patientFullName", expression = "java(labRequest.getPatient().getFirstName() + \" \" + labRequest.getPatient().getLastName())")
    @Mapping(target = "doctorId",        source = "labRequest.doctor.id")
    @Mapping(target = "doctorFullName",  expression = "java(\"Dr. \" + labRequest.getDoctor().getFirstName() + \" \" + labRequest.getDoctor().getLastName())")
    @Mapping(target = "result",          source = "labResult")
    LabRequestDetails toDetails(LabRequest labRequest, LabResult labResult);

    @Mapping(target = "performedBy", source = "performedBy.username")
    LabResultDetails toResultDetails(LabResult labResult);

}