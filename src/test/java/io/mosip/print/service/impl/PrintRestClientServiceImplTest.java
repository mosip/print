package io.mosip.print.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;

import io.mosip.print.constant.ApiName;
import io.mosip.print.exception.ApisResourceAccessException;
import io.mosip.print.util.RestApiClient;

/**
 * Test class for PrintRestClientServiceImpl.
 * Tests REST client service operations including GET and POST API calls
 * with various parameter combinations and error handling scenarios.
 */
@RunWith(MockitoJUnitRunner.class)
public class PrintRestClientServiceImplTest {

    @InjectMocks
    private PrintRestClientServiceImpl printRestClientService;

    @Mock
    private RestApiClient restApiClient;

    @Mock
    private Environment env;

    private String apiHostIpPort;
    private List<String> pathSegments;
    private String queryParamName;
    private String queryParamValue;
    private List<String> queryParamNameList;
    private List<Object> queryParamValueList;
    private Object requestedData;
    private Object expectedResponse;
    private Class<?> responseType;

    /**
     * Sets up test data before each test method execution.
     * Initializes common test objects and mock responses.
     */
    @Before
    public void setUp() {
        apiHostIpPort = "http://localhost:8080/api";
        pathSegments = Arrays.asList("v1", "users");
        queryParamName = "status,type";
        queryParamValue = "active,admin";
        queryParamNameList = Arrays.asList("status", "type");
        queryParamValueList = Arrays.asList("active", "admin");
        requestedData = new Object();
        expectedResponse = new Object();
        responseType = Object.class;
    }

    /**
     * Tests GET API with path segments and query parameters.
     * Verifies successful API call with proper URI construction.
     */
    @Test
    public void getApiWithStringParamsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("MASTER")).thenReturn(apiHostIpPort);
        when(restApiClient.getApi(any(URI.class), eq(responseType))).thenReturn(expectedResponse);

        Object result = printRestClientService.getApi(ApiName.MASTER, pathSegments, queryParamName, queryParamValue, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).getApi(any(URI.class), eq(responseType));
    }

    /**
     * Tests GET API with list parameters.
     * Verifies successful API call with list-based query parameters.
     */
    @Test
    public void getApiWithListParamsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("IDREPOSITORY")).thenReturn(apiHostIpPort);
        when(restApiClient.getApi(any(URI.class), eq(responseType))).thenReturn(expectedResponse);

        Object result = printRestClientService.getApi(ApiName.IDREPOSITORY, pathSegments, queryParamNameList, queryParamValueList, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).getApi(any(URI.class), eq(responseType));
    }

    /**
     * Tests GET API with null API host configuration.
     * Verifies that null is returned when API host is not configured.
     */
    @Test
    public void getApiWithNullApiHostReturnsNull() throws Exception {
        when(env.getProperty("AUTH")).thenReturn(null);

        Object result = printRestClientService.getApi(ApiName.AUTH, pathSegments, queryParamName, queryParamValue, responseType);

        assertNull(result);
        verify(restApiClient, times(0)).getApi(any(URI.class), eq(responseType));
    }

    /**
     * Tests GET API with null path segments.
     * Verifies successful API call without path segments.
     */
    @Test
    public void getApiWithNullPathSegmentsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("TEMPLATES")).thenReturn(apiHostIpPort);
        when(restApiClient.getApi(any(URI.class), eq(responseType))).thenReturn(expectedResponse);

        Object result = printRestClientService.getApi(ApiName.TEMPLATES, null, queryParamName, queryParamValue, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).getApi(any(URI.class), eq(responseType));
    }

    /**
     * Tests GET API with empty path segments.
     * Verifies successful API call with empty path segments list.
     */
    @Test
    public void getApiWithEmptyPathSegmentsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("IDA")).thenReturn(apiHostIpPort);
        when(restApiClient.getApi(any(URI.class), eq(responseType))).thenReturn(expectedResponse);

        Object result = printRestClientService.getApi(ApiName.IDA, new ArrayList<>(), queryParamName, queryParamValue, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).getApi(any(URI.class), eq(responseType));
    }

    /**
     * Tests GET API with null query parameters.
     * Verifies successful API call without query parameters.
     */
    @Test
    public void getApiWithNullQueryParamsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("CRYPTOMANAGERDECRYPT")).thenReturn(apiHostIpPort);
        when(restApiClient.getApi(any(URI.class), eq(responseType))).thenReturn(expectedResponse);

        Object result = printRestClientService.getApi(ApiName.CRYPTOMANAGERDECRYPT, pathSegments, (String) null, null, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).getApi(any(URI.class), eq(responseType));
    }

    /**
     * Tests GET API with empty string query parameters.
     * Verifies successful API call with empty string query parameters.
     */
    @Test
    public void getApiWithEmptyStringQueryParamsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("RETRIEVEIDENTITY")).thenReturn(apiHostIpPort);
        when(restApiClient.getApi(any(URI.class), eq(responseType))).thenReturn(expectedResponse);

        Object result = printRestClientService.getApi(ApiName.RETRIEVEIDENTITY, pathSegments, "", "", responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).getApi(any(URI.class), eq(responseType));
    }

    /**
     * Tests GET API exception handling.
     * Verifies that ApisResourceAccessException is thrown when underlying API call fails.
     */
    @Test(expected = ApisResourceAccessException.class)
    public void getApiWithExceptionThrowsApisResourceAccessException() throws Exception {
        when(env.getProperty("DIGITALSIGNATURE")).thenReturn(apiHostIpPort);
        when(restApiClient.getApi(any(URI.class), eq(responseType))).thenThrow(new RuntimeException("API call failed"));

        printRestClientService.getApi(ApiName.DIGITALSIGNATURE, pathSegments, queryParamName, queryParamValue, responseType);
    }

    /**
     * Tests GET API with list parameters exception handling.
     * Verifies exception handling for list-based parameter API calls.
     */
    @Test(expected = ApisResourceAccessException.class)
    public void getApiWithListParamsExceptionThrowsApisResourceAccessException() throws Exception {
        when(env.getProperty("CREATEVID")).thenReturn(apiHostIpPort);
        when(restApiClient.getApi(any(URI.class), eq(responseType))).thenThrow(new RuntimeException("API call failed"));

        printRestClientService.getApi(ApiName.CREATEVID, pathSegments, queryParamNameList, queryParamValueList, responseType);
    }

    /**
     * Tests POST API with media type.
     * Verifies successful POST API call with specified media type.
     */
    @Test
    public void postApiWithMediaTypeReturnsExpectedResponse() throws Exception {
        when(env.getProperty("PDFSIGN")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq(MediaType.APPLICATION_JSON), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        Object result = printRestClientService.postApi(ApiName.PDFSIGN, queryParamName, queryParamValue, requestedData, responseType, MediaType.APPLICATION_JSON);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq(MediaType.APPLICATION_JSON), eq(requestedData), eq(responseType));
    }

    /**
     * Tests POST API without media type.
     * Verifies successful POST API call using default media type.
     */
    @Test
    public void postApiWithoutMediaTypeReturnsExpectedResponse() throws Exception {
        when(env.getProperty("ENCRYPTIONSERVICE")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        Object result = printRestClientService.postApi(ApiName.ENCRYPTIONSERVICE, queryParamName, queryParamValue, requestedData, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType));
    }

    /**
     * Tests POST API with path segments.
     * Verifies successful POST API call with path segments and query parameters.
     */
    @Test
    public void postApiWithPathSegmentsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("USERDETAILS")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        Object result = printRestClientService.postApi(ApiName.USERDETAILS, pathSegments, queryParamName, queryParamValue, requestedData, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType));
    }

    /**
     * Tests POST API with list parameters and media type.
     * Verifies successful POST API call with list-based parameters and media type.
     */
    @Test
    public void postApiWithListParamsAndMediaTypeReturnsExpectedResponse() throws Exception {
        when(env.getProperty("CREATEDATASHARE")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq(MediaType.APPLICATION_JSON), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        Object result = printRestClientService.postApi(ApiName.CREATEDATASHARE, MediaType.APPLICATION_JSON, pathSegments, queryParamNameList, queryParamValueList, requestedData, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq(MediaType.APPLICATION_JSON), eq(requestedData), eq(responseType));
    }

    /**
     * Tests POST API with null API host configuration.
     * Verifies that null is returned when API host is not configured.
     */
    @Test
    public void postApiWithNullApiHostReturnsNull() throws Exception {
        when(env.getProperty("AUDIT")).thenReturn(null);

        Object result = printRestClientService.postApi(ApiName.AUDIT, queryParamName, queryParamValue, requestedData, responseType, MediaType.APPLICATION_JSON);

        assertNull(result);
        verify(restApiClient, times(0)).postApi(anyString(), any(MediaType.class), any(), any(Class.class));
    }

    /**
     * Tests POST API with null query parameters.
     * Verifies successful POST API call without query parameters.
     */
    @Test
    public void postApiWithNullQueryParamsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("GETUINBYVID")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        Object result = printRestClientService.postApi(ApiName.GETUINBYVID, pathSegments, null, null, requestedData, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType));
    }

    /**
     * Tests POST API with empty query parameter lists.
     * Verifies successful POST API call with empty parameter lists.
     */
    @Test
    public void postApiWithEmptyQueryParamListsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("RIDGENERATION")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq(MediaType.APPLICATION_JSON), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        Object result = printRestClientService.postApi(ApiName.RIDGENERATION, MediaType.APPLICATION_JSON, pathSegments, new ArrayList<>(), new ArrayList<>(), requestedData, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq(MediaType.APPLICATION_JSON), eq(requestedData), eq(responseType));
    }

    /**
     * Tests POST API with null path segments.
     * Verifies successful POST API call without path segments.
     */
    @Test
    public void postApiWithNullPathSegmentsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("INTERNALAUTH")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        Object result = printRestClientService.postApi(ApiName.INTERNALAUTH, null, queryParamName, queryParamValue, requestedData, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType));
    }

    /**
     * Tests POST API with empty path segments.
     * Verifies successful POST API call with empty path segments list.
     */
    @Test
    public void postApiWithEmptyPathSegmentsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("DEVICEVALIDATEHISTORY")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq(MediaType.APPLICATION_JSON), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        Object result = printRestClientService.postApi(ApiName.DEVICEVALIDATEHISTORY, MediaType.APPLICATION_JSON, new ArrayList<>(), queryParamNameList, queryParamValueList, requestedData, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq(MediaType.APPLICATION_JSON), eq(requestedData), eq(responseType));
    }

    /**
     * Tests POST API with empty string query parameters.
     * Verifies successful POST API call with empty string query parameters.
     */
    @Test
    public void postApiWithEmptyStringQueryParamsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("IDSCHEMAURL")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        Object result = printRestClientService.postApi(ApiName.IDSCHEMAURL, pathSegments, "", "", requestedData, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType));
    }

    /**
     * Tests POST API exception handling.
     * Verifies that ApisResourceAccessException is thrown when underlying POST API call fails.
     */
    @Test(expected = ApisResourceAccessException.class)
    public void postApiWithExceptionThrowsApisResourceAccessException() throws Exception {
        when(env.getProperty("ENCRYPTURL")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), any(MediaType.class), eq(requestedData), eq(responseType)))
                .thenThrow(new RuntimeException("POST API call failed"));

        printRestClientService.postApi(ApiName.ENCRYPTURL, queryParamName, queryParamValue, requestedData, responseType, MediaType.APPLICATION_JSON);
    }

    /**
     * Tests POST API with path segments exception handling.
     * Verifies exception handling for POST API calls with path segments.
     */
    @Test(expected = ApisResourceAccessException.class)
    public void postApiWithPathSegmentsExceptionThrowsApisResourceAccessException() throws Exception {
        when(env.getProperty("IDAUTHENCRYPTION")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType)))
                .thenThrow(new RuntimeException("POST API call failed"));

        printRestClientService.postApi(ApiName.IDAUTHENCRYPTION, pathSegments, queryParamName, queryParamValue, requestedData, responseType);
    }

    /**
     * Tests POST API with list parameters exception handling.
     * Verifies exception handling for POST API calls with list-based parameters.
     */
    @Test(expected = ApisResourceAccessException.class)
    public void postApiWithListParamsExceptionThrowsApisResourceAccessException() throws Exception {
        when(env.getProperty("REVERSEDATASYNC")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq(MediaType.APPLICATION_JSON), eq(requestedData), eq(responseType)))
                .thenThrow(new RuntimeException("POST API call failed"));

        printRestClientService.postApi(ApiName.REVERSEDATASYNC, MediaType.APPLICATION_JSON, pathSegments, queryParamNameList, queryParamValueList, requestedData, responseType);
    }

    /**
     * Tests GET API with pathSegments containing null and empty elements.
     * Verifies that null and empty path segments are properly filtered out.
     */
    @Test
    public void getApiWithNullAndEmptyPathSegmentsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("NGINXDMZURL")).thenReturn(apiHostIpPort);
        when(restApiClient.getApi(any(URI.class), eq(responseType))).thenReturn(expectedResponse);

        List<String> pathSegmentsWithNullAndEmpty = Arrays.asList("v1", null, "", "users", null);
        Object result = printRestClientService.getApi(ApiName.NGINXDMZURL, pathSegmentsWithNullAndEmpty, queryParamName, queryParamValue, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).getApi(any(URI.class), eq(responseType));
    }

    /**
     * Tests POST API with pathSegments containing null and empty elements.
     * Verifies that null and empty path segments are properly filtered out in POST calls.
     */
    @Test
    public void postApiWithNullAndEmptyPathSegmentsReturnsExpectedResponse() throws Exception {
        when(env.getProperty("REGISTRATIONCONNECTOR")).thenReturn(apiHostIpPort);
        when(restApiClient.postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType)))
                .thenReturn(expectedResponse);

        List<String> pathSegmentsWithNullAndEmpty = Arrays.asList("api", null, "", "register", null);
        Object result = printRestClientService.postApi(ApiName.REGISTRATIONCONNECTOR, pathSegmentsWithNullAndEmpty, queryParamName, queryParamValue, requestedData, responseType);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(restApiClient, times(1)).postApi(anyString(), eq((MediaType) null), eq(requestedData), eq(responseType));
    }
}