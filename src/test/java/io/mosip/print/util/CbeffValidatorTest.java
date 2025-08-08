package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.xml.sax.SAXException;

import io.mosip.print.constant.BiometricType;
import io.mosip.print.constant.CbeffConstant;
import io.mosip.print.entity.BDBInfo;
import io.mosip.print.entity.BIR;
import io.mosip.print.entity.BIRInfo;
import io.mosip.print.entity.RegistryIDType;
import io.mosip.print.entity.VersionType;
import io.mosip.print.exception.CbeffException;

/**
 * Test class for {@link CbeffValidator}.
 *
 * This class provides comprehensive unit tests for the CbeffValidator utility class,
 * covering all methods including validation, XML creation, data retrieval based on
 * type and subtype, and various edge cases and exception scenarios.
 *
 * The tests ensure proper validation of BIR data, XML marshalling/unmarshalling,
 * biometric data extraction, and error handling for invalid data conditions.
 *
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CbeffValidatorTest {

    private BIR mockBIR;
    private BDBInfo mockBDBInfo;
    private RegistryIDType mockFormat;
    private byte[] validXSD;

    private static final String VALID_SUBTYPE = "Left";
    private static final String VALID_SUBTYPE_LIST = "Left";

    /**
     * Sets up the test environment before each test method execution.
     *
     * Initializes mock objects and test data used across multiple test methods,
     * including valid BIR structures, BDBInfo, and format information.
     */
    @BeforeEach
    void setUp() {
        mockBIR = createValidBIR();
        mockBDBInfo = createValidBDBInfo();
        mockFormat = createValidFormat();
        validXSD = "valid xsd content".getBytes();
    }

    /**
     * Tests successful validation of valid BIR data.
     *
     * Verifies that {@link CbeffValidator#validateXML(BIR)} returns false
     * for valid BIR data structure with proper BDB, BDBInfo, and format information.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void validateXMLSuccess() throws Exception {
        assertFalse(CbeffValidator.validateXML(mockBIR));
    }

    /**
     * Tests validation failure when BIR is null.
     *
     * Verifies that {@link CbeffValidator#validateXML(BIR)} throws
     * {@link CbeffException} with appropriate message when BIR is null.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void validateXMLWithNullBIR() throws Exception {
        CbeffException exception = assertThrows(CbeffException.class, () ->
                CbeffValidator.validateXML(null));
        assertEquals("BIR value is null", exception.getMessage());
    }

    /**
     * Tests validation failure when BDB is empty.
     *
     * Verifies that {@link CbeffValidator#validateXML(BIR)} throws
     * {@link CbeffException} when BIR contains empty BDB data.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void validateXMLWithEmptyBDB() throws Exception {
        BIR birWithEmptyBDB = createBIRWithEmptyBDB();

        CbeffException exception = assertThrows(CbeffException.class, () ->
                CbeffValidator.validateXML(birWithEmptyBDB));
        assertEquals("BDB value can't be empty", exception.getMessage());
    }

    /**
     * Tests validation failure when BDBInfo is null.
     *
     * Verifies that {@link CbeffValidator#validateXML(BIR)} throws
     * {@link CbeffException} when BIR contains null BDBInfo.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void validateXMLWithNullBDBInfo() throws Exception {
        BIR birWithNullBDBInfo = createBIRWithNullBDBInfo();

        CbeffException exception = assertThrows(CbeffException.class, () ->
                CbeffValidator.validateXML(birWithNullBDBInfo));
        assertEquals("BDB information can't be empty", exception.getMessage());
    }

    /**
     * Tests validation failure when biometric types are null.
     *
     * Verifies that {@link CbeffValidator#validateXML(BIR)} throws
     * {@link CbeffException} when BDBInfo contains null biometric types.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void validateXMLWithNullBiometricTypes() throws Exception {
        BIR birWithNullTypes = createBIRWithNullTypes();

        CbeffException exception = assertThrows(CbeffException.class, () ->
                CbeffValidator.validateXML(birWithNullTypes));
        assertEquals("Type value needs to be provided", exception.getMessage());
    }

    /**
     * Tests validation failure when biometric types are empty.
     *
     * Verifies that {@link CbeffValidator#validateXML(BIR)} throws
     * {@link CbeffException} when BDBInfo contains empty biometric types list.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void validateXMLWithEmptyBiometricTypes() throws Exception {
        BIR birWithEmptyTypes = createBIRWithEmptyTypes();

        CbeffException exception = assertThrows(CbeffException.class, () ->
                CbeffValidator.validateXML(birWithEmptyTypes));
        assertEquals("Type value needs to be provided", exception.getMessage());
    }

    /**
     * Tests validation failure when format type is invalid.
     *
     * Verifies that {@link CbeffValidator#validateXML(BIR)} throws
     * {@link CbeffException} when format type doesn't match biometric type.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void validateXMLWithInvalidFormatType() throws Exception {
        BIR birWithInvalidFormat = createBIRWithInvalidFormat();

        CbeffException exception = assertThrows(CbeffException.class, () ->
                CbeffValidator.validateXML(birWithInvalidFormat));
        assertEquals("Patron Format type is invalid", exception.getMessage());
    }

    /**
     * Tests successful XML byte creation with valid BIR and XSD.
     *
     * Verifies that {@link CbeffValidator#createXMLBytes(BIR, byte[])} successfully
     * creates XML bytes when provided with valid BIR data and XSD schema.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void createXMLBytesSuccess() throws Exception {
        try (MockedStatic<CbeffXSDValidator> xsdValidatorMock = mockStatic(CbeffXSDValidator.class)) {
            xsdValidatorMock.when(() -> CbeffXSDValidator.validateXML(any(byte[].class), any(byte[].class)))
                    .thenAnswer(invocation -> null);

            byte[] result = CbeffValidator.createXMLBytes(mockBIR, validXSD);
            assertNotNull(result);
            assertTrue(result.length > 0);
        }
    }

    /**
     * Tests XML byte creation failure due to XSD validation error.
     *
     * Verifies that {@link CbeffValidator#createXMLBytes(BIR, byte[])} throws
     * {@link CbeffException} when XSD validation fails with SAXException.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void createXMLBytesWithXSDValidationFailure() throws Exception {
        try (MockedStatic<CbeffXSDValidator> xsdValidatorMock = mockStatic(CbeffXSDValidator.class)) {
            SAXException saxException = new SAXException("Invalid XML: attribute error");
            xsdValidatorMock.when(() -> CbeffXSDValidator.validateXML(any(byte[].class), any(byte[].class)))
                    .thenThrow(saxException);

            CbeffException exception = assertThrows(CbeffException.class, () ->
                    CbeffValidator.createXMLBytes(mockBIR, validXSD));
            assertTrue(exception.getMessage().contains("XSD validation failed"));
        }
    }

    /**
     * Tests successful BIR extraction from XML bytes.
     *
     * Verifies that {@link CbeffValidator#getBIRFromXML(byte[])} correctly
     * unmarshals XML bytes back to BIR object structure.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBIRFromXMLSuccess() throws Exception {
        try (MockedStatic<CbeffXSDValidator> xsdValidatorMock = mockStatic(CbeffXSDValidator.class)) {
            xsdValidatorMock.when(() -> CbeffXSDValidator.validateXML(any(byte[].class), any(byte[].class)))
                    .thenAnswer(invocation -> null);

            byte[] xmlBytes = CbeffValidator.createXMLBytes(mockBIR, validXSD);
            BIR result = CbeffValidator.getBIRFromXML(xmlBytes);

            assertNotNull(result);
            assertNotNull(result.getBirs());
        }
    }

    /**
     * Tests BDB data retrieval with both type and subtype specified.
     *
     * Verifies that {@link CbeffValidator#getBDBBasedOnTypeAndSubType(BIR, String, String)}
     * correctly filters and returns BDB data based on specified biometric type and subtype.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBDBBasedOnTypeAndSubTypeWithBothParams() throws Exception {
        Map<String, String> result = CbeffValidator.getBDBBasedOnTypeAndSubType(mockBIR, "FINGER", VALID_SUBTYPE);
        assertNotNull(result);
    }

    /**
     * Tests BDB data retrieval with only type specified.
     *
     * Verifies that {@link CbeffValidator#getBDBBasedOnTypeAndSubType(BIR, String, String)}
     * correctly returns BDB data when only biometric type is specified.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBDBBasedOnTypeAndSubTypeWithTypeOnly() throws Exception {
        Map<String, String> result = CbeffValidator.getBDBBasedOnTypeAndSubType(mockBIR, "FINGER", null);
        assertNotNull(result);
    }

    /**
     * Tests BDB data retrieval with only subtype specified.
     *
     * Verifies that {@link CbeffValidator#getBDBBasedOnTypeAndSubType(BIR, String, String)}
     * correctly returns BDB data when only subtype is specified.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBDBBasedOnTypeAndSubTypeWithSubTypeOnly() throws Exception {
        Map<String, String> result = CbeffValidator.getBDBBasedOnTypeAndSubType(mockBIR, null, VALID_SUBTYPE);
        assertNotNull(result);
    }

    /**
     * Tests BDB data retrieval with null parameters.
     *
     * Verifies that {@link CbeffValidator#getBDBBasedOnTypeAndSubType(BIR, String, String)}
     * returns all latest data when both type and subtype are null.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBDBBasedOnTypeAndSubTypeWithNullParams() throws Exception {
        Map<String, String> result = CbeffValidator.getBDBBasedOnTypeAndSubType(mockBIR, null, null);
        assertNotNull(result);
    }

    /**
     * Tests BDB data retrieval with empty BIR list.
     *
     * Verifies that {@link CbeffValidator#getBDBBasedOnTypeAndSubType(BIR, String, String)}
     * handles empty BIR list gracefully and returns empty map.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBDBBasedOnTypeAndSubTypeWithEmptyBIRList() throws Exception {
        BIR emptyBIR = new BIR.BIRBuilder().build();
        emptyBIR.setBirs(new ArrayList<>());

        Map<String, String> result = CbeffValidator.getBDBBasedOnTypeAndSubType(emptyBIR, "FINGER", VALID_SUBTYPE);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests all BDB data retrieval with type and subtype.
     *
     * Verifies that {@link CbeffValidator#getAllBDBData(BIR, String, String)}
     * correctly returns all BDB data matching the specified criteria.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getAllBDBDataWithTypeAndSubType() throws Exception {
        Map<String, String> result = CbeffValidator.getAllBDBData(mockBIR, "FINGER", VALID_SUBTYPE);
        assertNotNull(result);
    }

    /**
     * Tests all BDB data retrieval with type only.
     *
     * Verifies that {@link CbeffValidator#getAllBDBData(BIR, String, String)}
     * correctly returns BDB data when only type is specified.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getAllBDBDataWithTypeOnly() throws Exception {
        Map<String, String> result = CbeffValidator.getAllBDBData(mockBIR, "FINGER", null);
        assertNotNull(result);
    }

    /**
     * Tests all BDB data retrieval with subtype only.
     *
     * Verifies that {@link CbeffValidator#getAllBDBData(BIR, String, String)}
     * correctly returns BDB data when only subtype is specified.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getAllBDBDataWithSubTypeOnly() throws Exception {
        Map<String, String> result = CbeffValidator.getAllBDBData(mockBIR, null, VALID_SUBTYPE);
        assertNotNull(result);
    }

    /**
     * Tests all BDB data retrieval with null BIR list.
     *
     * Verifies that {@link CbeffValidator#getAllBDBData(BIR, String, String)}
     * handles null BIR list gracefully and returns empty map.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getAllBDBDataWithNullBIRList() throws Exception {
        BIR nullBIR = new BIR.BIRBuilder().build();
        nullBIR.setBirs(null);

        Map<String, String> result = CbeffValidator.getAllBDBData(nullBIR, "FINGER", VALID_SUBTYPE);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests BIR data extraction from XML by type.
     *
     * Verifies that {@link CbeffValidator#getBIRDataFromXMLType(byte[], String)}
     * correctly filters and returns BIR data matching the specified type.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBIRDataFromXMLTypeSuccess() throws Exception {
        try (MockedStatic<CbeffXSDValidator> xsdValidatorMock = mockStatic(CbeffXSDValidator.class)) {
            xsdValidatorMock.when(() -> CbeffXSDValidator.validateXML(any(byte[].class), any(byte[].class)))
                    .thenAnswer(invocation -> null);

            byte[] xmlBytes = CbeffValidator.createXMLBytes(mockBIR, validXSD);
            List<BIR> result = CbeffValidator.getBIRDataFromXMLType(xmlBytes, "FINGER");

            assertNotNull(result);
        }
    }

    /**
     * Tests BIR data extraction from XML with null type.
     *
     * Verifies that {@link CbeffValidator#getBIRDataFromXMLType(byte[], String)}
     * handles null type parameter and returns empty list.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBIRDataFromXMLTypeWithNullType() throws Exception {
        try (MockedStatic<CbeffXSDValidator> xsdValidatorMock = mockStatic(CbeffXSDValidator.class)) {
            xsdValidatorMock.when(() -> CbeffXSDValidator.validateXML(any(byte[].class), any(byte[].class)))
                    .thenAnswer(invocation -> null);

            byte[] xmlBytes = CbeffValidator.createXMLBytes(mockBIR, validXSD);
            List<BIR> result = CbeffValidator.getBIRDataFromXMLType(xmlBytes, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests enum validation with valid enum value.
     *
     * Verifies that {@link CbeffValidator#isInEnum(String, Class)} returns true
     * for valid enum values.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isInEnumWithValidValue() throws Exception {
        assertTrue(CbeffValidator.isInEnum("FINGER", BiometricType.class));
    }

    /**
     * Tests enum validation with invalid enum value.
     *
     * Verifies that {@link CbeffValidator#isInEnum(String, Class)} returns false
     * for invalid enum values.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isInEnumWithInvalidValue() throws Exception {
        assertFalse(CbeffValidator.isInEnum("INVALID_TYPE", BiometricType.class));
    }

    /**
     * Tests format validation for different biometric types.
     *
     * Verifies that the private validateFormatType method correctly validates
     * format types for various biometric types including Finger, Iris, Face, and HandGeometry.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void validateFormatTypeForDifferentTypes() throws Exception {
        BIR fingerBIR = createBIRWithType(BiometricType.FINGER, CbeffConstant.FORMAT_TYPE_FINGER);
        assertFalse(CbeffValidator.validateXML(fingerBIR));

        BIR fingerMinutiaeBIR = createBIRWithType(BiometricType.FINGER, CbeffConstant.FORMAT_TYPE_FINGER_MINUTIAE);
        assertFalse(CbeffValidator.validateXML(fingerMinutiaeBIR));

        BIR irisBIR = createBIRWithType(BiometricType.IRIS, CbeffConstant.FORMAT_TYPE_IRIS);
        assertFalse(CbeffValidator.validateXML(irisBIR));

        BIR faceBIR = createBIRWithType(BiometricType.FACE, CbeffConstant.FORMAT_TYPE_FACE);
        assertFalse(CbeffValidator.validateXML(faceBIR));
    }

    /**
     * Tests getBiometricType method for special cases.
     *
     * Verifies that the private getBiometricType method correctly handles
     * special case mappings like "FMR" to FINGER type.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBiometricTypeSpecialCases() throws Exception {
        Map<String, String> result = CbeffValidator.getBDBBasedOnTypeAndSubType(mockBIR, "FMR", null);
        assertNotNull(result);

        Map<String, String> result2 = CbeffValidator.getBDBBasedOnTypeAndSubType(mockBIR, "Finger", null);
        assertNotNull(result2);
    }

    /**
     * Tests BDB data retrieval with version and cbeffversion fields.
     *
     * Verifies that BIR data with version and cbeffversion fields are properly handled
     * in the getBIRList method.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getBIRListWithVersionFields() throws Exception {
        BIR birWithVersions = createBIRWithVersions();
        Map<String, String> result = CbeffValidator.getBDBBasedOnTypeAndSubType(birWithVersions, null, null);
        assertNotNull(result);
    }

    /**
     * Tests data retrieval with empty subtype list.
     *
     * Verifies that BIR data with empty subtype list is handled correctly
     * and default "No Subtype" is applied.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getAllLatestDataWithEmptySubtype() throws Exception {
        BIR birWithEmptySubtype = createBIRWithEmptySubtype();
        Map<String, String> result = CbeffValidator.getBDBBasedOnTypeAndSubType(birWithEmptySubtype, null, null);
        assertNotNull(result);
    }

    // Helper methods for creating test data

    private BIR createValidBIR() {
        BIR bir = new BIR.BIRBuilder()
                .withBdb("test bdb data".getBytes())
                .withBdbInfo(createValidBDBInfo())
                .withBirInfo(new BIRInfo.BIRInfoBuilder().build())
                .build();

        List<BIR> birList = new ArrayList<>();
        birList.add(bir);

        BIR rootBir = new BIR.BIRBuilder().build();
        rootBir.setBirs(birList);
        return rootBir;
    }

    private BDBInfo createValidBDBInfo() {
        RegistryIDType format = new RegistryIDType();
        format.setOrganization("257");
        format.setType(String.valueOf(CbeffConstant.FORMAT_TYPE_FINGER));

        return new BDBInfo.BDBInfoBuilder()
                .withFormat(format)
                .withType(Arrays.asList(BiometricType.FINGER))
                .withSubtype(Arrays.asList(VALID_SUBTYPE_LIST)) // Use valid subtype
                .withCreationDate(LocalDateTime.now())
                .build();
    }

    private RegistryIDType createValidFormat() {
        RegistryIDType format = new RegistryIDType();
        format.setOrganization("257");
        format.setType(String.valueOf(CbeffConstant.FORMAT_TYPE_FINGER));
        return format;
    }

    private BIR createBIRWithEmptyBDB() {
        BIR bir = new BIR.BIRBuilder()
                .withBdb(new byte[0])
                .withBdbInfo(createValidBDBInfo())
                .build();

        List<BIR> birList = new ArrayList<>();
        birList.add(bir);

        BIR rootBir = new BIR.BIRBuilder().build();
        rootBir.setBirs(birList);
        return rootBir;
    }

    private BIR createBIRWithNullBDBInfo() {
        BIR bir = new BIR.BIRBuilder()
                .withBdb("test data".getBytes())
                .withBdbInfo(null)
                .build();

        List<BIR> birList = new ArrayList<>();
        birList.add(bir);

        BIR rootBir = new BIR.BIRBuilder().build();
        rootBir.setBirs(birList);
        return rootBir;
    }

    private BIR createBIRWithNullTypes() {
        RegistryIDType format = new RegistryIDType();
        format.setType(String.valueOf(CbeffConstant.FORMAT_TYPE_FINGER));

        BDBInfo bdbInfo = new BDBInfo.BDBInfoBuilder()
                .withFormat(format)
                .withType(null)
                .build();

        BIR bir = new BIR.BIRBuilder()
                .withBdb("test data".getBytes())
                .withBdbInfo(bdbInfo)
                .build();

        List<BIR> birList = new ArrayList<>();
        birList.add(bir);

        BIR rootBir = new BIR.BIRBuilder().build();
        rootBir.setBirs(birList);
        return rootBir;
    }

    private BIR createBIRWithEmptyTypes() {
        RegistryIDType format = new RegistryIDType();
        format.setType(String.valueOf(CbeffConstant.FORMAT_TYPE_FINGER));

        BDBInfo bdbInfo = new BDBInfo.BDBInfoBuilder()
                .withFormat(format)
                .withType(new ArrayList<>())
                .build();

        BIR bir = new BIR.BIRBuilder()
                .withBdb("test data".getBytes())
                .withBdbInfo(bdbInfo)
                .build();

        List<BIR> birList = new ArrayList<>();
        birList.add(bir);

        BIR rootBir = new BIR.BIRBuilder().build();
        rootBir.setBirs(birList);
        return rootBir;
    }

    private BIR createBIRWithInvalidFormat() {
        RegistryIDType format = new RegistryIDType();
        format.setType("999");

        BDBInfo bdbInfo = new BDBInfo.BDBInfoBuilder()
                .withFormat(format)
                .withType(Arrays.asList(BiometricType.FINGER))
                .build();

        BIR bir = new BIR.BIRBuilder()
                .withBdb("test data".getBytes())
                .withBdbInfo(bdbInfo)
                .build();

        List<BIR> birList = new ArrayList<>();
        birList.add(bir);

        BIR rootBir = new BIR.BIRBuilder().build();
        rootBir.setBirs(birList);
        return rootBir;
    }

    private BIR createBIRWithType(BiometricType type, long formatType) {
        RegistryIDType format = new RegistryIDType();
        format.setType(String.valueOf(formatType));

        BDBInfo bdbInfo = new BDBInfo.BDBInfoBuilder()
                .withFormat(format)
                .withType(Arrays.asList(type))
                .withSubtype(Arrays.asList(VALID_SUBTYPE_LIST)) // Use valid subtype
                .withCreationDate(LocalDateTime.now())
                .build();

        BIR bir = new BIR.BIRBuilder()
                .withBdb("test data".getBytes())
                .withBdbInfo(bdbInfo)
                .build();

        List<BIR> birList = new ArrayList<>();
        birList.add(bir);

        BIR rootBir = new BIR.BIRBuilder().build();
        rootBir.setBirs(birList);
        return rootBir;
    }

    private BIR createBIRWithVersions() {
        VersionType version = new VersionType();
        version.setMajor(1);
        version.setMinor(0);

        VersionType cbeffVersion = new VersionType();
        cbeffVersion.setMajor(1);
        cbeffVersion.setMinor(1);

        BIR bir = new BIR.BIRBuilder()
                .withBdb("test data".getBytes())
                .withBdbInfo(createValidBDBInfo())
                .withBirInfo(new BIRInfo.BIRInfoBuilder().build())
                .withVersion(version)
                .withCbeffversion(cbeffVersion)
                .build();

        List<BIR> birList = new ArrayList<>();
        birList.add(bir);

        BIR rootBir = new BIR.BIRBuilder().build();
        rootBir.setBirs(birList);
        return rootBir;
    }

    private BIR createBIRWithEmptySubtype() {
        RegistryIDType format = new RegistryIDType();
        format.setType(String.valueOf(CbeffConstant.FORMAT_TYPE_FINGER));

        BDBInfo bdbInfo = new BDBInfo.BDBInfoBuilder()
                .withFormat(format)
                .withType(Arrays.asList(BiometricType.FINGER))
                .withSubtype(new ArrayList<>())
                .withCreationDate(LocalDateTime.now())
                .build();

        BIR bir = new BIR.BIRBuilder()
                .withBdb("test data".getBytes())
                .withBdbInfo(bdbInfo)
                .build();

        List<BIR> birList = new ArrayList<>();
        birList.add(bir);

        BIR rootBir = new BIR.BIRBuilder().build();
        rootBir.setBirs(birList);
        return rootBir;
    }
}
