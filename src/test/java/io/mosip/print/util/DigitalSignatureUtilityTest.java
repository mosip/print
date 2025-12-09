package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.print.constant.ApiName;
import io.mosip.print.core.http.RequestWrapper;
import io.mosip.print.core.http.ResponseWrapper;
import io.mosip.print.dto.ErrorDTO;
import io.mosip.print.dto.SignResponseDto;
import io.mosip.print.exception.ApisResourceAccessException;
import io.mosip.print.exception.DigitalSignatureException;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.service.PrintRestClientService;

/**
 * Unit tests for {@link DigitalSignatureUtility} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the DigitalSignatureUtility class,
 * including digital signature generation operations, API communication, error handling scenarios, JSON processing,
 * exception management, and various edge cases for digital signature creation and cryptographic operations.</p>
 */
@ExtendWith(MockitoExtension.class)
class DigitalSignatureUtilityTest {

    @Mock
    private PrintRestClientService<Object> printRestService;

    @Mock
    private Environment env;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private DigitalSignatureUtility digitalSignatureUtility;

    private static final String DIGITAL_SIGNATURE_ID = "test-digital-signature-id";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String REG_PROC_APPLICATION_VERSION = "1.0";
    private static final String CURRENT_DATETIME = "2025-08-03T10:00:00.000Z";
    private static final String TEST_DATA = "test-data-to-sign";
    private static final String SIGNATURE_RESPONSE = "test-signature-response";

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes the DigitalSignatureUtility instance with environment configuration
     * including digital signature ID, datetime patterns, and application version settings.
     */
    @BeforeEach
    void setUp() {
        when(env.getProperty("mosip.registration.processor.digital.signature.id")).thenReturn(DIGITAL_SIGNATURE_ID);
        when(env.getProperty("mosip.registration.processor.datetime.pattern")).thenReturn(DATETIME_PATTERN);
        when(env.getProperty("mosip.registration.processor.application.version")).thenReturn(REG_PROC_APPLICATION_VERSION);
    }

    /**
     * Tests successful digital signature generation operation.
     * Verifies that the method correctly generates digital signatures for valid input data,
     * processes API responses, and returns the expected signature string.
     */
    @Test
    void getDigitalSignatureShouldSucceedWithValidData() throws Exception {
        ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
        SignResponseDto signResponseDto = new SignResponseDto();
        signResponseDto.setSignature(SIGNATURE_RESPONSE);
        responseWrapper.setResponse(signResponseDto);
        responseWrapper.setErrors(new ArrayList<>());

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DigitalSignatureUtility.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            when(printRestService.postApi(eq(ApiName.DIGITALSIGNATURE), anyString(), anyString(),
                    any(RequestWrapper.class), eq(ResponseWrapper.class))).thenReturn(responseWrapper);
            when(mapper.writeValueAsString(signResponseDto)).thenReturn("{\"signature\":\"test-signature-response\"}");
            when(mapper.readValue("{\"signature\":\"test-signature-response\"}", SignResponseDto.class))
                    .thenReturn(signResponseDto);

            String result = digitalSignatureUtility.getDigitalSignature(TEST_DATA);

            assertEquals(SIGNATURE_RESPONSE, result);
        }
    }

    /**
     * Tests digital signature generation when API response contains errors.
     * Verifies that the method handles error responses gracefully and still returns
     * the signature when available despite error conditions.
     */
    @Test
    void getDigitalSignatureWithErrorsShouldHandleErrorsGracefully() throws Exception {
        ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
        SignResponseDto signResponseDto = new SignResponseDto();
        signResponseDto.setSignature(SIGNATURE_RESPONSE);
        responseWrapper.setResponse(signResponseDto);

        List<ErrorDTO> errors = new ArrayList<>();
        ErrorDTO error1 = new ErrorDTO();
        error1.setMessage("Error message 1");
        ErrorDTO error2 = new ErrorDTO();
        error2.setMessage("Error message 2");
        errors.add(error1);
        errors.add(error2);
        responseWrapper.setErrors(errors);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DigitalSignatureUtility.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            when(printRestService.postApi(eq(ApiName.DIGITALSIGNATURE), anyString(), anyString(),
                    any(RequestWrapper.class), eq(ResponseWrapper.class))).thenReturn(responseWrapper);
            when(mapper.writeValueAsString(signResponseDto)).thenReturn("{\"signature\":\"test-signature-response\"}");
            when(mapper.readValue("{\"signature\":\"test-signature-response\"}", SignResponseDto.class))
                    .thenReturn(signResponseDto);

            String result = digitalSignatureUtility.getDigitalSignature(TEST_DATA);

            assertEquals(SIGNATURE_RESPONSE, result);
        }
    }

    /**
     * Tests digital signature generation when error list is null.
     * Verifies that the method handles null error lists gracefully and processes
     * the digital signature operation without encountering null pointer exceptions.
     */
    @Test
    void getDigitalSignatureWithNullErrorsShouldHandleGracefully() throws Exception {
        ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
        SignResponseDto signResponseDto = new SignResponseDto();
        signResponseDto.setSignature(SIGNATURE_RESPONSE);
        responseWrapper.setResponse(signResponseDto);
        responseWrapper.setErrors(null);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DigitalSignatureUtility.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            when(printRestService.postApi(eq(ApiName.DIGITALSIGNATURE), anyString(), anyString(),
                    any(RequestWrapper.class), eq(ResponseWrapper.class))).thenReturn(responseWrapper);
            when(mapper.writeValueAsString(signResponseDto)).thenReturn("{\"signature\":\"test-signature-response\"}");
            when(mapper.readValue("{\"signature\":\"test-signature-response\"}", SignResponseDto.class))
                    .thenReturn(signResponseDto);

            String result = digitalSignatureUtility.getDigitalSignature(TEST_DATA);

            assertEquals(SIGNATURE_RESPONSE, result);
        }
    }

    /**
     * Tests digital signature generation when error list is empty.
     * Verifies that the method handles empty error lists correctly and proceeds
     * with normal digital signature processing operations.
     */
    @Test
    void getDigitalSignatureWithEmptyErrorsShouldProceedNormally() throws Exception {
        ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
        SignResponseDto signResponseDto = new SignResponseDto();
        signResponseDto.setSignature(SIGNATURE_RESPONSE);
        responseWrapper.setResponse(signResponseDto);
        responseWrapper.setErrors(new ArrayList<>());

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DigitalSignatureUtility.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            when(printRestService.postApi(eq(ApiName.DIGITALSIGNATURE), anyString(), anyString(),
                    any(RequestWrapper.class), eq(ResponseWrapper.class))).thenReturn(responseWrapper);
            when(mapper.writeValueAsString(signResponseDto)).thenReturn("{\"signature\":\"test-signature-response\"}");
            when(mapper.readValue("{\"signature\":\"test-signature-response\"}", SignResponseDto.class))
                    .thenReturn(signResponseDto);

            String result = digitalSignatureUtility.getDigitalSignature(TEST_DATA);

            assertEquals(SIGNATURE_RESPONSE, result);
        }
    }

    /**
     * Tests digital signature generation when ApisResourceAccessException occurs.
     * Verifies that DigitalSignatureException is thrown when API communication fails,
     * wrapping the original exception with appropriate error messaging.
     */
    @Test
    void getDigitalSignatureWithApisResourceAccessExceptionShouldThrowDigitalSignatureException() throws Exception {
        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DigitalSignatureUtility.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            when(printRestService.postApi(eq(ApiName.DIGITALSIGNATURE), anyString(), anyString(),
                    any(RequestWrapper.class), eq(ResponseWrapper.class)))
                    .thenThrow(new ApisResourceAccessException("API Error"));

            DigitalSignatureException exception = assertThrows(DigitalSignatureException.class, () ->
                    digitalSignatureUtility.getDigitalSignature(TEST_DATA)
            );

            assertTrue(exception.getMessage().contains("API Error"));
        }
    }

    /**
     * Tests digital signature generation when JsonProcessingException occurs during JSON writing.
     * Verifies that DigitalSignatureException is thrown when JSON serialization fails,
     * providing appropriate error information for debugging purposes.
     */
    @Test
    void getDigitalSignatureWithJsonProcessingExceptionFromMapperShouldThrowDigitalSignatureException() throws Exception {
        ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
        SignResponseDto signResponseDto = new SignResponseDto();
        signResponseDto.setSignature(SIGNATURE_RESPONSE);
        responseWrapper.setResponse(signResponseDto);
        responseWrapper.setErrors(new ArrayList<>());

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DigitalSignatureUtility.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            when(printRestService.postApi(eq(ApiName.DIGITALSIGNATURE), anyString(), anyString(),
                    any(RequestWrapper.class), eq(ResponseWrapper.class))).thenReturn(responseWrapper);
            when(mapper.writeValueAsString(signResponseDto)).thenThrow(new JsonProcessingException("JSON Error") {});

            DigitalSignatureException exception = assertThrows(DigitalSignatureException.class, () ->
                    digitalSignatureUtility.getDigitalSignature(TEST_DATA)
            );

            assertTrue(exception.getMessage().contains("JSON Error"));
        }
    }

    /**
     * Tests digital signature generation when IOException occurs during JSON reading.
     * Verifies that DigitalSignatureException is thrown when JSON deserialization fails,
     * handling read operations gracefully with proper exception wrapping.
     */
    @Test
    void getDigitalSignatureWithIoExceptionFromMapperReadValueShouldThrowDigitalSignatureException() throws Exception {
        ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
        SignResponseDto signResponseDto = new SignResponseDto();
        signResponseDto.setSignature(SIGNATURE_RESPONSE);
        responseWrapper.setResponse(signResponseDto);
        responseWrapper.setErrors(new ArrayList<>());

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DigitalSignatureUtility.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            when(printRestService.postApi(eq(ApiName.DIGITALSIGNATURE), anyString(), anyString(),
                    any(RequestWrapper.class), eq(ResponseWrapper.class))).thenReturn(responseWrapper);
            when(mapper.writeValueAsString(signResponseDto)).thenReturn("{\"signature\":\"test-signature-response\"}");
            when(mapper.readValue(anyString(), eq(SignResponseDto.class)))
                    .thenThrow(new JsonProcessingException("JSON Read Error") {});

            DigitalSignatureException exception = assertThrows(DigitalSignatureException.class, () ->
                    digitalSignatureUtility.getDigitalSignature(TEST_DATA)
            );

            assertTrue(exception.getMessage().contains("JSON Read Error"));
        }
    }

    /**
     * Tests digital signature generation with null input data.
     * Verifies that the method handles null input gracefully and still processes
     * the digital signature request without causing null pointer exceptions.
     */
    @Test
    void getDigitalSignatureWithNullDataShouldHandleGracefully() throws Exception {
        ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
        SignResponseDto signResponseDto = new SignResponseDto();
        signResponseDto.setSignature(SIGNATURE_RESPONSE);
        responseWrapper.setResponse(signResponseDto);
        responseWrapper.setErrors(new ArrayList<>());

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DigitalSignatureUtility.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            when(printRestService.postApi(eq(ApiName.DIGITALSIGNATURE), anyString(), anyString(),
                    any(RequestWrapper.class), eq(ResponseWrapper.class))).thenReturn(responseWrapper);
            when(mapper.writeValueAsString(signResponseDto)).thenReturn("{\"signature\":\"test-signature-response\"}");
            when(mapper.readValue("{\"signature\":\"test-signature-response\"}", SignResponseDto.class))
                    .thenReturn(signResponseDto);

            String result = digitalSignatureUtility.getDigitalSignature(null);

            assertEquals(SIGNATURE_RESPONSE, result);
        }
    }

    /**
     * Tests digital signature generation with empty input data.
     * Verifies that the method handles empty string input appropriately and processes
     * the digital signature request for empty data scenarios.
     */
    @Test
    void getDigitalSignatureWithEmptyDataShouldProcessCorrectly() throws Exception {
        ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
        SignResponseDto signResponseDto = new SignResponseDto();
        signResponseDto.setSignature(SIGNATURE_RESPONSE);
        responseWrapper.setResponse(signResponseDto);
        responseWrapper.setErrors(new ArrayList<>());

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DigitalSignatureUtility.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            when(printRestService.postApi(eq(ApiName.DIGITALSIGNATURE), anyString(), anyString(),
                    any(RequestWrapper.class), eq(ResponseWrapper.class))).thenReturn(responseWrapper);
            when(mapper.writeValueAsString(signResponseDto)).thenReturn("{\"signature\":\"test-signature-response\"}");
            when(mapper.readValue("{\"signature\":\"test-signature-response\"}", SignResponseDto.class))
                    .thenReturn(signResponseDto);

            String result = digitalSignatureUtility.getDigitalSignature("");

            assertEquals(SIGNATURE_RESPONSE, result);
        }
    }
}
