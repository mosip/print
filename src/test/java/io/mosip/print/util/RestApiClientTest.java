package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.print.logger.PrintLogger;

/**
 * Unit tests for {@link RestApiClient} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the RestApiClient class,
 * including GET and POST API calls, exception handling, HTTP header management, different media types,
 * and various request/response scenarios for REST client operations.</p>
 */
@ExtendWith(MockitoExtension.class)
class RestApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Environment environment;

    @InjectMocks
    private RestApiClient restApiClient;

    private static final String TEST_URL = "http://test-url.com";
    private static final String TEST_RESPONSE = "test-response";

    /**
     * Tests successful GET API call with URI parameter.
     * Verifies that the method correctly performs GET requests using URI and returns
     * the expected response when the REST call succeeds.
     */
    @Test
    void getApiWithUriShouldSucceedWithValidRequest() throws Exception {
        URI testUri = URI.create(TEST_URL);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(TEST_RESPONSE, HttpStatus.OK);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.exchange(eq(testUri), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(responseEntity);

            String result = restApiClient.getApi(testUri, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests GET API call with URI when exception occurs.
     * Verifies that the method handles RestClientException gracefully and returns null
     * when the REST call fails.
     */
    @Test
    void getApiWithUriShouldReturnNullWhenExceptionOccurs() throws Exception {
        URI testUri = URI.create(TEST_URL);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.exchange(eq(testUri), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                    .thenThrow(new RestClientException("REST client error"));

            String result = restApiClient.getApi(testUri, String.class);

            assertNull(result);
        }
    }

    /**
     * Tests successful GET API call with String URL parameter.
     * Verifies that the method correctly performs GET requests using string URL and returns
     * the expected response when the REST call succeeds.
     */
    @Test
    void getApiWithStringUrlShouldSucceedWithValidRequest() {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.getForObject(TEST_URL, String.class)).thenReturn(TEST_RESPONSE);

            String result = restApiClient.getApi(TEST_URL, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests GET API call with String URL when exception occurs.
     * Verifies that the method handles RestClientException gracefully and returns null
     * when the REST call with string URL fails.
     */
    @Test
    void getApiWithStringUrlShouldReturnNullWhenExceptionOccurs() {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.getForObject(TEST_URL, String.class))
                    .thenThrow(new RestClientException("REST client error"));

            String result = restApiClient.getApi(TEST_URL, String.class);

            assertNull(result);
        }
    }

    /**
     * Tests successful POST API call with request data.
     * Verifies that the method correctly performs POST requests with JSON media type
     * and returns the expected response when the REST call succeeds.
     */
    @Test
    void postApiShouldSucceedWithValidRequest() throws Exception {
        Object requestData = "test-request";

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, requestData, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests POST API call when exception occurs during execution.
     * Verifies that the method handles RestClientException gracefully and returns null
     * when the POST request fails.
     */
    @Test
    void postApiShouldReturnNullWhenExceptionOccurs() throws Exception {
        Object requestData = "test-request";

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenThrow(new RestClientException("POST request failed"));

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, requestData, String.class);

            assertNull(result);
        }
    }

    /**
     * Tests POST API call with null media type parameter.
     * Verifies that the method handles null media type gracefully and still processes
     * the request successfully.
     */
    @Test
    void postApiWithNullMediaTypeShouldSucceed() throws Exception {
        Object requestData = "test-request";

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, null, requestData, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests POST API call with null request type parameter.
     * Verifies that the method handles null request data gracefully and still processes
     * the request successfully.
     */
    @Test
    void postApiWithNullRequestTypeShouldSucceed() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, null, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests setRequestHeader functionality with both null parameters.
     * Verifies that the method handles null media type and null request data gracefully
     * in the header setting process.
     */
    @Test
    void setRequestHeaderWithNullParametersShouldHandleGracefully() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, null, null, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests setRequestHeader functionality with HttpEntity request type.
     * Verifies that the method properly handles HttpEntity objects and preserves
     * custom headers while adding content type.
     */
    @Test
    void setRequestHeaderWithHttpEntityShouldPreserveHeaders() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Custom-Header", "custom-value");
        headers.add("Authorization", "Bearer token");
        HttpEntity<String> httpEntity = new HttpEntity<>("test-body", headers);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, httpEntity, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests setRequestHeader with HttpEntity when Content-Type already exists.
     * Verifies that the method respects existing Content-Type headers and does not
     * override them when they are already present.
     */
    @Test
    void setRequestHeaderWithHttpEntityContentTypeExistsShouldRespectExisting() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/xml");
        headers.add("Authorization", "Bearer token");
        HttpEntity<String> httpEntity = new HttpEntity<>("test-body", headers);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, httpEntity, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests setRequestHeader with HttpEntity having null header values.
     * Verifies that the method handles null header values gracefully without
     * causing exceptions during header processing.
     */
    @Test
    void setRequestHeaderWithHttpEntityNullValuesShouldHandleGracefully() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Valid-Header", "valid-value");
        headers.put("Null-Header", null);
        HttpEntity<String> httpEntity = new HttpEntity<>("test-body", headers);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, httpEntity, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests setRequestHeader with HttpEntity having empty header values.
     * Verifies that the method handles empty header value lists gracefully
     * during header processing operations.
     */
    @Test
    void setRequestHeaderWithHttpEntityEmptyValuesShouldHandleGracefully() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Valid-Header", "valid-value");
        headers.put("Empty-Header", Arrays.asList());
        HttpEntity<String> httpEntity = new HttpEntity<>("test-body", headers);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, httpEntity, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests setRequestHeader functionality when ClassCastException occurs.
     * Verifies that the method handles ClassCastException gracefully when the request
     * object cannot be cast to HttpEntity.
     */
    @Test
    void setRequestHeaderWithClassCastExceptionShouldHandleGracefully() throws Exception {
        Object nonHttpEntity = "not-an-http-entity";

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, nonHttpEntity, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests setRequestHeader with HttpEntity having null body.
     * Verifies that the method handles HttpEntity objects with null body content
     * gracefully while preserving header information.
     */
    @Test
    void setRequestHeaderWithNullHttpEntityBodyShouldHandleGracefully() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Custom-Header", "custom-value");
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, httpEntity, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests setRequestHeader with multiple header values.
     * Verifies that the method correctly processes headers with multiple values
     * and handles both single and multi-value header scenarios.
     */
    @Test
    void setRequestHeaderWithMultipleHeaderValuesShouldProcessCorrectly() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Multi-Value-Header", Arrays.asList("value1", "value2", "value3"));
        headers.add("Single-Value-Header", "single-value");
        HttpEntity<String> httpEntity = new HttpEntity<>("test-body", headers);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_JSON, httpEntity, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }

    /**
     * Tests GET API call with different response types.
     * Verifies that the method can handle various response types beyond String,
     * such as Integer, and properly deserialize the response.
     */
    @Test
    void getApiWithDifferentResponseTypesShouldHandleVariousTypes() throws Exception {
        URI testUri = URI.create(TEST_URL);
        Integer intResponse = 42;
        ResponseEntity<Integer> responseEntity = new ResponseEntity<>(intResponse, HttpStatus.OK);

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.exchange(eq(testUri), eq(HttpMethod.GET), any(HttpEntity.class), eq(Integer.class)))
                    .thenReturn(responseEntity);

            Integer result = restApiClient.getApi(testUri, Integer.class);

            assertNotNull(result);
            assertEquals(intResponse, result);
        }
    }

    /**
     * Tests POST API call with different media types.
     * Verifies that the method correctly handles various media types beyond JSON,
     * such as XML, for content type specification.
     */
    @Test
    void postApiWithDifferentMediaTypesShouldHandleVariousTypes() throws Exception {
        Object requestData = "test-request";

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(RestApiClient.class))
                    .thenReturn(mockLogger);

            when(restTemplate.postForObject(eq(TEST_URL), any(HttpEntity.class), eq(String.class)))
                    .thenReturn(TEST_RESPONSE);

            String result = restApiClient.postApi(TEST_URL, MediaType.APPLICATION_XML, requestData, String.class);

            assertNotNull(result);
            assertEquals(TEST_RESPONSE, result);
        }
    }
}