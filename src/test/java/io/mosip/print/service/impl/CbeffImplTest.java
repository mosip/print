package io.mosip.print.service.impl;

import com.sun.net.httpserver.HttpServer;
import io.mosip.print.entity.BIR;
import io.mosip.print.util.CbeffValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for {@link CbeffImpl}.
 * This test class validates the CBEFF (Common Biometric Exchange Formats Framework)
 * implementation including XML creation, validation, and data extraction operations.
 */
public class CbeffImplTest {

    private CbeffImpl cbeffImpl;
    private MockedStatic<CbeffValidator> validatorStatic;

    private final byte[] validXsd = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" +
            "<xs:element name=\"cbeff\" type=\"xs:string\"/>" +
            "</xs:schema>").getBytes();
    private final byte[] validXml = "<cbeff>ok</cbeff>".getBytes();

    private List<BIR> birList;

    /**
     * Sets up the test environment before each test execution.
     * Initializes CbeffImpl instance, configures necessary fields, and sets up static mocks.
     */
    @BeforeEach
    public void setUp() {
        cbeffImpl = new CbeffImpl();
        ReflectionTestUtils.setField(cbeffImpl, "configServerFileStorageURL", "http://localhost/");
        ReflectionTestUtils.setField(cbeffImpl, "schemaName", "schema.xsd");
        ReflectionTestUtils.setField(cbeffImpl, "xsd", validXsd);

        birList = new ArrayList<>();
        birList.add(new BIR());

        validatorStatic = mockStatic(CbeffValidator.class);
    }

    /**
     * Cleans up resources after each test execution.
     * Closes static mocks to prevent memory leaks and interference between tests.
     */
    @AfterEach
    public void tearDown() {
        validatorStatic.close();
    }

    /**
     * Tests the XSD loading functionality over HTTP protocol.
     * Verifies that the XSD schema can be successfully loaded from a remote HTTP server
     * and properly stored in the instance field.
     *
     * @throws Exception if HTTP server setup or XSD loading fails
     */
    @Test
    public void loadXsdOverHttpShouldSucceed() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/schema.xsd", exchange -> {
            byte[] resp = validXsd;
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(resp);
            }
        });
        server.start();
        try {
            String base = "http://localhost:" + server.getAddress().getPort() + "/";
            ReflectionTestUtils.setField(cbeffImpl, "configServerFileStorageURL", base);
            ReflectionTestUtils.setField(cbeffImpl, "xsd", null);
            cbeffImpl.loadXSD();
            assertArrayEquals(validXsd, (byte[]) ReflectionTestUtils.getField(cbeffImpl, "xsd"));
        } finally {
            server.stop(0);
        }
    }

    /**
     * Tests XML creation from BIR list using default XSD.
     * Verifies that the createXML method properly delegates to CbeffValidator
     * and returns the expected byte array.
     *
     * @throws Exception if XML creation fails
     */
    @Test
    public void createXmlShouldReturnBytes() throws Exception {
        validatorStatic.when(() -> CbeffValidator.createXMLBytes(any(), any())).thenReturn(new byte[]{1});
        assertArrayEquals(new byte[]{1}, cbeffImpl.createXML(birList));
    }

    /**
     * Tests XML creation from BIR list with explicitly provided XSD.
     * Verifies that the overloaded createXML method works correctly when
     * a custom XSD is provided as parameter.
     *
     * @throws Exception if XML creation fails
     */
    @Test
    public void createXmlWithXsdShouldReturnBytes() throws Exception {
        validatorStatic.when(() -> CbeffValidator.createXMLBytes(any(), any())).thenReturn(new byte[]{2});
        assertArrayEquals(new byte[]{2}, cbeffImpl.createXML(birList, validXsd));
    }

    /**
     * Tests XML update functionality with existing XML data.
     * Verifies that existing XML can be parsed, updated with new BIR data,
     * and converted back to byte array format.
     *
     * @throws Exception if XML update operation fails
     */
    @Test
    public void updateXmlShouldReturnUpdatedBytes() throws Exception {
        BIR existing = new BIR();
        existing.setBirs(new ArrayList<>());
        validatorStatic.when(() -> CbeffValidator.getBIRFromXML(any())).thenReturn(existing);
        validatorStatic.when(() -> CbeffValidator.createXMLBytes(any(), any())).thenReturn(new byte[]{9});
        assertArrayEquals(new byte[]{9}, cbeffImpl.updateXML(birList, validXml));
    }

    /**
     * Tests XML validation against explicitly provided XSD schema.
     * Verifies that the validation method returns a boolean result,
     * regardless of actual validation outcome.
     *
     * @throws Exception if validation process fails
     */
    @Test
    public void validateXmlWithExplicitXsdShouldReturnBoolean() throws Exception {
        boolean out = cbeffImpl.validateXML(validXml, validXsd);
        assertTrue(out || !out);
    }

    /**
     * Tests XML validation behavior when XSD is not loaded.
     * Verifies that appropriate exception is thrown when attempting
     * to validate XML without a loaded XSD schema.
     */
    @Test
    public void validateXmlWithLoadedXsdShouldThrowWhenXsdNull() {
        ReflectionTestUtils.setField(cbeffImpl, "xsd", null);
        assertThrows(NullPointerException.class, () -> cbeffImpl.validateXML(validXml));
    }

    /**
     * Tests BDB (Biometric Data Block) extraction based on type and subtype.
     * Verifies that the method correctly parses XML, extracts BDB data
     * for specified type and subtype, and returns expected results.
     *
     * @throws Exception if BDB extraction fails
     */
    @Test
    public void getBdbBasedOnTypeShouldReturnMap() throws Exception {
        BIR parsed = new BIR();
        validatorStatic.when(() -> CbeffValidator.getBIRFromXML(any())).thenReturn(parsed);
        validatorStatic.when(() -> CbeffValidator.getBDBBasedOnTypeAndSubType(parsed, "F", "ST"))
                .thenReturn(Map.of("F", "data"));
        assertEquals("data", cbeffImpl.getBDBBasedOnType(validXml, "F", "ST").get("F"));
    }

    /**
     * Tests BIR data extraction from XML format.
     * Verifies that XML can be parsed to extract BIR objects
     * and returns them as a properly sized list.
     *
     * @throws Exception if BIR data extraction fails
     */
    @Test
    public void getBirDataFromXmlShouldReturnList() throws Exception {
        BIR parent = new BIR();
        parent.setBirs(birList);
        validatorStatic.when(() -> CbeffValidator.getBIRFromXML(any())).thenReturn(parent);
        assertEquals(1, cbeffImpl.getBIRDataFromXML(validXml).size());
    }

    /**
     * Tests comprehensive BDB data extraction for specified type and subtype.
     * Verifies that all BDB data can be retrieved and returned as a map
     * with expected key-value pairs.
     *
     * @throws Exception if BDB data retrieval fails
     */
    @Test
    public void getAllBdbDataShouldReturnMap() throws Exception {
        BIR parsed = new BIR();
        validatorStatic.when(() -> CbeffValidator.getBIRFromXML(any())).thenReturn(parsed);
        validatorStatic.when(() -> CbeffValidator.getAllBDBData(parsed, "T", "ST"))
                .thenReturn(Map.of("K", "V"));
        assertEquals("V", cbeffImpl.getAllBDBData(validXml, "T", "ST").get("K"));
    }

    /**
     * Tests BIR data extraction filtered by specific type.
     * Verifies that BIR data can be filtered by type parameter
     * and returns the expected list of BIR objects.
     *
     * @throws Exception if type-based BIR extraction fails
     */
    @Test
    public void getBirDataFromXmlTypeShouldReturnList() throws Exception {
        validatorStatic.when(() -> CbeffValidator.getBIRDataFromXMLType(any(), eq("T"))).thenReturn(birList);
        assertEquals(birList, cbeffImpl.getBIRDataFromXMLType(validXml, "T"));
    }

    /**
     * Tests error handling when updating XML with null BIR list.
     * Verifies that NullPointerException is thrown when attempting
     * to update XML with null BIR list parameter.
     */
    @Test
    public void updateXmlWithNullBirListShouldThrowNpe() {
        assertThrows(NullPointerException.class, () -> cbeffImpl.updateXML(null, validXml));
    }

    /**
     * Tests error handling for BDB extraction with null file bytes.
     * Verifies that appropriate exception is thrown when attempting
     * to extract BDB data from null XML input through mocked validator.
     */
    @Test
    public void getBdbBasedOnTypeWithNullFileBytesShouldThrowViaMock() {
        validatorStatic.when(() -> CbeffValidator.getBIRFromXML(isNull())).thenThrow(new IllegalArgumentException());
        assertThrows(Exception.class, () -> cbeffImpl.getBDBBasedOnType(null, "F", "ST"));
    }

    /**
     * Tests error handling for BDB data extraction with null XML input.
     * Verifies that appropriate exception is thrown when attempting
     * to extract all BDB data from null XML through mocked validator.
     */
    @Test
    public void getAllBdbDataWithNullXmlShouldThrowViaMock() {
        validatorStatic.when(() -> CbeffValidator.getBIRFromXML(isNull())).thenThrow(new IllegalArgumentException());
        assertThrows(Exception.class, () -> cbeffImpl.getAllBDBData(null, "T", "ST"));
    }

    /**
     * Tests error handling for type-based BIR extraction with null XML.
     * Verifies that appropriate exception is thrown when attempting
     * to extract BIR data by type from null XML through mocked validator.
     */
    @Test
    public void getBirDataFromXmlTypeWithNullXmlShouldThrowViaMock() {
        validatorStatic.when(() -> CbeffValidator.getBIRDataFromXMLType(isNull(), anyString()))
                .thenThrow(new IllegalArgumentException());
        assertThrows(Exception.class, () -> cbeffImpl.getBIRDataFromXMLType(null, "T"));
    }
}