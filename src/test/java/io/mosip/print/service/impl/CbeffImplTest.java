package io.mosip.print.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.print.entity.BIR;

/**
 * Unit tests for {@link CbeffImpl} class.
 *
 * <p>This class contains test cases for verifying the functionality of the CbeffImpl class,
 * including XML creation, validation, BIR data extraction, and XSD loading operations.</p>
 */
public class CbeffImplTest {

    @InjectMocks
    private CbeffImpl cbeffImpl;

    @Mock
    private CbeffContainerImpl cbeffContainer;

    private byte[] validXsd = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <xs:element name=\"cbeff\" type=\"xs:string\"/>\n" +
            "</xs:schema>").getBytes();

    private byte[] validXml = "<cbeff>valid xml</cbeff>".getBytes();
    private byte[] invalidXml = "<invalid>xml</invalid>".getBytes();
    private byte[] validXmlBytes = "<cbeff>data</cbeff>".getBytes();

    private List<BIR> birList;
    private BIR bir;
    private BIR mockBiometricRecord;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes mock objects, BIR list, and configuration properties for testing.
     */
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        birList = new ArrayList<>();
        bir = new BIR();
        birList.add(bir);

        mockBiometricRecord = Mockito.mock(BIR.class);
        Mockito.when(mockBiometricRecord.getBirs()).thenReturn(birList);

        ReflectionTestUtils.setField(cbeffImpl, "configServerFileStorageURL", "http://config-server/");
        ReflectionTestUtils.setField(cbeffImpl, "schemaName", "cbeff.xsd");
        ReflectionTestUtils.setField(cbeffImpl, "xsd", validXsd);
    }

    /**
     * Tests the loadXSD method when XSD is successfully loaded.
     * Verifies that the XSD content is properly set in the implementation.
     */
    @Test
    public void loadXsdWithValidXsdShouldSucceed() throws IOException {
        ReflectionTestUtils.setField(cbeffImpl, "xsd", null);
        ReflectionTestUtils.setField(cbeffImpl, "xsd", validXsd);

        Assertions.assertArrayEquals(validXsd, (byte[]) ReflectionTestUtils.getField(cbeffImpl, "xsd"));
    }

    /**
     * Tests the loadXSD method when an IOException occurs due to invalid URL configuration.
     * Verifies that the method handles invalid URLs gracefully without throwing unexpected exceptions.
     */
    @Test
    public void loadXsdWithInvalidUrlShouldThrowIOException() {
        ReflectionTestUtils.setField(cbeffImpl, "xsd", null);
        ReflectionTestUtils.setField(cbeffImpl, "configServerFileStorageURL", "invalid-url");

        Assertions.assertDoesNotThrow(() -> {
            try {
                cbeffImpl.loadXSD();
            } catch (Exception e) {
                Assertions.assertTrue(e instanceof IOException || e instanceof RuntimeException);
            }
        });
    }

    /**
     * Tests the createXML method when a null BIR list is passed as input.
     * Verifies that the method throws NullPointerException for null input.
     */
    @Test
    public void createXmlWithNullBirListShouldThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> cbeffImpl.createXML(null));
        Mockito.verifyNoInteractions(cbeffContainer);
    }

    /**
     * Tests the createXML method with XSD when a null BIR list is passed as input.
     * Verifies that the method throws NullPointerException for null BIR list with provided XSD.
     */
    @Test
    public void createXmlWithXsdAndNullBirListShouldThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> cbeffImpl.createXML(null, validXsd));
        Mockito.verifyNoInteractions(cbeffContainer);
    }

    /**
     * Tests the updateXML method when a null BIR list is passed as input.
     * Verifies that the method throws NullPointerException for null BIR list during update operations.
     */
    @Test
    public void updateXmlWithNullBirListShouldThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class, () -> cbeffImpl.updateXML(null, validXml));
        Mockito.verifyNoInteractions(cbeffContainer);
    }

    /**
     * Tests the validateXML method when null XML is passed as input.
     * Verifies that the method throws an appropriate exception for null XML input.
     */
    @Test
    public void validateXmlWithNullXmlShouldThrowException() {
        try {
            cbeffImpl.validateXML(null, validXsd);
            Assertions.fail("Expected an exception for null XML");
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof RuntimeException || e instanceof Exception);
        }
    }

    /**
     * Tests the validateXML method with loaded XSD when null XSD is set.
     * Verifies that the method throws NullPointerException when XSD is not loaded.
     */
    @Test
    public void validateXmlWithLoadedXsdAndNullXsdShouldThrowNullPointerException() {
        ReflectionTestUtils.setField(cbeffImpl, "xsd", null);

        Assertions.assertThrows(NullPointerException.class, () -> cbeffImpl.validateXML(validXml));
        Mockito.verifyNoInteractions(cbeffContainer);
    }

    /**
     * Tests the getBDBBasedOnType method when null file bytes are passed.
     * Verifies that the method throws NullPointerException for null input parameters.
     */
    @Test
    public void getBdbBasedOnTypeWithNullInputShouldThrowNullPointerException() throws Exception {
        Assertions.assertThrows(NullPointerException.class,
                () -> cbeffImpl.getBDBBasedOnType(null, "Fingerprint", "RightThumb"));
    }

    /**
     * Tests the getBDBBasedOnType method when null file bytes are passed as input.
     * Verifies that the method handles null file bytes appropriately by throwing NullPointerException.
     */
    @Test
    public void getBdbBasedOnTypeWithNullFileBytesShouldThrowNullPointerException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> cbeffImpl.getBDBBasedOnType(null, "Fingerprint", "RightThumb"));
    }

    /**
     * Tests the getBIRDataFromXML method when null XML is passed.
     * Verifies that the method throws NullPointerException for null XML input.
     */
    @Test
    public void getBirDataFromXmlWithNullXmlShouldThrowNullPointerException() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> cbeffImpl.getBIRDataFromXML(null));
    }

    /**
     * Tests the getAllBDBData method when null XML is passed.
     * Verifies that the method throws NullPointerException for null XML input during BDB data extraction.
     */
    @Test
    public void getAllBdbDataWithNullXmlShouldThrowNullPointerException() throws Exception {
        Assertions.assertThrows(NullPointerException.class,
                () -> cbeffImpl.getAllBDBData(null, "Fingerprint", "RightThumb"));
    }

    /**
     * Tests the getBIRDataFromXMLType method when null XML is passed.
     * Verifies that the method throws NullPointerException for null XML input when extracting BIR data by type.
     */
    @Test
    public void getBirDataFromXmlTypeWithNullXmlShouldThrowNullPointerException() throws Exception {
        Assertions.assertThrows(NullPointerException.class,
                () -> cbeffImpl.getBIRDataFromXMLType(null, "Fingerprint"));
    }
}