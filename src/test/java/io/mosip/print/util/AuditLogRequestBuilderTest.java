package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import io.mosip.print.constant.ApiName;
import io.mosip.print.core.http.RequestWrapper;
import io.mosip.print.core.http.ResponseWrapper;
import io.mosip.print.dto.AuditResponseDto;
import io.mosip.print.exception.ApisResourceAccessException;
import io.mosip.print.service.PrintRestClientService;

/**
 * Unit tests for {@link AuditLogRequestBuilder} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the AuditLogRequestBuilder class,
 * including audit request creation, API integration, exception handling, and various parameter validation scenarios.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuditLogRequestBuilderTest {

    @Mock
    private PrintRestClientService<Object> registrationProcessorRestService;

    @Mock
    private Environment env;

    @InjectMocks
    private AuditLogRequestBuilder auditLogRequestBuilder;

    private static final String AUDIT_SERVICE_ID = "AUDIT_SERVICE";
    private static final String REG_PROC_APPLICATION_VERSION = "1.0";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String CURRENT_DATETIME = "2025-08-03T10:00:00.000Z";
    private static final String HOST_IP = "192.168.1.1";
    private static final String HOST_NAME = "test-host";
    private static final String REGISTRATION_ID = "REG123456789";

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes environment properties and mock configurations required for audit logging tests.
     */
    @BeforeEach
    void setUp() {
        when(env.getProperty("mosip.print.audit.id")).thenReturn(AUDIT_SERVICE_ID);
        when(env.getProperty("mosip.print.application.version")).thenReturn(REG_PROC_APPLICATION_VERSION);
        when(env.getProperty("mosip.print.datetime.pattern")).thenReturn(DATETIME_PATTERN);
    }

    /**
     * Tests successful audit request creation with ApiName parameter.
     * Verifies that the audit request builder correctly creates and processes audit requests
     * when provided with valid parameters and ApiName.
     */
    @Test
    void createAuditRequestBuilderWithApiNameShouldSucceed() throws ApisResourceAccessException {
        ResponseWrapper<AuditResponseDto> expectedResponse = new ResponseWrapper<>();
        AuditResponseDto auditResponseDto = new AuditResponseDto();
        expectedResponse.setResponse(auditResponseDto);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<ServerUtil> serverUtilMock = mockStatic(ServerUtil.class)) {

            ServerUtil mockServerUtil = mock(ServerUtil.class);
            dateUtilsMock.when(DateUtils::getUTCCurrentDateTimeString).thenReturn(CURRENT_DATETIME);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN)).thenReturn(CURRENT_DATETIME);
            serverUtilMock.when(ServerUtil::getServerUtilInstance).thenReturn(mockServerUtil);
            when(mockServerUtil.getServerIp()).thenReturn(HOST_IP);
            when(mockServerUtil.getServerName()).thenReturn(HOST_NAME);

            when(registrationProcessorRestService.postApi(
                    eq(ApiName.AUDIT),
                    anyString(),
                    anyString(),
                    any(RequestWrapper.class),
                    eq(ResponseWrapper.class)
            )).thenReturn(expectedResponse);

            ResponseWrapper<AuditResponseDto> result = auditLogRequestBuilder.createAuditRequestBuilder(
                    "Test Description",
                    "EVENT001",
                    "Test Event",
                    "USER_ACTION",
                    REGISTRATION_ID,
                    ApiName.AUDIT
            );

            assertNotNull(result);
            assertEquals(expectedResponse, result);
        }
    }

    /**
     * Tests audit request creation with ApiName parameter when API exception occurs.
     * Verifies that the audit request builder handles ApisResourceAccessException gracefully
     * and returns a valid response even when the API call fails.
     */
    @Test
    void createAuditRequestBuilderWithApiNameShouldHandleException() throws ApisResourceAccessException {
        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<ServerUtil> serverUtilMock = mockStatic(ServerUtil.class)) {

            ServerUtil mockServerUtil = mock(ServerUtil.class);
            dateUtilsMock.when(DateUtils::getUTCCurrentDateTimeString).thenReturn(CURRENT_DATETIME);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN)).thenReturn(CURRENT_DATETIME);
            serverUtilMock.when(ServerUtil::getServerUtilInstance).thenReturn(mockServerUtil);
            when(mockServerUtil.getServerIp()).thenReturn(HOST_IP);
            when(mockServerUtil.getServerName()).thenReturn(HOST_NAME);

            when(registrationProcessorRestService.postApi(
                    eq(ApiName.AUDIT),
                    anyString(),
                    anyString(),
                    any(RequestWrapper.class),
                    eq(ResponseWrapper.class)
            )).thenThrow(new ApisResourceAccessException("API Error"));

            ResponseWrapper<AuditResponseDto> result = auditLogRequestBuilder.createAuditRequestBuilder(
                    "Test Description",
                    "EVENT001",
                    "Test Event",
                    "USER_ACTION",
                    REGISTRATION_ID,
                    ApiName.AUDIT
            );

            assertNotNull(result);
        }
    }

    /**
     * Tests successful audit request creation with module parameters.
     * Verifies that the audit request builder correctly processes requests when provided
     * with module-specific parameters including module ID and module name.
     */
    @Test
    void createAuditRequestBuilderWithModuleShouldSucceed() throws ApisResourceAccessException {
        ResponseWrapper<AuditResponseDto> expectedResponse = new ResponseWrapper<>();
        AuditResponseDto auditResponseDto = new AuditResponseDto();
        expectedResponse.setResponse(auditResponseDto);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<ServerUtil> serverUtilMock = mockStatic(ServerUtil.class)) {

            ServerUtil mockServerUtil = mock(ServerUtil.class);
            dateUtilsMock.when(DateUtils::getUTCCurrentDateTimeString).thenReturn(CURRENT_DATETIME);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN)).thenReturn(CURRENT_DATETIME);
            serverUtilMock.when(ServerUtil::getServerUtilInstance).thenReturn(mockServerUtil);
            when(mockServerUtil.getServerIp()).thenReturn(HOST_IP);
            when(mockServerUtil.getServerName()).thenReturn(HOST_NAME);

            when(registrationProcessorRestService.postApi(
                    eq(ApiName.AUDIT),
                    anyString(),
                    anyString(),
                    any(RequestWrapper.class),
                    eq(ResponseWrapper.class)
            )).thenReturn(expectedResponse);

            ResponseWrapper<AuditResponseDto> result = auditLogRequestBuilder.createAuditRequestBuilder(
                    "Test Description",
                    "EVENT001",
                    "Test Event",
                    "USER_ACTION",
                    "MODULE001",
                    "Test Module",
                    REGISTRATION_ID
            );

            assertNotNull(result);
            assertEquals(expectedResponse, result);
        }
    }

    /**
     * Tests audit request creation with module parameters when API exception occurs.
     * Verifies that the audit request builder handles exceptions appropriately when
     * using module-specific parameters and API calls fail.
     */
    @Test
    void createAuditRequestBuilderWithModuleShouldHandleException() throws ApisResourceAccessException {
        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<ServerUtil> serverUtilMock = mockStatic(ServerUtil.class)) {

            ServerUtil mockServerUtil = mock(ServerUtil.class);
            dateUtilsMock.when(DateUtils::getUTCCurrentDateTimeString).thenReturn(CURRENT_DATETIME);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN)).thenReturn(CURRENT_DATETIME);
            serverUtilMock.when(ServerUtil::getServerUtilInstance).thenReturn(mockServerUtil);
            when(mockServerUtil.getServerIp()).thenReturn(HOST_IP);
            when(mockServerUtil.getServerName()).thenReturn(HOST_NAME);

            when(registrationProcessorRestService.postApi(
                    eq(ApiName.AUDIT),
                    anyString(),
                    anyString(),
                    any(RequestWrapper.class),
                    eq(ResponseWrapper.class)
            )).thenThrow(new ApisResourceAccessException("API Error"));

            ResponseWrapper<AuditResponseDto> result = auditLogRequestBuilder.createAuditRequestBuilder(
                    "Test Description",
                    "EVENT001",
                    "Test Event",
                    "USER_ACTION",
                    "MODULE001",
                    "Test Module",
                    REGISTRATION_ID
            );

            assertNotNull(result);
        }
    }

    /**
     * Tests audit request builder with null parameter values.
     * Verifies that the audit request builder handles null input parameters gracefully
     * and still produces a valid audit response without failing.
     */
    @Test
    void createAuditRequestBuilderWithNullValuesShouldHandleGracefully() throws ApisResourceAccessException {
        ResponseWrapper<AuditResponseDto> expectedResponse = new ResponseWrapper<>();

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<ServerUtil> serverUtilMock = mockStatic(ServerUtil.class)) {

            ServerUtil mockServerUtil = mock(ServerUtil.class);
            dateUtilsMock.when(DateUtils::getUTCCurrentDateTimeString).thenReturn(CURRENT_DATETIME);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN)).thenReturn(CURRENT_DATETIME);
            serverUtilMock.when(ServerUtil::getServerUtilInstance).thenReturn(mockServerUtil);
            when(mockServerUtil.getServerIp()).thenReturn(HOST_IP);
            when(mockServerUtil.getServerName()).thenReturn(HOST_NAME);

            when(registrationProcessorRestService.postApi(
                    eq(ApiName.AUDIT),
                    anyString(),
                    anyString(),
                    any(RequestWrapper.class),
                    eq(ResponseWrapper.class)
            )).thenReturn(expectedResponse);

            ResponseWrapper<AuditResponseDto> result = auditLogRequestBuilder.createAuditRequestBuilder(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            assertNotNull(result);
        }
    }

    /**
     * Tests audit request builder with ApiName and null parameter values.
     * Verifies that the audit request builder handles null input parameters appropriately
     * when ApiName is provided, ensuring robust error handling.
     */
    @Test
    void createAuditRequestBuilderWithApiNameAndNullValuesShouldHandleGracefully() throws ApisResourceAccessException {
        ResponseWrapper<AuditResponseDto> expectedResponse = new ResponseWrapper<>();

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<ServerUtil> serverUtilMock = mockStatic(ServerUtil.class)) {

            ServerUtil mockServerUtil = mock(ServerUtil.class);
            dateUtilsMock.when(DateUtils::getUTCCurrentDateTimeString).thenReturn(CURRENT_DATETIME);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN)).thenReturn(CURRENT_DATETIME);
            serverUtilMock.when(ServerUtil::getServerUtilInstance).thenReturn(mockServerUtil);
            when(mockServerUtil.getServerIp()).thenReturn(HOST_IP);
            when(mockServerUtil.getServerName()).thenReturn(HOST_NAME);

            when(registrationProcessorRestService.postApi(
                    eq(ApiName.AUDIT),
                    anyString(),
                    anyString(),
                    any(RequestWrapper.class),
                    eq(ResponseWrapper.class)
            )).thenReturn(expectedResponse);

            ResponseWrapper<AuditResponseDto> result = auditLogRequestBuilder.createAuditRequestBuilder(
                    null,
                    null,
                    null,
                    null,
                    null,
                    ApiName.AUDIT
            );

            assertNotNull(result);
        }
    }
}
