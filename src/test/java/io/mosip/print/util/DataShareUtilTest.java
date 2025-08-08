package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.print.constant.ApiName;
import io.mosip.print.dto.DataShare;
import io.mosip.print.dto.DataShareResponseDto;
import io.mosip.print.dto.ErrorDTO;
import io.mosip.print.exception.ApiNotAccessibleException;
import io.mosip.print.exception.DataShareException;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.service.PrintRestClientService;

/**
 * Unit tests for {@link DataShareUtil} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the DataShareUtil class,
 * including data sharing operations, API communication, error handling scenarios, HTTP exception processing,
 * JSON parsing operations, and various edge cases for data share creation and management.</p>
 */
@ExtendWith(MockitoExtension.class)
class DataShareUtilTest {

    @Mock
    private PrintRestClientService<Object> restUtil;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private DataShareUtil dataShareUtil;

    private static final String POLICY_ID = "test-policy-id";
    private static final String PARTNER_ID = "test-partner-id";
    private static final byte[] TEST_DATA = "test credential data".getBytes();
    private static final String RESPONSE_STRING = "{\"dataShare\":{\"url\":\"http://test.com\"}}";

    private DataShareResponseDto mockResponseDto;
    private DataShare mockDataShare;
    private ErrorDTO mockError;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes mock objects for data share operations including response DTOs,
     * data share objects, and error handling components.
     */
    @BeforeEach
    void setUp() {
        mockResponseDto = new DataShareResponseDto();
        mockDataShare = new DataShare();
        mockDataShare.setUrl("http://test-datashare.com");
        mockError = new ErrorDTO();
        mockError.setErrorCode("ERR-001");
        mockError.setMessage("Test error");
    }

    /**
     * Tests successful data share creation operation.
     * Verifies that the method correctly creates a data share with valid input parameters
     * and returns the expected DataShare object with proper URL configuration.
     */
    @Test
    void getDataShareShouldSucceedWithValidParameters() throws Exception {
        mockResponseDto.setDataShare(mockDataShare);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);
            when(mapper.readValue(RESPONSE_STRING, DataShareResponseDto.class)).thenReturn(mockResponseDto);

            DataShare result = dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID);

            assertNotNull(result);
            assertEquals("http://test-datashare.com", result.getUrl());
        }
    }

    /**
     * Tests data share creation when API returns null response.
     * Verifies that DataShareException is thrown when the REST API call returns
     * null response data during data share creation.
     */
    @Test
    void getDataShareWithNullResponseShouldThrowDataShareException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);
            when(mapper.readValue(RESPONSE_STRING, DataShareResponseDto.class)).thenReturn(null);

            assertThrows(DataShareException.class, () ->
                    dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID)
            );
        }
    }

    /**
     * Tests data share creation when API response contains error information.
     * Verifies that DataShareException is thrown when the response contains
     * error details indicating operation failure.
     */
    @Test
    void getDataShareWithErrorResponseShouldThrowDataShareException() throws Exception {
        List<ErrorDTO> errors = new ArrayList<>();
        errors.add(mockError);
        mockResponseDto.setErrors(errors);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);
            when(mapper.readValue(RESPONSE_STRING, DataShareResponseDto.class)).thenReturn(mockResponseDto);

            assertThrows(DataShareException.class, () ->
                    dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID)
            );
        }
    }

    /**
     * Tests data share creation with empty error list in response.
     * Verifies that the method succeeds when the response contains an empty error list
     * and returns a valid DataShare object.
     */
    @Test
    void getDataShareWithEmptyErrorListShouldSucceed() throws Exception {
        List<ErrorDTO> emptyErrors = new ArrayList<>();
        mockResponseDto.setErrors(emptyErrors);
        mockResponseDto.setDataShare(mockDataShare);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);
            when(mapper.readValue(RESPONSE_STRING, DataShareResponseDto.class)).thenReturn(mockResponseDto);

            DataShare result = dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID);

            assertNotNull(result);
            assertEquals("http://test-datashare.com", result.getUrl());
        }
    }

    /**
     * Tests data share creation when HttpClientErrorException is wrapped in RestClientException.
     * Verifies that ApiNotAccessibleException is thrown when client-side HTTP errors occur
     * during the REST API communication.
     */
    @Test
    void getDataShareWithHttpClientErrorExceptionShouldThrowApiNotAccessibleException() throws Exception {
        HttpClientErrorException clientException = new HttpClientErrorException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Bad Request");
        RestClientException wrapperException = new RestClientException("REST error", clientException);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenThrow(wrapperException);

            assertThrows(ApiNotAccessibleException.class, () ->
                    dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID)
            );
        }
    }

    /**
     * Tests data share creation when HttpServerErrorException is wrapped in RestClientException.
     * Verifies that ApiNotAccessibleException is thrown when server-side HTTP errors occur
     * during the REST API communication.
     */
    @Test
    void getDataShareWithHttpServerErrorExceptionShouldThrowApiNotAccessibleException() throws Exception {
        HttpServerErrorException serverException = new HttpServerErrorException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        RestClientException wrapperException = new RestClientException("REST error", serverException);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenThrow(wrapperException);

            assertThrows(ApiNotAccessibleException.class, () ->
                    dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID)
            );
        }
    }

    /**
     * Tests data share creation when generic runtime exception occurs.
     * Verifies that DataShareException is thrown when unexpected runtime errors occur
     * during the data share creation process.
     */
    @Test
    void getDataShareWithGenericExceptionShouldThrowDataShareException() throws Exception {
        RuntimeException genericException = new RuntimeException("Generic error");

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenThrow(genericException);

            assertThrows(DataShareException.class, () ->
                    dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID)
            );
        }
    }

    /**
     * Tests data share creation when IOException occurs during JSON parsing.
     * Verifies that DataShareException is thrown when JSON parsing fails during
     * response processing operations.
     */
    @Test
    void getDataShareWithIoExceptionShouldThrowDataShareException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);

            when(mapper.readValue(anyString(), eq(DataShareResponseDto.class)))
                    .thenThrow(new RuntimeException(new IOException("JSON parsing error")));

            assertThrows(DataShareException.class, () ->
                    dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID)
            );
        }
    }

    /**
     * Tests data share creation with null data parameter.
     * Verifies that DataShareException is thrown when null data is provided,
     * as ByteArrayResource constructor throws IllegalArgumentException for null data.
     */
    @Test
    void getDataShareWithNullDataShouldThrowDataShareException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            assertThrows(DataShareException.class, () ->
                    dataShareUtil.getDataShare(null, POLICY_ID, PARTNER_ID)
            );
        }
    }

    /**
     * Tests data share creation with empty data array.
     * Verifies that the method handles empty data gracefully and returns
     * a valid DataShare object when other parameters are correct.
     */
    @Test
    void getDataShareWithEmptyDataShouldSucceed() throws Exception {
        byte[] emptyData = new byte[0];
        mockResponseDto.setDataShare(mockDataShare);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);
            when(mapper.readValue(RESPONSE_STRING, DataShareResponseDto.class)).thenReturn(mockResponseDto);

            DataShare result = dataShareUtil.getDataShare(emptyData, POLICY_ID, PARTNER_ID);

            assertNotNull(result);
        }
    }

    /**
     * Tests data share creation with null policy ID parameter.
     * Verifies that the method handles null policy ID gracefully and still
     * creates a valid data share when other parameters are provided.
     */
    @Test
    void getDataShareWithNullPolicyIdShouldSucceed() throws Exception {
        mockResponseDto.setDataShare(mockDataShare);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);
            when(mapper.readValue(RESPONSE_STRING, DataShareResponseDto.class)).thenReturn(mockResponseDto);

            DataShare result = dataShareUtil.getDataShare(TEST_DATA, null, PARTNER_ID);

            assertNotNull(result);
        }
    }

    /**
     * Tests data share creation with null partner ID parameter.
     * Verifies that the method handles null partner ID gracefully and still
     * creates a valid data share when other parameters are provided.
     */
    @Test
    void getDataShareWithNullPartnerIdShouldSucceed() throws Exception {
        mockResponseDto.setDataShare(mockDataShare);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);
            when(mapper.readValue(RESPONSE_STRING, DataShareResponseDto.class)).thenReturn(mockResponseDto);

            DataShare result = dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, null);

            assertNotNull(result);
        }
    }

    /**
     * Tests ByteArrayResource getFilename method functionality.
     * Verifies that the method correctly processes data through ByteArrayResource
     * and returns the expected DataShare with proper URL configuration.
     */
    @Test
    void byteArrayResourceGetFilenameShouldProcessCorrectly() throws Exception {
        mockResponseDto.setDataShare(mockDataShare);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);
            when(mapper.readValue(RESPONSE_STRING, DataShareResponseDto.class)).thenReturn(mockResponseDto);

            DataShare result = dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID);

            assertNotNull(result);
            assertEquals("http://test-datashare.com", result.getUrl());
        }
    }

    /**
     * Tests data share creation when response has null errors.
     * Verifies that the method handles null error list gracefully and returns
     * a valid DataShare when the response is otherwise successful.
     */
    @Test
    void getDataShareWithNullErrorsShouldSucceed() throws Exception {
        mockResponseDto.setErrors(null);
        mockResponseDto.setDataShare(mockDataShare);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenReturn(RESPONSE_STRING);
            when(mapper.readValue(RESPONSE_STRING, DataShareResponseDto.class)).thenReturn(mockResponseDto);

            DataShare result = dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID);

            assertNotNull(result);
            assertEquals("http://test-datashare.com", result.getUrl());
        }
    }

    /**
     * Tests exception handling with direct HttpClientErrorException.
     * Verifies that DataShareException is thrown when HttpClientErrorException occurs
     * directly (not wrapped in RestClientException) during API communication.
     */
    @Test
    void getDataShareWithDirectHttpClientErrorExceptionShouldThrowDataShareException() throws Exception {
        HttpClientErrorException clientException = new HttpClientErrorException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Bad Request");

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenThrow(clientException);

            assertThrows(DataShareException.class, () ->
                    dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID)
            );
        }
    }

    /**
     * Tests exception handling with direct HttpServerErrorException.
     * Verifies that DataShareException is thrown when HttpServerErrorException occurs
     * directly (not wrapped in RestClientException) during API communication.
     */
    @Test
    void getDataShareWithDirectHttpServerErrorExceptionShouldThrowDataShareException() throws Exception {
        HttpServerErrorException serverException = new HttpServerErrorException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(DataShareUtil.class))
                    .thenReturn(mockLogger);

            when(restUtil.postApi(eq(ApiName.CREATEDATASHARE), anyList(), anyString(), anyString(),
                    any(HttpEntity.class), eq(String.class))).thenThrow(serverException);

            assertThrows(DataShareException.class, () ->
                    dataShareUtil.getDataShare(TEST_DATA, POLICY_ID, PARTNER_ID)
            );
        }
    }
}
