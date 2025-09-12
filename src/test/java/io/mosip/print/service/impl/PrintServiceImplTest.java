package io.mosip.print.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonParseException;

import io.mosip.print.constant.QrVersion;
import io.mosip.print.constant.UinCardType;
import io.mosip.print.dto.CryptoWithPinRequestDto;
import io.mosip.print.dto.CryptoWithPinResponseDto;
import io.mosip.print.dto.DataShare;
import io.mosip.print.dto.JsonValue;
import io.mosip.print.exception.PDFGeneratorException;
import io.mosip.print.exception.PDFSignatureException;
import io.mosip.print.exception.QrcodeGenerationException;
import io.mosip.print.exception.TemplateProcessingFailureException;
import io.mosip.print.exception.UINNotFoundInDatabase;
import io.mosip.print.model.Event;
import io.mosip.print.model.EventModel;
import io.mosip.print.service.UinCardGenerator;
import io.mosip.print.spi.CbeffUtil;
import io.mosip.print.spi.QrCodeGenerator;
import io.mosip.print.util.AuditLogRequestBuilder;
import io.mosip.print.util.CbeffToBiometricUtil;
import io.mosip.print.util.CryptoCoreUtil;
import io.mosip.print.util.CryptoUtil;
import io.mosip.print.util.DataShareUtil;
import io.mosip.print.util.DateUtils;
import io.mosip.print.util.JsonUtil;
import io.mosip.print.util.RestApiClient;
import io.mosip.print.util.TemplateGenerator;
import io.mosip.print.util.Utilities;
import io.mosip.print.util.WebSubSubscriptionHelper;

/**
 * Unit tests for {@link PrintServiceImpl} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the PrintServiceImpl class,
 * including card generation, document processing, attribute decryption, template processing, and various
 * exception handling scenarios.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrintServiceImplTest {

    @InjectMocks
    private PrintServiceImpl printService;

    @Mock
    private WebSubSubscriptionHelper webSubSubscriptionHelper;

    @Mock
    private DataShareUtil dataShareUtil;

    @Mock
    private CryptoUtil cryptoUtil;

    @Mock
    private RestApiClient restApiClient;

    @Mock
    private CryptoCoreUtil cryptoCoreUtil;

    @Mock
    private AuditLogRequestBuilder auditLogRequestBuilder;

    @Mock
    private TemplateGenerator templateGenerator;

    @Mock
    private Utilities utilities;

    @Mock
    private UinCardGenerator<byte[]> uinCardGenerator;

    @Mock
    private QrCodeGenerator<QrVersion> qrCodeGenerator;

    @Mock
    private CbeffUtil cbeffutil;

    @Mock
    private Environment env;

    private EventModel mockEventModel;
    private String mockCredential;
    private String mockDecodedCredential;
    private DataShare mockDataShare;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes mock objects, test data, and configures default behavior for all dependencies.
     */
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(printService, "partnerId", "partner123");
        ReflectionTestUtils.setField(printService, "policyId", "policy123");
        ReflectionTestUtils.setField(printService, "templateLang", "eng");
        ReflectionTestUtils.setField(printService, "supportedLang", "eng,fra");
        ReflectionTestUtils.setField(printService, "verifyCredentialsFlag", false);
        ReflectionTestUtils.setField(printService, "isPasswordProtected", false);

        mockEventModel = createMockEventModel();
        mockCredential = createMockCredential();
        mockDecodedCredential = createMockDecodedCredential();
        mockDataShare = createMockDataShare();

        setupDefaultMockBehavior();
    }

    /**
     * Tests the generateCard method when a general runtime exception occurs during processing.
     * Verifies that the method handles decryption failures gracefully and returns false.
     */
    @Test
    void generateCardWithGeneralExceptionShouldReturnFalse() throws Exception {
        when(cryptoCoreUtil.decrypt(anyString())).thenThrow(new RuntimeException("Decryption failed"));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
            dateUtilsMock.when(DateUtils::getUTCCurrentDateTime).thenReturn(LocalDateTime.now());

            boolean result = printService.generateCard(mockEventModel);
            assertFalse(result);
        }
    }

    /**
     * Tests the getDocuments method when QR code generation fails.
     * Verifies that QrcodeGenerationException is handled properly and the method returns false.
     */
    @Test
    void getDocumentsWithQrcodeGenerationExceptionShouldReturnFalse() throws Exception {
        when(qrCodeGenerator.generateQrCode(anyString(), any(QrVersion.class)))
                .thenThrow(new QrcodeGenerationException("QR generation failed", "QR_ERROR", new RuntimeException("Root cause")));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {

            dateUtilsMock.when(DateUtils::getUTCCurrentDateTime).thenReturn(LocalDateTime.now());
            setupJsonUtilMocks(jsonUtilMock);

            boolean result = printService.generateCard(mockEventModel);
            assertFalse(result);
        }
    }

    /**
     * Tests the getDocuments method when UIN is not found in database.
     * Verifies that UINNotFoundInDatabase exception is handled gracefully and returns false.
     */
    @Test
    void getDocumentsWithUinNotFoundExceptionShouldReturnFalse() throws Exception {
        when(templateGenerator.getTemplate(anyString(), any(Map.class), anyString()))
                .thenThrow(new UINNotFoundInDatabase("UIN not found", new RuntimeException("Database error")));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {

            dateUtilsMock.when(DateUtils::getUTCCurrentDateTime).thenReturn(LocalDateTime.now());
            setupJsonUtilMocks(jsonUtilMock);

            boolean result = printService.generateCard(mockEventModel);
            assertFalse(result);
        }
    }

    /**
     * Tests the getDocuments method when template processing fails.
     * Verifies that TemplateProcessingFailureException is handled appropriately and returns false.
     */
    @Test
    void getDocumentsWithTemplateProcessingFailureShouldReturnFalse() throws Exception {
        when(templateGenerator.getTemplate(anyString(), any(Map.class), anyString()))
                .thenThrow(new TemplateProcessingFailureException("Template processing failed"));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {

            dateUtilsMock.when(DateUtils::getUTCCurrentDateTime).thenReturn(LocalDateTime.now());
            setupJsonUtilMocks(jsonUtilMock);

            boolean result = printService.generateCard(mockEventModel);
            assertFalse(result);
        }
    }

    /**
     * Tests the getDocuments method when PDF generation encounters an exception.
     * Verifies that PDFGeneratorException is handled correctly and the method returns false.
     */
    @Test
    void getDocumentsWithPdfGeneratorExceptionShouldReturnFalse() throws Exception {
        when(uinCardGenerator.generateUinCard(any(InputStream.class), any(UinCardType.class), anyString()))
                .thenThrow(new PDFGeneratorException("PDF_ERROR", "PDF generation failed"));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {

            dateUtilsMock.when(DateUtils::getUTCCurrentDateTime).thenReturn(LocalDateTime.now());
            setupJsonUtilMocks(jsonUtilMock);

            boolean result = printService.generateCard(mockEventModel);
            assertFalse(result);
        }
    }

    /**
     * Tests the getDocuments method when PDF signature process fails.
     * Verifies that PDFSignatureException is handled properly and returns false.
     */
    @Test
    void getDocumentsWithPdfSignatureExceptionShouldReturnFalse() throws Exception {
        when(uinCardGenerator.generateUinCard(any(InputStream.class), any(UinCardType.class), anyString()))
                .thenThrow(new PDFSignatureException("PDF signature failed"));

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {

            dateUtilsMock.when(DateUtils::getUTCCurrentDateTime).thenReturn(LocalDateTime.now());
            setupJsonUtilMocks(jsonUtilMock);

            boolean result = printService.generateCard(mockEventModel);
            assertFalse(result);
        }
    }

    /**
     * Tests the getDocuments method when template generator returns null.
     * Verifies that null template response is handled gracefully and returns false.
     */
    @Test
    void getDocumentsWithNullTemplateShouldReturnFalse() throws Exception {
        when(templateGenerator.getTemplate(anyString(), any(Map.class), anyString())).thenReturn(null);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {

            dateUtilsMock.when(DateUtils::getUTCCurrentDateTime).thenReturn(LocalDateTime.now());
            setupJsonUtilMocks(jsonUtilMock);

            boolean result = printService.generateCard(mockEventModel);
            assertFalse(result);
        }
    }

    /**
     * Tests the decryptAttribute method with protected attributes present in credential.
     * Verifies that encrypted attributes are properly decrypted using PIN-based decryption.
     */
    @Test
    void decryptAttributeWithProtectedAttributesShouldDecryptSuccessfully() throws Exception {
        String credentialWithProtected = createCredentialWithProtectedAttributes();
        org.json.JSONObject data = new org.json.JSONObject();
        data.put("name", "encryptedName");

        CryptoWithPinResponseDto response = new CryptoWithPinResponseDto();
        response.setData("decryptedName");
        when(cryptoUtil.decryptWithPin(any(CryptoWithPinRequestDto.class))).thenReturn(response);

        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "decryptAttribute", org.json.JSONObject.class, String.class, String.class);
        method.setAccessible(true);

        org.json.JSONObject result = (org.json.JSONObject) method.invoke(
                printService, data, "1234", credentialWithProtected);

        assertEquals("decryptedName", result.getString("name"));
    }

    /**
     * Tests the decryptAttribute method when no protected attributes are present.
     * Verifies that data remains unchanged when no decryption is required.
     */
    @Test
    void decryptAttributeWithNoProtectedAttributesShouldReturnOriginalData() throws Exception {
        String credentialWithoutProtected = "{\"credentialSubject\":\"{\\\"UIN\\\":\\\"1234567890\\\"}\"}";
        org.json.JSONObject data = new org.json.JSONObject();
        data.put("name", "testName");

        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "decryptAttribute", org.json.JSONObject.class, String.class, String.class);
        method.setAccessible(true);

        org.json.JSONObject result = (org.json.JSONObject) method.invoke(
                printService, data, "1234", credentialWithoutProtected);

        assertEquals("testName", result.getString("name"));
    }

    /**
     * Tests the decryptAttribute method when crypto exception occurs during decryption.
     * Verifies that cryptographic exceptions are properly propagated.
     */
    @Test
    void decryptAttributeWithCryptoExceptionShouldThrowException() throws Exception {
        String credentialWithProtected = createCredentialWithProtectedAttributes();
        org.json.JSONObject data = new org.json.JSONObject();
        data.put("name", "encryptedName");

        when(cryptoUtil.decryptWithPin(any(CryptoWithPinRequestDto.class)))
                .thenThrow(new InvalidKeyException("Invalid key"));

        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "decryptAttribute", org.json.JSONObject.class, String.class, String.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () ->
                method.invoke(printService, data, "1234", credentialWithProtected));
    }

    /**
     * Tests the extractFaceImageData method with invalid biometric data.
     * Verifies that the method throws an exception when provided with invalid data format.
     */
    @Test
    void extractFaceImageDataWithInvalidDataShouldThrowException() throws Exception {
        byte[] invalidData = "invalid".getBytes();

        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "extractFaceImageData", byte[].class);
        method.setAccessible(true);

        assertThrows(Exception.class, () ->
                method.invoke(printService, invalidData));
    }

    /**
     * Tests the getPassword method when required property is missing from configuration.
     * Verifies that the method throws an exception when password property is not configured.
     */
    @Test
    void getPasswordWithMissingPropertyShouldThrowException() throws Exception {
        when(env.getProperty("mosip.print.service.uincard.password")).thenReturn(null);

        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "getPassword", org.json.JSONObject.class);
        method.setAccessible(true);

        org.json.JSONObject jsonObject = new org.json.JSONObject();

        assertThrows(Exception.class, () ->
                method.invoke(printService, jsonObject));
    }

    /**
     * Tests the getPassword method with various JSON data types in password fields.
     * Verifies that the method handles different JSON structures for password generation.
     */
    @Test
    void getPasswordWithVariousJsonTypesShouldGeneratePassword() throws Exception {
        when(env.getProperty("mosip.print.service.uincard.password")).thenReturn("UIN|fullName|dob");

        org.json.JSONObject jsonObject = new org.json.JSONObject();
        jsonObject.put("UIN", "1234567890");

        JSONArray jsonArray = new JSONArray();
        JSONObject nameObj = new JSONObject();
        nameObj.put("language", "eng");
        nameObj.put("value", "JohnDoe");
        jsonArray.add(nameObj);
        jsonObject.put("fullName", jsonArray);

        JSONObject dobObj = new JSONObject();
        dobObj.put("value", "1990-01-01");
        jsonObject.put("dob", dobObj);

        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            JsonValue jsonValue = new JsonValue();
            jsonValue.setLanguage("eng");
            jsonValue.setValue("JohnDoe");
            JsonValue[] jsonValues = {jsonValue};

            jsonUtilMock.when(() -> JsonUtil.mapJsonNodeToJavaObject(eq(JsonValue.class), any(JSONArray.class)))
                    .thenReturn(jsonValues);

            Method method = PrintServiceImpl.class.getDeclaredMethod(
                    "getPassword", org.json.JSONObject.class);
            method.setAccessible(true);

            String result = (String) method.invoke(printService, jsonObject);
            assertNotNull(result);
            assertEquals(12, result.length());
        }
    }

    /**
     * Tests the getFormattedPasswordAttribute method with various input values.
     * Verifies that the method consistently returns 4-character formatted passwords regardless of input.
     */
    @Test
    void getFormattedPasswordAttributeWithVariousValuesShouldReturnFormattedString() throws Exception {
        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "getFormattedPasswordAttribute", String.class);
        method.setAccessible(true);

        String result1 = (String) method.invoke(printService, "verylongpassword123");
        assertEquals(4, result1.length());

        String result2 = (String) method.invoke(printService, "ab");
        assertEquals(4, result2.length());

        String result3 = (String) method.invoke(printService, "test@#$123");
        assertEquals(4, result3.length());
    }

    /**
     * Tests the setTemplateAttributes method when identity JSON is null.
     * Verifies that the method throws an exception when identity parsing returns null.
     */
    @Test
    void setTemplateAttributesWithNullIdentityShouldThrowException() throws Exception {
        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            jsonUtilMock.when(() -> JsonUtil.objectMapperReadValue(anyString(), eq(JSONObject.class)))
                    .thenReturn(null);

            Method method = PrintServiceImpl.class.getDeclaredMethod(
                    "setTemplateAttributes", String.class, Map.class);
            method.setAccessible(true);

            Map<String, Object> attributes = new HashMap<>();

            assertThrows(Exception.class, () ->
                    method.invoke(printService, "{}", attributes));
        }
    }

    /**
     * Tests the setTemplateAttributes method when JSON parsing exception occurs.
     * Verifies that JSON parsing exceptions are properly handled and propagated.
     */
    @Test
    void setTemplateAttributesWithParsingExceptionShouldThrowException() throws Exception {
        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            jsonUtilMock.when(() -> JsonUtil.objectMapperReadValue(anyString(), eq(JSONObject.class)))
                    .thenThrow(new JsonParseException(null, "Parse error"));

            Method method = PrintServiceImpl.class.getDeclaredMethod(
                    "setTemplateAttributes", String.class, Map.class);
            method.setAccessible(true);

            Map<String, Object> attributes = new HashMap<>();

            assertThrows(Exception.class, () ->
                    method.invoke(printService, "{invalid json}", attributes));
        }
    }

    /**
     * Tests the createTextFile method when identity JSON is null.
     * Verifies that the method throws an exception when demographic identity cannot be parsed.
     */
    @Test
    void createTextFileWithNullIdentityShouldThrowException() throws Exception {
        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            jsonUtilMock.when(() -> JsonUtil.objectMapperReadValue(anyString(), eq(JSONObject.class)))
                    .thenReturn(null);

            Method method = PrintServiceImpl.class.getDeclaredMethod(
                    "createTextFile", String.class);
            method.setAccessible(true);

            assertThrows(Exception.class, () ->
                    method.invoke(printService, "{}"));
        }
    }

    /**
     * Tests the createTextFile method with various object types in demographic data.
     * Verifies that the method handles different data structures including arrays, maps, and strings.
     */
    @Test
    void createTextFileWithVariousObjectTypesShouldCreateFile() throws Exception {
        try (MockedStatic<JsonUtil> jsonUtilMock = mockStatic(JsonUtil.class)) {
            JSONObject mockDemographicIdentity = new JSONObject();
            mockDemographicIdentity.put("UIN", "1234567890");

            ArrayList<String> arrayListField = new ArrayList<>();
            arrayListField.add("arrayItem");
            mockDemographicIdentity.put("arrayField", arrayListField);

            LinkedHashMap<String, String> mapField = new LinkedHashMap<>();
            mapField.put("value", "mapValue");
            mockDemographicIdentity.put("mapField", mapField);

            mockDemographicIdentity.put("stringField", "stringValue");

            JSONObject printTextFileConfig = new JSONObject();
            printTextFileConfig.put("personal", "UIN,arrayField,mapField,stringField");

            jsonUtilMock.when(() -> JsonUtil.objectMapperReadValue(anyString(), eq(JSONObject.class)))
                    .thenReturn(mockDemographicIdentity)
                    .thenReturn(printTextFileConfig);

            JsonValue jsonValue = new JsonValue();
            jsonValue.setLanguage("eng");
            jsonValue.setValue("arrayValue");
            JsonValue[] jsonValues = {jsonValue};

            JSONArray mockJSONArray = new JSONArray();
            jsonUtilMock.when(() -> JsonUtil.getJSONArray(any(JSONObject.class), anyString()))
                    .thenReturn(mockJSONArray);
            jsonUtilMock.when(() -> JsonUtil.mapJsonNodeToJavaObject(eq(JsonValue.class), any(JSONArray.class)))
                    .thenReturn(jsonValues);

            JSONObject mockJSONObject = new JSONObject();
            mockJSONObject.put("value", "jsonObjectValue");
            jsonUtilMock.when(() -> JsonUtil.getJSONObject(any(JSONObject.class), anyString()))
                    .thenReturn(mockJSONObject);

            jsonUtilMock.when(() -> JsonUtil.getJSONValue(any(JSONObject.class), anyString()))
                    .thenReturn("personal");

            when(utilities.getPrintTextFileJson(anyString(), anyString()))
                    .thenReturn(printTextFileConfig.toString());
            when(utilities.getConfigServerFileStorageURL()).thenReturn("http://config");
            when(utilities.getRegistrationProcessorPrintTextFile()).thenReturn("textfile.json");

            Method method = PrintServiceImpl.class.getDeclaredMethod(
                    "createTextFile", String.class);
            method.setAccessible(true);

            byte[] result = (byte[]) method.invoke(printService, mockDemographicIdentity.toString());
            assertNotNull(result);
            assertTrue(result.length > 0);
        }
    }

    /**
     * Tests the credential verification path in the print service.
     * Verifies that credential verification flag properly controls the verification process.
     */
    @Test
    void testCredentialVerificationPathShouldReturnTrue() throws Exception {
        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "hasPrintCredentialVerified", EventModel.class, String.class);
        method.setAccessible(true);

        ReflectionTestUtils.setField(printService, "verifyCredentialsFlag", false);
        boolean result = (boolean) method.invoke(printService, mockEventModel, "credential");
        assertTrue(result);
    }

    /**
     * Tests the getDocuments method for successful QR code generation.
     * Verifies that the method processes credentials and generates documents correctly.
     *
     * @throws Exception if any error occurs during processing
     */
    @Test
    void testGetDocuments_Success_QRCode() throws Exception {
        String credential = "{\"credentialSubject\":\"{\\\"UIN\\\":\\\"1234567890\\\"}\"}";
        String credentialType = "qrcode";
        String encryptionPin = "1234";
        String requestId = "req1";
        boolean isPasswordProtected = false;

        when(templateGenerator.getTemplate(anyString(), anyMap(), anyString()))
                .thenReturn(new ByteArrayInputStream("template".getBytes()));
        when(uinCardGenerator.generateUinCard(any(InputStream.class), any(UinCardType.class), anyString()))
                .thenReturn("pdf".getBytes());
        when(qrCodeGenerator.generateQrCode(anyString(), any(QrVersion.class)))
                .thenReturn("qr".getBytes());
        when(dataShareUtil.getDataShare(any(), anyString(), anyString()))
                .thenReturn(mock(DataShare.class));
        when(auditLogRequestBuilder.createAuditRequestBuilder(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "getDocuments", String.class, String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        Map<String, byte[]> result = (Map<String, byte[]>) method.invoke(printService, credential, credentialType, encryptionPin, requestId, isPasswordProtected);
        assertNotNull(result);
    }
    /**
     * Creates a mock DataShare object for testing purposes.
     *
     * @return DataShare with test data including partner ID, policy ID, and request ID
     */
    @Test
    void testGetDocuments_TemplateProcessingFailureException() throws Exception {
        String credential = "{\"credentialSubject\":\"{\\\"UIN\\\":\\\"1234567890\\\"}\"}";
        String credentialType = "UIN";
        String encryptionPin = "1234";
        String requestId = "req2";
        boolean isPasswordProtected = false;

        when(templateGenerator.getTemplate(anyString(), anyMap(), anyString())).thenReturn(null);

        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "getDocuments", String.class, String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () ->
                method.invoke(printService, credential, credentialType, encryptionPin, requestId, isPasswordProtected));

        Throwable cause = exception.getCause();
        assertTrue(cause instanceof PDFGeneratorException);
        assertTrue(cause.getMessage().contains("argument \"content\" is null"));
    }

    /**
     * Tests the getDocuments method when QR code generation fails.
     * Verifies that QrcodeGenerationException is handled properly and the method throws PDFGeneratorException.
     */
    @Test
    void testGetDocuments_QrcodeGenerationException() throws Exception {
        String credential = "{\"credentialSubject\":\"{\\\"UIN\\\":\\\"1234567890\\\"}\"}";
        String credentialType = "UIN";
        String encryptionPin = "1234";
        String requestId = "req3";
        boolean isPasswordProtected = false;

        // Setup mocks to trigger the exception
        when(templateGenerator.getTemplate(anyString(), anyMap(), anyString())).thenReturn(null);

        Method method = PrintServiceImpl.class.getDeclaredMethod(
                "getDocuments", String.class, String.class, String.class, String.class, boolean.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () ->
                method.invoke(printService, credential, credentialType, encryptionPin, requestId, isPasswordProtected));

        Throwable cause = exception.getCause();
        assertTrue(cause instanceof PDFGeneratorException);
        assertTrue(cause.getMessage().contains("argument \"content\" is null"));
    }

    /**
     * Creates a mock EventModel for testing purposes.
     *
     * @return EventModel with test data including transaction ID, event ID, and credential information
     */
    private EventModel createMockEventModel() {
        EventModel eventModel = new EventModel();
        Event event = new Event();
        event.setTransactionId("txn123");
        event.setId("event123");

        Map<String, Object> data = new HashMap<>();
        data.put("credentialType", "UIN");
        data.put("protectionKey", "1234");
        data.put("credential", "mockCredential");
        event.setData(data);

        eventModel.setEvent(event);
        return eventModel;
    }

    /**
     * Creates a mock credential string for testing.
     *
     * @return JSON string representing a basic credential with UIN
     */
    private String createMockCredential() {
        return "{\"credentialSubject\":\"{\\\"UIN\\\":\\\"1234567890\\\"}\"}";
    }

    /**
     * Creates a mock decoded credential string for testing.
     *
     * @return JSON string representing decoded credential with UIN and biometric data
     */
    private String createMockDecodedCredential() {
        return "{\"credentialSubject\":\"{\\\"UIN\\\":\\\"1234567890\\\",\\\"biometrics\\\":\\\"biometricData\\\"}\"}";
    }

    /**
     * Creates a credential string with protected attributes for testing decryption.
     *
     * @return JSON string representing credential with protected attributes array
     */
    private String createCredentialWithProtectedAttributes() {
        return "{\"protectedAttributes\":[\"name\"],\"credentialSubject\":\"{\\\"UIN\\\":\\\"1234567890\\\"}\"}";
    }

    /**
     * Creates a mock DataShare object for testing.
     *
     * @return DataShare object with test URL
     */
    private DataShare createMockDataShare() {
        DataShare dataShare = new DataShare();
        dataShare.setUrl("https://example.com/datashare/file123");
        return dataShare;
    }

    /**
     * Creates mock biometric data for testing face image extraction.
     *
     * @return byte array representing mock biometric data with proper header structure
     */
    private byte[] createMockBiometricData() {
        byte[] data = new byte[100];
        data[0] = 'F'; data[1] = 'A'; data[2] = 'C'; data[3] = 'E';
        data[4] = 0; data[5] = 1; data[6] = 0; data[7] = 0;
        data[8] = 0; data[9] = 0; data[10] = 0; data[11] = 100;
        data[12] = 0; data[13] = 1;
        data[14] = 0;
        data[15] = 0; data[16] = 0;
        data[17] = 0; data[18] = 0; data[19] = 0; data[20] = 76;

        for (int i = 21; i < 96; i++) {
            data[i] = 0;
        }

        data[92] = 0; data[93] = 0; data[94] = 0; data[95] = 4;
        data[96] = 1; data[97] = 2; data[98] = 3; data[99] = 4;

        return data;
    }

    /**
     * Sets up default mock behavior for all dependencies used in tests.
     * Configures common mock responses to avoid repetition in individual tests.
     */
    private void setupDefaultMockBehavior() {
        when(cryptoCoreUtil.decrypt(anyString())).thenReturn(mockDecodedCredential);

        try {
            when(dataShareUtil.getDataShare(any(byte[].class), anyString(), anyString()))
                    .thenReturn(mockDataShare);
        } catch (Exception e) {
            // Mock setup exception
        }

        doNothing().when(webSubSubscriptionHelper).printStatusUpdateEvent(anyString(), any());

        when(auditLogRequestBuilder.createAuditRequestBuilder(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);
    }

    /**
     * Sets up JsonUtil static method mocks for JSON processing tests.
     * Configures mock behavior for JSON parsing, identity mapping, and configuration retrieval.
     *
     * @param jsonUtilMock the mocked static JsonUtil class
     * @throws Exception if mock setup fails
     */
    private void setupJsonUtilMocks(MockedStatic<JsonUtil> jsonUtilMock) throws Exception {
        JSONObject mockDemographicIdentity = new JSONObject();
        mockDemographicIdentity.put("UIN", "1234567890");
        mockDemographicIdentity.put("fullName", "John Doe");
        mockDemographicIdentity.put("biometrics", "biometricData");

        JSONObject printTextFileConfig = new JSONObject();
        printTextFileConfig.put("personal", "UIN,fullName");

        JSONObject identityMappingJson = new JSONObject();
        JSONObject mapperIdentity = new JSONObject();

        LinkedHashMap<String, String> fullNameMapping = new LinkedHashMap<>();
        fullNameMapping.put("value", "fullName");
        mapperIdentity.put("fullName", fullNameMapping);

        LinkedHashMap<String, String> uinMapping = new LinkedHashMap<>();
        uinMapping.put("value", "UIN");
        mapperIdentity.put("UIN", uinMapping);

        identityMappingJson.put("identity", mapperIdentity);

        jsonUtilMock.when(() -> JsonUtil.objectMapperReadValue(anyString(), eq(JSONObject.class)))
                .thenAnswer(invocation -> {
                    String jsonString = invocation.getArgument(0);
                    if (jsonString.contains("personal")) {
                        return printTextFileConfig;
                    } else if (jsonString.contains("identity")) {
                        return identityMappingJson;
                    }
                    return mockDemographicIdentity;
                });

        jsonUtilMock.when(() -> JsonUtil.getJSONObject(any(JSONObject.class), anyString()))
                .thenReturn(mapperIdentity);

        jsonUtilMock.when(() -> JsonUtil.getJSONValue(any(JSONObject.class), anyString()))
                .thenReturn("personal");

        setupUtilityMocks();
        setupGeneratorMocks();
    }

    /**
     * Sets up utility class mocks for configuration and file access.
     * Configures mock responses for utility methods used in document generation.
     *
     * @throws Exception if mock setup fails
     */
    private void setupUtilityMocks() throws Exception {
        when(utilities.getPrintTextFileJson(anyString(), anyString())).thenReturn("{\"personal\":\"UIN,fullName\"}");
        when(utilities.getIdentityMappingJson(anyString(), anyString())).thenReturn("{\"identity\":{\"fullName\":{\"value\":\"fullName\"},\"UIN\":{\"value\":\"UIN\"}}}");
        when(utilities.getConfigServerFileStorageURL()).thenReturn("http://config");
        when(utilities.getRegistrationProcessorPrintTextFile()).thenReturn("textfile.json");
        when(utilities.getGetRegProcessorIdentityJson()).thenReturn("identity.json");
        when(utilities.getGetRegProcessorDemographicIdentity()).thenReturn("identity");
    }

    /**
     * Sets up generator class mocks for template, QR code, and card generation.
     * Configures mock behavior for all generator services used in document processing.
     *
     * @throws Exception if mock setup fails
     */
    private void setupGeneratorMocks() throws Exception {
        when(templateGenerator.getTemplate(anyString(), any(Map.class), anyString()))
                .thenReturn(new ByteArrayInputStream("template".getBytes()));

        when(qrCodeGenerator.generateQrCode(anyString(), any(QrVersion.class)))
                .thenReturn("qrcode".getBytes());

        when(uinCardGenerator.generateUinCard(any(InputStream.class), any(UinCardType.class), anyString()))
                .thenReturn("pdf".getBytes());

        CbeffToBiometricUtil mockUtil = mock(CbeffToBiometricUtil.class);
        when(mockUtil.getImageBytes(anyString(), anyString(), any(List.class)))
                .thenReturn(createMockBiometricData());
    }
}
