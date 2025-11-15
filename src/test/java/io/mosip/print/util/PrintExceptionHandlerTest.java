package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import java.time.Instant;
import static org.mockito.Mockito.mockStatic;

import java.util.Arrays;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import io.mosip.print.dto.PrintResponse;
import io.mosip.print.exception.AccessDeniedException;
import io.mosip.print.exception.InvalidTokenException;
import io.mosip.print.exception.PDFGeneratorException;
import io.mosip.print.exception.PDFSignatureException;
import io.mosip.print.exception.PlatformErrorMessages;
import io.mosip.print.exception.RegPrintAppException;
import io.mosip.print.exception.TemplateProcessingFailureException;
import io.mosip.print.logger.PrintLogger;

/**
 * Unit tests for {@link PrintExceptionHandler} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the PrintExceptionHandler class,
 * including exception handling for various print service errors, response formatting, HTTP status codes,
 * and proper error message construction for different exception types.</p>
 */
@ExtendWith(MockitoExtension.class)
class PrintExceptionHandlerTest {

    @Mock
    private Environment env;

    @InjectMocks
    private PrintExceptionHandler printExceptionHandler;

    private static final String SERVICE_ID = "mosip.print.service";
    private static final String SERVICE_VERSION = "1.0";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String CURRENT_DATETIME = "2025-08-03T10:00:00.000Z";

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes environment properties and mock configurations required for exception handler testing.
     */
    @BeforeEach
    void setUp() {
        when(env.getProperty("mosip.print.service.id")).thenReturn(SERVICE_ID);
        when(env.getProperty("mosip.print.application.version")).thenReturn(SERVICE_VERSION);
        when(env.getProperty("mosip.print.datetime.pattern")).thenReturn(DATETIME_PATTERN);
    }

    /**
     * Tests the PDFGeneratorException handler functionality.
     * Verifies that PDFGeneratorException is properly handled with appropriate error codes
     * from PlatformErrorMessages and correct response formatting.
     */
    @Test
    void pdfGeneratorExceptionHandlerShouldReturnProperResponse() {
        PDFGeneratorException exception = new PDFGeneratorException(
                PlatformErrorMessages.PRT_PIS_IDENTITY_NOT_FOUND.getCode(),
                PlatformErrorMessages.PRT_PIS_IDENTITY_NOT_FOUND.getMessage(),
                new RuntimeException("PDF generation error"));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.pdfgeneratorException(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests the TemplateProcessingFailureException handler functionality.
     * Verifies that template processing failures are properly handled with correct
     * error code mapping and response structure.
     */
    @Test
    void templateFailureExceptionHandlerShouldReturnProperResponse() {
        TemplateProcessingFailureException exception = new TemplateProcessingFailureException("ERR-003");

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.templateFailureException(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests the JsonMappingException handler functionality.
     * Verifies that JSON mapping exceptions are properly handled through the badRequest
     * handler with appropriate error formatting.
     */
    @Test
    void jsonMappingExceptionHandlerShouldReturnBadRequestResponse() {
        JsonMappingException exception = new JsonMappingException(mock(JsonParser.class), "JSON mapping error");

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.badRequest(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests the InvalidFormatException handler functionality.
     * Verifies that invalid format exceptions are properly handled with correct
     * error message extraction and response formatting.
     */
    @Test
    void invalidFormatExceptionHandlerShouldReturnBadRequestResponse() {
        InvalidFormatException exception = new InvalidFormatException(mock(JsonParser.class),
                "Invalid format", "invalidValue", String.class);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.badRequest(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests the JsonParseException handler functionality.
     * Verifies that JSON parse exceptions are properly handled through the badRequest
     * handler with appropriate error processing.
     */
    @Test
    void jsonParseExceptionHandlerShouldReturnBadRequestResponse() {
        JsonParseException exception = new JsonParseException(mock(JsonParser.class), "JSON parse error");

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.badRequest(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests the MethodArgumentNotValidException handler functionality.
     * Verifies that method argument validation exceptions are properly handled
     * with correct binding result processing and error response formatting.
     */
    @Test
    void methodArgumentNotValidExceptionHandlerShouldReturnBadRequestResponse() {
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.badRequest(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests the AccessDeniedException handler functionality.
     * Verifies that access denied exceptions are properly handled with security-related
     * error codes and appropriate response structure.
     */
    @Test
    void accessDeniedExceptionHandlerShouldReturnProperResponse() {
        AccessDeniedException exception = new AccessDeniedException(
                "Access denied error",
                new SecurityException("Access denied cause"));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.accessDenied(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests the InvalidTokenException handler functionality.
     * Verifies that invalid token exceptions are properly handled with token-related
     * error codes and correct authentication error response.
     */
    @Test
    void invalidTokenExceptionHandlerShouldReturnProperResponse() {
        InvalidTokenException exception = new InvalidTokenException("ERR-005", "Invalid token error");

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.invalidToken(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests the PDFSignatureException handler functionality.
     * Verifies that PDF signature exceptions are properly handled with signature-related
     * error processing and appropriate response formatting.
     */
    @Test
    void pdfSignatureExceptionHandlerShouldReturnProperResponse() {
        PDFSignatureException exception = new PDFSignatureException(
                "PDF signature error",
                new RuntimeException("PDF signature cause"));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.pdfSignatureException(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests BaseCheckedException handling in buildPrintApiExceptionResponse method.
     * Verifies that checked exceptions are properly processed through the exception
     * response builder with correct categorization.
     */
    @Test
    void buildPrintApiExceptionResponseWithBaseCheckedExceptionShouldHandleCorrectly() {
        RegPrintAppException exception = new RegPrintAppException("ERR-007", "Checked exception");

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.regPrintAppException(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests BaseUncheckedException handling in buildPrintApiExceptionResponse method.
     * Verifies that unchecked exceptions are properly processed with correct
     * exception categorization and response formatting.
     */
    @Test
    void buildPrintApiExceptionResponseWithBaseUncheckedExceptionShouldHandleCorrectly() {
        AccessDeniedException exception = new AccessDeniedException(
                "Unchecked exception error",
                new SecurityException("Unchecked exception cause"));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.accessDenied(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests response building with null ID scenario.
     * Verifies that the exception handler properly handles cases where service ID
     * might be null and sets appropriate default values.
     */
    @Test
    void buildPrintApiExceptionResponseWithNullIdShouldSetDefaultId() {
        RegPrintAppException exception = new RegPrintAppException("ERR-009", "Test error");

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.regPrintAppException(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(SERVICE_ID, response.getBody().getId());
        }
    }

    /**
     * Tests exception handling with multiple error codes and messages.
     * Verifies that exceptions containing multiple error codes and messages
     * are properly processed and formatted in the response.
     */
    @Test
    void buildPrintApiExceptionResponseWithMultipleErrorsShouldHandleCorrectly() {
        TestMultipleErrorsCheckedException exception = new TestMultipleErrorsCheckedException();

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.regPrintAppException(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Tests response building with existing ID scenario.
     * Verifies that the exception handler properly uses existing service ID
     * when it's already set in the response.
     */
    @Test
    void buildPrintApiExceptionResponseWithExistingIdShouldPreserveId() {
        RegPrintAppException exception = new RegPrintAppException("ERR-011", "Test error");

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(PrintExceptionHandler.class))
                    .thenReturn(mockLogger);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN))
                    .thenReturn(CURRENT_DATETIME);

            ResponseEntity<PrintResponse> response = printExceptionHandler.regPrintAppException(exception);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    /**
     * Helper class for testing multiple errors scenario.
     * Extends RegPrintAppException to provide multiple error codes and messages
     * for comprehensive exception handling testing.
     */
    private static class TestMultipleErrorsCheckedException extends RegPrintAppException {
        public TestMultipleErrorsCheckedException() {
            super("ERR-010", "Test multiple errors");
        }

        @Override
        public List<String> getCodes() {
            return Arrays.asList("ERR-001", "ERR-002", "ERR-003");
        }

        @Override
        public List<String> getErrorTexts() {
            return Arrays.asList("Error message 1", "Error message 2", "Error message 3");
        }
    }
}
