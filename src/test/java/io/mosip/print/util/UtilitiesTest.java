package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.print.constant.ApiName;
import io.mosip.print.dto.ErrorDTO;
import io.mosip.print.exception.ExceptionUtils;
import io.mosip.print.exception.IdRepoAppException;
import io.mosip.print.idrepo.dto.IdResponseDTO1;
import io.mosip.print.idrepo.dto.ResponseDTO;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.service.PrintRestClientService;

/**
 * Unit tests for {@link Utilities} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the Utilities class,
 * including JSON retrieval from external sources, identity mapping operations, IdRepo data access,
 * UIN retrieval, configuration file processing, and various exception handling scenarios for utility operations.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
class UtilitiesTest {

    @Mock
    private ObjectMapper objMapper;

    @Mock
    private RestApiClient restApiClient;

    @Mock
    private PrintRestClientService<Object> restClientService;

    @InjectMocks
    private Utilities utilities;

    private static final String CONFIG_SERVER_URL = "http://config-server/";
    private static final String URI_PATH = "/identity/mapping.json";
    private static final String JSON_RESPONSE = "{\"identity\":{\"name\":{\"value\":\"fullName\"}}}";
    private static final String UIN = "1234567890";
    private static final String REG_ID = "REG123456789";

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes the Utilities instance with configuration URLs and file paths
     * required for JSON processing, identity mapping, and external service communication.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(utilities, "configServerFileStorageURL", CONFIG_SERVER_URL);
        ReflectionTestUtils.setField(utilities, "getRegProcessorIdentityJson", "/identity.json");
        ReflectionTestUtils.setField(utilities, "getRegProcessorDemographicIdentity", "/demographic.json");
        ReflectionTestUtils.setField(utilities, "registrationProcessorPrintTextFile", "/printtext.json");
    }

    /**
     * Tests successful JSON retrieval from external API.
     * Verifies that the method correctly retrieves JSON data from a given URL
     * and returns the expected JSON response content.
     */
    @Test
    void getJsonShouldSucceedWithValidUrlAndPath() throws Exception {
        when(restApiClient.getApi(any(URI.class), eq(String.class))).thenReturn(JSON_RESPONSE);

        String result = utilities.getJson(CONFIG_SERVER_URL, URI_PATH);

        assertNotNull(result);
        assertEquals(JSON_RESPONSE, result);
    }

    /**
     * Tests JSON retrieval when an exception occurs during API call.
     * Verifies that the method handles exceptions gracefully and returns null
     * when the external API call fails.
     */
    @Test
    void getJsonWithExceptionShouldReturnNull() throws Exception {
        try (MockedStatic<ExceptionUtils> exceptionUtilsMock = mockStatic(ExceptionUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            printLoggerMock.when(() -> PrintLogger.getLogger(Utilities.class))
                    .thenReturn(mock(org.slf4j.Logger.class));
            exceptionUtilsMock.when(() -> ExceptionUtils.getStackTrace(any(Exception.class)))
                    .thenReturn("Stack trace");

            when(restApiClient.getApi(any(URI.class), eq(String.class)))
                    .thenThrow(new RuntimeException("API Error"));

            String result = utilities.getJson(CONFIG_SERVER_URL, URI_PATH);

            assertNull(result);
        }
    }

    /**
     * Tests getPrintTextFileJson method when cached value already exists.
     * Verifies that the method returns the cached JSON string without making
     * an external API call when data is already available.
     */
    @Test
    void getPrintTextFileJsonWithExistingValueShouldReturnCachedData() throws Exception {
        ReflectionTestUtils.setField(utilities, "printTextFileJsonString", JSON_RESPONSE);

        String result = utilities.getPrintTextFileJson(CONFIG_SERVER_URL, URI_PATH);

        assertEquals(JSON_RESPONSE, result);
    }

    /**
     * Tests getPrintTextFileJson method when no cached value exists.
     * Verifies that the method fetches JSON data from external API when
     * no cached data is available and returns the retrieved content.
     */
    @Test
    void getPrintTextFileJsonWithoutExistingValueShouldFetchFromApi() throws Exception {
        ReflectionTestUtils.setField(utilities, "printTextFileJsonString", null);
        when(restApiClient.getApi(any(URI.class), eq(String.class))).thenReturn(JSON_RESPONSE);

        String result = utilities.getPrintTextFileJson(CONFIG_SERVER_URL, URI_PATH);

        assertEquals(JSON_RESPONSE, result);
    }

    /**
     * Tests getPrintTextFileJson method when cached value is empty.
     * Verifies that the method fetches fresh JSON data from external API
     * when the cached value is an empty string.
     */
    @Test
    void getPrintTextFileJsonWithEmptyExistingValueShouldFetchFromApi() throws Exception {
        ReflectionTestUtils.setField(utilities, "printTextFileJsonString", "");
        when(restApiClient.getApi(any(URI.class), eq(String.class))).thenReturn(JSON_RESPONSE);

        String result = utilities.getPrintTextFileJson(CONFIG_SERVER_URL, URI_PATH);

        assertEquals(JSON_RESPONSE, result);
    }

    /**
     * Tests getIdentityMappingJson method when cached value already exists.
     * Verifies that the method returns the cached identity mapping JSON string
     * without making an external API call when data is already available.
     */
    @Test
    void getIdentityMappingJsonWithExistingValueShouldReturnCachedData() throws Exception {
        ReflectionTestUtils.setField(utilities, "identityMappingJsonString", JSON_RESPONSE);

        String result = utilities.getIdentityMappingJson(CONFIG_SERVER_URL, URI_PATH);

        assertEquals(JSON_RESPONSE, result);
    }

    /**
     * Tests getIdentityMappingJson method when no cached value exists.
     * Verifies that the method fetches identity mapping JSON data from external API
     * when no cached data is available and returns the retrieved content.
     */
    @Test
    void getIdentityMappingJsonWithoutExistingValueShouldFetchFromApi() throws Exception {
        ReflectionTestUtils.setField(utilities, "identityMappingJsonString", null);
        when(restApiClient.getApi(any(URI.class), eq(String.class))).thenReturn(JSON_RESPONSE);

        String result = utilities.getIdentityMappingJson(CONFIG_SERVER_URL, URI_PATH);

        assertEquals(JSON_RESPONSE, result);
    }

    /**
     * Tests getIdentityMappingJson method when cached value is empty.
     * Verifies that the method fetches fresh identity mapping JSON data from external API
     * when the cached value is an empty string.
     */
    @Test
    void getIdentityMappingJsonWithEmptyExistingValueShouldFetchFromApi() throws Exception {
        ReflectionTestUtils.setField(utilities, "identityMappingJsonString", "");
        when(restApiClient.getApi(any(URI.class), eq(String.class))).thenReturn(JSON_RESPONSE);

        String result = utilities.getIdentityMappingJson(CONFIG_SERVER_URL, URI_PATH);

        assertEquals(JSON_RESPONSE, result);
    }

    /**
     * Tests successful IdRepo JSON retrieval operation.
     * Verifies that the method correctly retrieves identity data from IdRepo service
     * using UIN and returns a properly formatted JSONObject.
     */
    @Test
    void retrieveIdrepoJsonShouldSucceedWithValidUin() throws Exception {
        IdResponseDTO1 idResponseDto = createValidIdResponseDto();
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("name", "John Doe");

        when(restClientService.getApi(eq(ApiName.IDREPOGETIDBYUIN), any(List.class),
                anyString(), anyString(), eq(IdResponseDTO1.class))).thenReturn(idResponseDto);
        when(objMapper.writeValueAsString(any())).thenReturn("{\"name\":\"John Doe\"}");

        JSONObject result = utilities.retrieveIdrepoJson(UIN);

        assertNotNull(result);
    }

    /**
     * Tests IdRepo JSON retrieval with null UIN parameter.
     * Verifies that the method handles null UIN input gracefully
     * and returns null without attempting API calls.
     */
    @Test
    void retrieveIdrepoJsonWithNullUinShouldReturnNull() throws Exception {
        JSONObject result = utilities.retrieveIdrepoJson(null);

        assertNull(result);
    }

    /**
     * Tests IdRepo JSON retrieval when API returns null response.
     * Verifies that the method handles null API responses gracefully
     * and returns null when no data is available from IdRepo service.
     */
    @Test
    void retrieveIdrepoJsonWithNullResponseShouldReturnNull() throws Exception {
        when(restClientService.getApi(eq(ApiName.IDREPOGETIDBYUIN), any(List.class),
                anyString(), anyString(), eq(IdResponseDTO1.class))).thenReturn(null);

        JSONObject result = utilities.retrieveIdrepoJson(UIN);

        assertNull(result);
    }

    /**
     * Tests IdRepo JSON retrieval when response contains errors.
     * Verifies that the method throws IdRepoAppException when the API response
     * contains error information indicating operation failure.
     */
    @Test
    void retrieveIdrepoJsonWithErrorsShouldThrowIdRepoAppException() throws Exception {
        IdResponseDTO1 idResponseDto = createIdResponseDtoWithErrors();

        when(restClientService.getApi(eq(ApiName.IDREPOGETIDBYUIN), any(List.class),
                anyString(), anyString(), eq(IdResponseDTO1.class))).thenReturn(idResponseDto);

        assertThrows(IdRepoAppException.class, () -> utilities.retrieveIdrepoJson(UIN));
    }

    /**
     * Tests IdRepo JSON retrieval when JSON parsing exception occurs.
     * Verifies that the method throws IdRepoAppException when JSON parsing
     * fails during response processing operations.
     */
    @Test
    void retrieveIdrepoJsonWithParseExceptionShouldThrowIdRepoAppException() throws Exception {
        IdResponseDTO1 idResponseDto = createValidIdResponseDto();

        try (MockedStatic<ExceptionUtils> exceptionUtilsMock = mockStatic(ExceptionUtils.class)) {
            exceptionUtilsMock.when(() -> ExceptionUtils.getStackTrace(any(Exception.class)))
                    .thenReturn("Stack trace");

            when(restClientService.getApi(eq(ApiName.IDREPOGETIDBYUIN), any(List.class),
                    anyString(), anyString(), eq(IdResponseDTO1.class))).thenReturn(idResponseDto);
            when(objMapper.writeValueAsString(any())).thenReturn("invalid-json{");

            assertThrows(IdRepoAppException.class, () -> utilities.retrieveIdrepoJson(UIN));
        }
    }

    /**
     * Tests successful registration processor mapping JSON retrieval.
     * Verifies that the method correctly retrieves and processes registration processor
     * mapping configuration from external sources when no cached data exists.
     */
    @Test
    void getRegistrationProcessorMappingJsonShouldSucceedWithFreshData() throws Exception {
        ReflectionTestUtils.setField(utilities, "mappingJsonString", null);

        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            JSONObject expectedJson = new JSONObject();
            expectedJson.put("name", "value");

            lenient().when(restApiClient.getApi(any(URI.class), eq(String.class))).thenReturn(JSON_RESPONSE);
            lenient().when(objMapper.readValue(anyString(), eq(JSONObject.class))).thenReturn(new JSONObject());
            jsonUtilMock.when(() -> JsonUtil.getJSONObject(any(JSONObject.class), anyString()))
                    .thenReturn(expectedJson);

            JSONObject result = utilities.getRegistrationProcessorMappingJson();

            assertNotNull(result);
        }
    }

    /**
     * Tests registration processor mapping JSON retrieval with existing cached value.
     * Verifies that the method correctly processes cached mapping JSON data
     * without making external API calls when data is already available.
     */
    @Test
    void getRegistrationProcessorMappingJsonWithExistingValueShouldUseCachedData() throws Exception {
        ReflectionTestUtils.setField(utilities, "mappingJsonString", JSON_RESPONSE);

        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            JSONObject expectedJson = new JSONObject();
            expectedJson.put("name", "value");

            lenient().when(objMapper.readValue(anyString(), eq(JSONObject.class))).thenReturn(new JSONObject());
            jsonUtilMock.when(() -> JsonUtil.getJSONObject(any(JSONObject.class), anyString()))
                    .thenReturn(expectedJson);

            JSONObject result = utilities.getRegistrationProcessorMappingJson();

            assertNotNull(result);
        }
    }

    /**
     * Tests getMappingJsonValue method with LinkedHashMap data structure.
     * Verifies that the method correctly extracts values from nested LinkedHashMap
     * structure within the mapping JSON configuration.
     */
    @Test
    void getMappingJsonValueWithLinkedHashMapShouldExtractValue() throws Exception {
        ReflectionTestUtils.setField(utilities, "mappingJsonString", JSON_RESPONSE);

        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            JSONObject mappingJson = new JSONObject();
            LinkedHashMap<String, String> valueMap = new LinkedHashMap<>();
            valueMap.put("value", "testValue");
            mappingJson.put("testKey", valueMap);

            lenient().when(objMapper.readValue(anyString(), eq(JSONObject.class))).thenReturn(new JSONObject());
            jsonUtilMock.when(() -> JsonUtil.getJSONObject(any(JSONObject.class), anyString()))
                    .thenReturn(mappingJson);

            String result = utilities.getMappingJsonValue("testKey");

            assertEquals("testValue", result);
        }
    }

    /**
     * Tests getMappingJsonValue method with LinkedHashMap containing null value.
     * Verifies that the method handles null values gracefully within LinkedHashMap
     * structure and returns null appropriately.
     */
    @Test
    void getMappingJsonValueWithLinkedHashMapNullValueShouldReturnNull() throws Exception {
        ReflectionTestUtils.setField(utilities, "mappingJsonString", JSON_RESPONSE);

        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            JSONObject mappingJson = new JSONObject();
            LinkedHashMap<String, String> valueMap = new LinkedHashMap<>();
            valueMap.put("value", null);
            mappingJson.put("testKey", valueMap);

            lenient().when(objMapper.readValue(anyString(), eq(JSONObject.class))).thenReturn(new JSONObject());
            jsonUtilMock.when(() -> JsonUtil.getJSONObject(any(JSONObject.class), anyString()))
                    .thenReturn(mappingJson);

            String result = utilities.getMappingJsonValue("testKey");

            assertNull(result);
        }
    }

    /**
     * Tests getMappingJsonValue method with direct string value.
     * Verifies that the method correctly handles direct string values in mapping JSON
     * without nested data structure processing.
     */
    @Test
    void getMappingJsonValueWithDirectStringValueShouldReturnValue() throws Exception {
        ReflectionTestUtils.setField(utilities, "mappingJsonString", JSON_RESPONSE);

        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            JSONObject mappingJson = new JSONObject();
            mappingJson.put("testKey", "directValue");

            lenient().when(objMapper.readValue(anyString(), eq(JSONObject.class))).thenReturn(new JSONObject());
            jsonUtilMock.when(() -> JsonUtil.getJSONObject(any(JSONObject.class), anyString()))
                    .thenReturn(mappingJson);

            String result = utilities.getMappingJsonValue("testKey");

            assertEquals("directValue", result);
        }
    }

    /**
     * Tests getMappingJsonValue method with null value in mapping JSON.
     * Verifies that the method handles null values appropriately in mapping JSON
     * and returns null without causing exceptions.
     */
    @Test
    void getMappingJsonValueWithNullValueShouldReturnNull() throws Exception {
        ReflectionTestUtils.setField(utilities, "mappingJsonString", JSON_RESPONSE);

        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            JSONObject mappingJson = new JSONObject();
            mappingJson.put("testKey", null);

            lenient().when(objMapper.readValue(anyString(), eq(JSONObject.class))).thenReturn(new JSONObject());
            jsonUtilMock.when(() -> JsonUtil.getJSONObject(any(JSONObject.class), anyString()))
                    .thenReturn(mappingJson);

            String result = utilities.getMappingJsonValue("testKey");

            assertNull(result);
        }
    }

    /**
     * Tests successful UIN retrieval from registration ID.
     * Verifies that the method correctly retrieves UIN data from registration service
     * using registration ID and returns a properly formatted JSONObject.
     */
    @Test
    void retrieveUinShouldSucceedWithValidRegistrationId() throws Exception {
        IdResponseDTO1 idResponseDto = createValidIdResponseDto();
        JSONObject expectedJson = new JSONObject();
        expectedJson.put("uin", UIN);

        when(restClientService.getApi(eq(ApiName.RETRIEVEIDENTITYFROMRID), any(List.class),
                anyString(), anyString(), eq(IdResponseDTO1.class))).thenReturn(idResponseDto);
        when(objMapper.writeValueAsString(any())).thenReturn("{\"uin\":\"1234567890\"}");

        JSONObject result = utilities.retrieveUIN(REG_ID);

        assertNotNull(result);
    }

    /**
     * Tests UIN retrieval with null registration ID parameter.
     * Verifies that the method handles null registration ID input gracefully
     * and returns null without attempting API calls.
     */
    @Test
    void retrieveUinWithNullRegIdShouldReturnNull() throws Exception {
        JSONObject result = utilities.retrieveUIN(null);

        assertNull(result);
    }

    /**
     * Tests UIN retrieval when response contains errors.
     * Verifies that the method throws IdRepoAppException when the API response
     * contains error information indicating UIN retrieval failure.
     */
    @Test
    void retrieveUinWithErrorsShouldThrowIdRepoAppException() throws Exception {
        IdResponseDTO1 idResponseDto = createIdResponseDtoWithErrors();

        when(restClientService.getApi(eq(ApiName.RETRIEVEIDENTITYFROMRID), any(List.class),
                anyString(), anyString(), eq(IdResponseDTO1.class))).thenReturn(idResponseDto);

        assertThrows(IdRepoAppException.class, () -> utilities.retrieveUIN(REG_ID));
    }

    /**
     * Tests UIN retrieval when JSON parsing exception occurs.
     * Verifies that the method throws IdRepoAppException when JSON parsing
     * fails during UIN retrieval response processing.
     */
    @Test
    void retrieveUinWithParseExceptionShouldThrowIdRepoAppException() throws Exception {
        IdResponseDTO1 idResponseDto = createValidIdResponseDto();

        try (MockedStatic<ExceptionUtils> exceptionUtilsMock = mockStatic(ExceptionUtils.class)) {
            exceptionUtilsMock.when(() -> ExceptionUtils.getStackTrace(any(Exception.class)))
                    .thenReturn("Stack trace");

            when(restClientService.getApi(eq(ApiName.RETRIEVEIDENTITYFROMRID), any(List.class),
                    anyString(), anyString(), eq(IdResponseDTO1.class))).thenReturn(idResponseDto);
            when(objMapper.writeValueAsString(any())).thenReturn("invalid-json{");

            assertThrows(IdRepoAppException.class, () -> utilities.retrieveUIN(REG_ID));
        }
    }

    /**
     * Helper method to create valid IdResponseDTO1 for testing purposes.
     * Creates a mock response object with proper identity data and empty error list
     * for successful API response simulation.
     *
     * @return IdResponseDTO1 with valid response data
     */
    private IdResponseDTO1 createValidIdResponseDto() {
        IdResponseDTO1 idResponseDto = new IdResponseDTO1();
        ResponseDTO responseDto = new ResponseDTO();
        responseDto.setIdentity(new Object());
        idResponseDto.setResponse(responseDto);
        idResponseDto.setErrors(new ArrayList<>());
        return idResponseDto;
    }

    /**
     * Helper method to create IdResponseDTO1 with error information for testing.
     * Creates a mock response object containing error data to simulate
     * API failure scenarios and error handling.
     *
     * @return IdResponseDTO1 with error information
     */
    private IdResponseDTO1 createIdResponseDtoWithErrors() {
        IdResponseDTO1 idResponseDto = new IdResponseDTO1();
        List<ErrorDTO> errors = new ArrayList<>();
        ErrorDTO error = new ErrorDTO();
        error.setMessage("Test error message");
        errors.add(error);
        idResponseDto.setErrors(errors);
        return idResponseDto;
    }
}
