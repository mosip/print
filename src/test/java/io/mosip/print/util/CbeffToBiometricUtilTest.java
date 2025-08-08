package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.print.constant.BiometricType;
import io.mosip.print.entity.BIR;
import io.mosip.print.entity.BDBInfo;
import io.mosip.print.exception.BiometricTagMatchException;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.model.KeyValuePair;
import io.mosip.print.model.Response;
import io.mosip.print.spi.CbeffUtil;
import io.mosip.print.spi.IBioApi;

/**
 * Unit tests for {@link CbeffToBiometricUtil} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the CbeffToBiometricUtil class,
 * including CBEFF data processing, biometric image extraction, CBEFF merging operations, template extraction,
 * and various validation scenarios for biometric data handling.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CbeffToBiometricUtilTest {

    @Mock
    private CbeffUtil cbeffutil;

    @Mock
    private IBioApi bioApi;

    @InjectMocks
    private CbeffToBiometricUtil cbeffToBiometricUtil;

    private static final String BASE64_CBEFF_DATA = Base64.encodeBase64String("test-cbeff-data".getBytes());
    private static final byte[] TEST_IMAGE_BYTES = "test-image-bytes".getBytes();
    private static final byte[] TEST_XML_BYTES = "test-xml-bytes".getBytes();

    /**
     * Tests the default constructor of CbeffToBiometricUtil.
     * Verifies that a new instance can be created successfully using the default constructor.
     */
    @Test
    void defaultConstructorShouldCreateInstance() {
        CbeffToBiometricUtil util = new CbeffToBiometricUtil();
        assertNotNull(util);
    }

    /**
     * Tests the constructor with CbeffUtil parameter.
     * Verifies that a new instance can be created successfully when provided with a CbeffUtil dependency.
     */
    @Test
    void constructorWithCbeffUtilShouldCreateInstance() {
        CbeffUtil mockCbeffUtil = mock(CbeffUtil.class);
        CbeffToBiometricUtil util = new CbeffToBiometricUtil(mockCbeffUtil);
        assertNotNull(util);
    }

    /**
     * Tests successful image bytes retrieval from CBEFF data.
     * Verifies that biometric image bytes are correctly extracted when valid CBEFF data,
     * biometric type, and subtype are provided.
     */
    @Test
    void getImageBytesShouldSucceedWithValidData() throws Exception {
        List<BIR> birList = createMockBIRList();

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CbeffToBiometricUtil.class))
                    .thenReturn(mockLogger);

            when(cbeffutil.getBIRDataFromXML(any(byte[].class))).thenReturn(birList);

            byte[] result = cbeffToBiometricUtil.getImageBytes(BASE64_CBEFF_DATA, "Face",
                    Arrays.asList("Front"));

            assertArrayEquals(TEST_IMAGE_BYTES, result);
        }
    }

    /**
     * Tests image bytes retrieval with null CBEFF file string.
     * Verifies that the method handles null CBEFF data gracefully and returns null.
     */
    @Test
    void getImageBytesWithNullCbeffStringShouldReturnNull() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CbeffToBiometricUtil.class))
                    .thenReturn(mockLogger);

            byte[] result = cbeffToBiometricUtil.getImageBytes(null, "Face",
                    Arrays.asList("Front"));

            assertNull(result);
        }
    }

    /**
     * Tests image bytes retrieval with no matching type and subtype.
     * Verifies that the method returns null when no biometric data matches the specified type and subtype.
     */
    @Test
    void getImageBytesWithNoMatchShouldReturnNull() throws Exception {
        List<BIR> birList = createMockBIRList();

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CbeffToBiometricUtil.class))
                    .thenReturn(mockLogger);

            when(cbeffutil.getBIRDataFromXML(any(byte[].class))).thenReturn(birList);

            byte[] result = cbeffToBiometricUtil.getImageBytes(BASE64_CBEFF_DATA, "Fingerprint",
                    Arrays.asList("LeftThumb"));

            assertNull(result);
        }
    }

    /**
     * Tests image bytes retrieval with null BdbInfo.
     * Verifies that the method handles BIR objects with null BdbInfo appropriately and returns null.
     */
    @Test
    void getImageBytesWithNullBdbInfoShouldReturnNull() throws Exception {
        List<BIR> birList = createMockBIRListWithNullBdbInfo();

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CbeffToBiometricUtil.class))
                    .thenReturn(mockLogger);

            when(cbeffutil.getBIRDataFromXML(any(byte[].class))).thenReturn(birList);

            byte[] result = cbeffToBiometricUtil.getImageBytes(BASE64_CBEFF_DATA, "Face",
                    Arrays.asList("Front"));

            assertNull(result);
        }
    }

    /**
     * Tests successful CBEFF merge operation.
     * Verifies that two CBEFF files with different biometric types can be successfully merged.
     */
    @Test
    void mergeCbeffShouldSucceedWithDifferentTypes() throws Exception {
        List<BIR> file1BirList = createMockBIRListWithDifferentType("Face");
        List<BIR> file2BirList = createMockBIRListWithDifferentType("Fingerprint");

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CbeffToBiometricUtil.class))
                    .thenReturn(mockLogger);

            when(cbeffutil.getBIRDataFromXML(any(byte[].class)))
                    .thenReturn(file1BirList)
                    .thenReturn(file2BirList);
            when(cbeffutil.createXML(any(List.class))).thenReturn(TEST_XML_BYTES);

            InputStream result = cbeffToBiometricUtil.mergeCbeff(BASE64_CBEFF_DATA, BASE64_CBEFF_DATA);

            assertNotNull(result);
        }
    }

    /**
     * Tests CBEFF merge with matching biometric types.
     * Verifies that attempting to merge CBEFF files with the same biometric types throws BiometricTagMatchException.
     */
    @Test
    void mergeCbeffWithMatchingTypesShouldThrowException() throws Exception {
        List<BIR> file1BirList = createMockBIRListWithDifferentType("Face");
        List<BIR> file2BirList = createMockBIRListWithDifferentType("Face");

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CbeffToBiometricUtil.class))
                    .thenReturn(mockLogger);

            when(cbeffutil.getBIRDataFromXML(any(byte[].class)))
                    .thenReturn(file1BirList)
                    .thenReturn(file2BirList);

            assertThrows(BiometricTagMatchException.class, () ->
                    cbeffToBiometricUtil.mergeCbeff(BASE64_CBEFF_DATA, BASE64_CBEFF_DATA)
            );
        }
    }

    /**
     * Tests successful CBEFF extraction with specified types.
     * Verifies that biometric data of specified types can be successfully extracted from CBEFF data.
     */
    @Test
    void extractCbeffWithTypesShouldSucceedWithMatchingTypes() throws Exception {
        List<BIR> birList = createMockBIRList();

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CbeffToBiometricUtil.class))
                    .thenReturn(mockLogger);

            when(cbeffutil.getBIRDataFromXML(any(byte[].class))).thenReturn(birList);
            when(cbeffutil.createXML(any(List.class))).thenReturn(TEST_XML_BYTES);

            InputStream result = cbeffToBiometricUtil.extractCbeffWithTypes(BASE64_CBEFF_DATA,
                    Arrays.asList("Face"));

            assertNotNull(result);
        }
    }

    /**
     * Tests CBEFF extraction with no matching types.
     * Verifies that the method returns null when no biometric data matches the specified types.
     */
    @Test
    void extractCbeffWithTypesNoMatchShouldReturnNull() throws Exception {
        List<BIR> birList = createMockBIRList();

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CbeffToBiometricUtil.class))
                    .thenReturn(mockLogger);

            when(cbeffutil.getBIRDataFromXML(any(byte[].class))).thenReturn(birList);

            InputStream result = cbeffToBiometricUtil.extractCbeffWithTypes(BASE64_CBEFF_DATA,
                    Arrays.asList("Fingerprint"));

            assertNull(result);
        }
    }

    /**
     * Tests CBEFF extraction with empty extracted BIR list.
     * Verifies that the method returns null when the extraction process results in an empty BIR list.
     */
    @Test
    void extractCbeffWithTypesEmptyExtractedShouldReturnNull() throws Exception {
        List<BIR> birList = new ArrayList<>();

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CbeffToBiometricUtil.class))
                    .thenReturn(mockLogger);

            when(cbeffutil.getBIRDataFromXML(any(byte[].class))).thenReturn(birList);

            InputStream result = cbeffToBiometricUtil.extractCbeffWithTypes(BASE64_CBEFF_DATA,
                    Arrays.asList("Face"));

            assertNull(result);
        }
    }

    /**
     * Tests the getBIRTypeList method functionality.
     * Verifies that the method correctly retrieves the BIR type list from CBEFF data.
     */
    @Test
    void getBirTypeListShouldReturnExpectedList() throws Exception {
        List<BIR> expectedBirList = createMockBIRList();
        when(cbeffutil.getBIRDataFromXML(any(byte[].class))).thenReturn(expectedBirList);

        List<BIR> result = cbeffToBiometricUtil.getBIRTypeList(BASE64_CBEFF_DATA);

        assertEquals(expectedBirList, result);
    }

    /**
     * Tests the getBIRDataFromXML method functionality.
     * Verifies that the method correctly retrieves BIR data from XML byte array.
     */
    @Test
    void getBirDataFromXmlShouldReturnExpectedData() throws Exception {
        List<BIR> expectedBirList = createMockBIRList();
        when(cbeffutil.getBIRDataFromXML(any(byte[].class))).thenReturn(expectedBirList);

        List<BIR> result = cbeffToBiometricUtil.getBIRDataFromXML(TEST_XML_BYTES);

        assertEquals(expectedBirList, result);
    }

    /**
     * Tests the extractTemplate method functionality.
     * Verifies that biometric template extraction works correctly with valid BIR data and flags.
     */
    @Test
    void extractTemplateShouldReturnExpectedResult() throws Exception {
        BIR sampleBir = createMockBIR("Face", Arrays.asList("Front"));
        BIR expectedResult = createMockBIR("Face", Arrays.asList("Front"));
        KeyValuePair[] flags = new KeyValuePair[0];

        Response<BIR> mockResponse = new Response<>();
        mockResponse.setResponse(expectedResult);

        lenient().when(bioApi.extractTemplate(sampleBir, flags)).thenReturn(mockResponse);

        BIR result = cbeffToBiometricUtil.extractTemplate(sampleBir, flags);

        assertEquals(expectedResult, result);
    }

    /**
     * Tests the isBiometricType method through reflection with matching type.
     * Verifies that the private method correctly identifies matching biometric types.
     */
    @Test
    void isBiometricTypeMethodShouldReturnTrueForMatchingType() throws Exception {
        List<BiometricType> biometricTypeList = Arrays.asList(BiometricType.FACE);

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(cbeffToBiometricUtil,
                "isBiometricType", "Face", biometricTypeList);

        assertEquals(true, result);
    }

    /**
     * Tests the isBiometricType method through reflection with no match.
     * Verifies that the private method correctly identifies non-matching biometric types.
     */
    @Test
    void isBiometricTypeMethodShouldReturnFalseForNonMatchingType() throws Exception {
        List<BiometricType> biometricTypeList = Arrays.asList(BiometricType.FACE);

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(cbeffToBiometricUtil,
                "isBiometricType", "Fingerprint", biometricTypeList);

        assertEquals(false, result);
    }

    /**
     * Tests the isSubType method through reflection with matching subtype.
     * Verifies that the private method correctly identifies matching biometric subtypes.
     */
    @Test
    void isSubTypeMethodShouldReturnTrueForMatchingSubtype() throws Exception {
        List<String> subTypeList = Arrays.asList("Front");
        List<String> subType = Arrays.asList("Front");

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(cbeffToBiometricUtil,
                "isSubType", subType, subTypeList);

        assertEquals(true, result);
    }

    /**
     * Tests the isSubType method through reflection with no match.
     * Verifies that the private method correctly identifies non-matching biometric subtypes.
     */
    @Test
    void isSubTypeMethodShouldReturnFalseForNonMatchingSubtype() throws Exception {
        List<String> subTypeList = Arrays.asList("Front");
        List<String> subType = Arrays.asList("Back");

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(cbeffToBiometricUtil,
                "isSubType", subType, subTypeList);

        assertEquals(false, result);
    }

    /**
     * Tests the isBiometricTypeSame method with different types.
     * Verifies that the private method correctly identifies when biometric types are different.
     */
    @Test
    void isBiometricTypeSameMethodShouldReturnFalseForDifferentTypes() throws Exception {
        List<BIR> file1BirList = createMockBIRListWithDifferentType("Face");
        List<BIR> file2BirList = createMockBIRListWithDifferentType("Fingerprint");

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(cbeffToBiometricUtil,
                "isBiometricTypeSame", file1BirList, file2BirList);

        assertEquals(false, result);
    }

    /**
     * Tests the isBiometricTypeSame method with same types.
     * Verifies that the private method correctly identifies when biometric types are the same.
     */
    @Test
    void isBiometricTypeSameMethodShouldReturnTrueForSameTypes() throws Exception {
        List<BIR> file1BirList = createMockBIRListWithDifferentType("Face");
        List<BIR> file2BirList = createMockBIRListWithDifferentType("Face");

        boolean result = (boolean) ReflectionTestUtils.invokeMethod(cbeffToBiometricUtil,
                "isBiometricTypeSame", file1BirList, file2BirList);

        assertEquals(true, result);
    }

    /**
     * Creates a mock BIR list for testing purposes.
     *
     * @return List of mock BIR objects with Face biometric type and Front subtype
     */
    private List<BIR> createMockBIRList() {
        List<BIR> birList = new ArrayList<>();
        BIR bir = createMockBIR("Face", Arrays.asList("Front"));
        birList.add(bir);
        return birList;
    }

    /**
     * Creates a mock BIR list with null BdbInfo for testing edge cases.
     *
     * @return List of mock BIR objects with null BdbInfo
     */
    private List<BIR> createMockBIRListWithNullBdbInfo() {
        List<BIR> birList = new ArrayList<>();
        BIR bir = new BIR();
        bir.setBdbInfo(null);
        birList.add(bir);
        return birList;
    }

    /**
     * Creates a mock BIR list with a specific biometric type.
     *
     * @param type the biometric type to set for the BIR objects
     * @return List of mock BIR objects with the specified biometric type
     */
    private List<BIR> createMockBIRListWithDifferentType(String type) {
        List<BIR> birList = new ArrayList<>();
        BIR bir = createMockBIR(type, Arrays.asList("Front"));
        birList.add(bir);
        return birList;
    }

    /**
     * Creates a mock BIR object with specified type and subtypes.
     *
     * @param type the biometric type for the BIR object
     * @param subTypes the list of subtypes for the BIR object
     * @return a mock BIR object with the specified configuration
     */
    private BIR createMockBIR(String type, List<String> subTypes) {
        BIR bir = new BIR();
        BDBInfo bdbInfo = new BDBInfo();

        List<BiometricType> biometricTypes = new ArrayList<>();
        biometricTypes.add(getBiometricTypeFromString(type));
        bdbInfo.setType(biometricTypes);
        bdbInfo.setSubtype(subTypes);

        bir.setBdbInfo(bdbInfo);
        bir.setBdb(TEST_IMAGE_BYTES);

        return bir;
    }

    /**
     * Converts a string representation to the corresponding BiometricType enum.
     *
     * @param type the string representation of the biometric type
     * @return the corresponding BiometricType enum value
     */
    private BiometricType getBiometricTypeFromString(String type) {
        switch (type.toLowerCase()) {
            case "face":
                return BiometricType.FACE;
            case "fingerprint":
                return BiometricType.FINGER;
            case "iris":
                return BiometricType.IRIS;
            default:
                return BiometricType.FACE;
        }
    }
}
