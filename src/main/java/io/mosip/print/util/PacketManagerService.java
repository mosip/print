package io.mosip.print.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.print.constant.ApiName;
import io.mosip.print.constant.LoggerFileConstant;
import io.mosip.print.dto.BiometricRequestDto;
import io.mosip.print.dto.BiometricType;
import io.mosip.print.dto.Document;
import io.mosip.print.dto.DocumentDto;
import io.mosip.print.dto.FieldDto;
import io.mosip.print.dto.FieldDtos;
import io.mosip.print.dto.FieldResponseDto;
import io.mosip.print.dto.InfoDto;
import io.mosip.print.dto.ValidatePacketResponse;
import io.mosip.print.exception.ApisResourceAccessException;
import io.mosip.print.exception.PacketManagerException;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.service.PrintRestClientService;

@Component
public class PacketManagerService {

	private static Logger regProcLogger = PrintLogger.getLogger(PacketManagerService.class);
    private static final String ID = "mosip.commmons.packetmanager";
    private static final String VERSION = "v1";

    @Autowired
	private PrintRestClientService<Object> restApi;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    private void setObjectMapper() {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }


    public String getField(String id, String field, String source, String process) throws ApisResourceAccessException, PacketManagerException, JsonProcessingException, IOException {
        FieldDto fieldDto = new FieldDto(id, field, source, process, false);

        RequestWrapper<FieldDto> request = new RequestWrapper<>();
        request.setId(ID);
        request.setVersion(VERSION);
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(fieldDto);
        ResponseWrapper<FieldResponseDto> response = (ResponseWrapper) restApi.postApi(ApiName.PACKETMANAGER_SEARCH_FIELD, "", "", request, ResponseWrapper.class);

        if (response.getErrors() != null && response.getErrors().size() > 0) {
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id, JsonUtils.javaObjectToJsonString(response));
            throw new PacketManagerException(response.getErrors().get(0).getErrorCode(), response.getErrors().get(0).getMessage());
        }

        FieldResponseDto fieldResponseDto = objectMapper.readValue(JsonUtils.javaObjectToJsonString(response.getResponse()), FieldResponseDto.class);

        return fieldResponseDto.getFields().get(field);
    }

    public Map<String, String> getFields(String id, List<String> fields, String source, String process) throws ApisResourceAccessException, PacketManagerException, JsonProcessingException, IOException {
        FieldDtos fieldDto = new FieldDtos(id, fields, source, process, false);

        RequestWrapper<FieldDtos> request = new RequestWrapper<>();
        request.setId(ID);
        request.setVersion(VERSION);
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(fieldDto);
        ResponseWrapper<FieldResponseDto> response = (ResponseWrapper) restApi.postApi(ApiName.PACKETMANAGER_SEARCH_FIELDS, "", "", request, ResponseWrapper.class);

        if (response.getErrors() != null && response.getErrors().size() > 0) {
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id, JsonUtils.javaObjectToJsonString(response));
            throw new PacketManagerException(response.getErrors().get(0).getErrorCode(), response.getErrors().get(0).getMessage());
        }

        FieldResponseDto fieldResponseDto = objectMapper.readValue(JsonUtils.javaObjectToJsonString(response.getResponse()), FieldResponseDto.class);

        return fieldResponseDto.getFields();
    }

    public Document getDocument(String id, String documentName, String source, String process) throws ApisResourceAccessException, PacketManagerException, JsonProcessingException, IOException {
        DocumentDto fieldDto = new DocumentDto(id, documentName, source, process);

        RequestWrapper<DocumentDto> request = new RequestWrapper<>();
        request.setId(ID);
        request.setVersion(VERSION);
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(fieldDto);
        ResponseWrapper<Document> response = (ResponseWrapper) restApi.postApi(ApiName.PACKETMANAGER_SEARCH_DOCUMENT, "", "", request, ResponseWrapper.class);

        if (response.getErrors() != null && response.getErrors().size() > 0) {
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id, JsonUtils.javaObjectToJsonString(response));
            throw new PacketManagerException(response.getErrors().get(0).getErrorCode(), response.getErrors().get(0).getMessage());
        }

        Document document = objectMapper.readValue(JsonUtils.javaObjectToJsonString(response.getResponse()), Document.class);

        return document;
    }

    public ValidatePacketResponse validate(String id, String source, String process) throws ApisResourceAccessException, PacketManagerException, JsonProcessingException, IOException {
        InfoDto fieldDto = new InfoDto(id, source, process, false);

        RequestWrapper<InfoDto> request = new RequestWrapper<>();
        request.setId(ID);
        request.setVersion(VERSION);
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(fieldDto);
        ResponseWrapper<ValidatePacketResponse> response = (ResponseWrapper) restApi.postApi(ApiName.PACKETMANAGER_VALIDATE, "", "", request, ResponseWrapper.class);

        if (response.getErrors() != null && response.getErrors().size() > 0) {
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id, JsonUtils.javaObjectToJsonString(response));
            throw new PacketManagerException(response.getErrors().get(0).getErrorCode(), response.getErrors().get(0).getMessage());
        }
        ValidatePacketResponse validatePacketResponse = objectMapper.readValue(JsonUtils.javaObjectToJsonString(response.getResponse()), ValidatePacketResponse.class);

        return validatePacketResponse;
    }

    public List<FieldResponseDto> getAudits(String id, String source, String process) throws ApisResourceAccessException, PacketManagerException, JsonProcessingException, IOException {
        InfoDto fieldDto = new InfoDto(id, source, process, false);
        List<FieldResponseDto> response = new ArrayList<>();

        RequestWrapper<InfoDto> request = new RequestWrapper<>();
        request.setId(ID);
        request.setVersion(VERSION);
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(fieldDto);
        ResponseWrapper<List<FieldResponseDto>> responseObj = (ResponseWrapper) restApi.postApi(ApiName.PACKETMANAGER_SEARCH_AUDITS, "", "", request, ResponseWrapper.class);

        if (responseObj.getErrors() != null && responseObj.getErrors().size() > 0) {
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id, JsonUtils.javaObjectToJsonString(responseObj));
            throw new PacketManagerException(responseObj.getErrors().get(0).getErrorCode(), responseObj.getErrors().get(0).getMessage());
        }

        for (Object o : responseObj.getResponse()) {
            FieldResponseDto fieldResponseDto = objectMapper.readValue(JsonUtils.javaObjectToJsonString(o), FieldResponseDto.class);
            response.add(fieldResponseDto);
        }

        return response;
    }

    public BiometricRecord getBiometrics(String id, String person, List<BiometricType> modalities, String source, String process) throws ApisResourceAccessException, PacketManagerException, JsonProcessingException, IOException {
        BiometricRequestDto fieldDto = new BiometricRequestDto(id, person, modalities, source, process, false);

        RequestWrapper<BiometricRequestDto> request = new RequestWrapper<>();
        request.setId(ID);
        request.setVersion(VERSION);
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(fieldDto);
        ResponseWrapper<BiometricRecord> response = (ResponseWrapper) restApi.postApi(ApiName.PACKETMANAGER_SEARCH_BIOMETRICS, "", "", request, ResponseWrapper.class);

        if (response.getErrors() != null && response.getErrors().size() > 0) {
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id, JsonUtils.javaObjectToJsonString(response));
            throw new PacketManagerException(response.getErrors().get(0).getErrorCode(), response.getErrors().get(0).getMessage());
        }
        if (response.getResponse() != null) {
            BiometricRecord biometricRecord = objectMapper.readValue(JsonUtils.javaObjectToJsonString(response.getResponse()), BiometricRecord.class);
            return biometricRecord;
        }
        return null;

    }

    public Map<String, String> getMetaInfo(String id, String source, String process) throws ApisResourceAccessException, PacketManagerException, JsonProcessingException, IOException {
        InfoDto fieldDto = new InfoDto(id, source, process, false);

        RequestWrapper<InfoDto> request = new RequestWrapper<>();
        request.setId(ID);
        request.setVersion(VERSION);
        request.setRequesttime(DateUtils.getUTCCurrentDateTime());
        request.setRequest(fieldDto);
        ResponseWrapper<FieldResponseDto> response = (ResponseWrapper) restApi.postApi(ApiName.PACKETMANAGER_SEARCH_METAINFO, "", "", request, ResponseWrapper.class);

        if (response.getErrors() != null && response.getErrors().size() > 0) {
            regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id, JsonUtils.javaObjectToJsonString(response));
            throw new PacketManagerException(response.getErrors().get(0).getErrorCode(), response.getErrors().get(0).getMessage());
        }

        FieldResponseDto fieldResponseDto = objectMapper.readValue(JsonUtils.javaObjectToJsonString(response.getResponse()), FieldResponseDto.class);

        return fieldResponseDto.getFields();
    }

}
