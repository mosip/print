package io.mosip.print.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import io.mosip.print.entity.BIR;
import io.mosip.print.util.CbeffValidator;
import io.mosip.print.util.CbeffXSDValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link CbeffContainerImpl} class.
 *
 * <p>This class contains test cases for verifying the functionality of the CbeffContainerImpl class,
 * including BIR type creation, update operations, and XML validation.</p>
 */
class CbeffContainerImplTest {

    private CbeffContainerImpl cbeffContainer;
    private BIR sampleBir;
    private List<BIR> birList;
    private byte[] sampleXmlBytes;
    private byte[] sampleXsdBytes;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes the CbeffContainerImpl instance and test data for testing.
     */
    @BeforeEach
    void setUp() {
        cbeffContainer = new CbeffContainerImpl();
        sampleBir = new BIR();
        birList = new ArrayList<>();
        birList.add(sampleBir);
        sampleXmlBytes = "<xml>sample</xml>".getBytes();
        sampleXsdBytes = "<xsd>sample</xsd>".getBytes();
    }

    /**
     * Tests the createBIRType method with a list containing one BIR object.
     * Verifies that the method creates a BIR container with the provided BIR list,
     * initializes BIR info with integrity set to false, and returns a properly structured BIR.
     */
    @Test
    void createBIRTypeWithValidBirListShouldReturnCompleteBir() {
        BIR result = cbeffContainer.createBIRType(birList);

        assertNotNull(result);
        assertNotNull(result.getBirs());
        assertEquals(1, result.getBirs().size());
        assertEquals(sampleBir, result.getBirs().get(0));
        assertNotNull(result.getBirInfo());
        assertFalse(result.getBirInfo().getIntegrity());
    }

    /**
     * Tests the createBIRType method with an empty BIR list.
     * Verifies that the method handles empty lists properly and returns a BIR with empty BIR list.
     */
    @Test
    void createBIRTypeWithEmptyBirListShouldReturnBirWithEmptyList() {
        List<BIR> emptyList = new ArrayList<>();

        BIR result = cbeffContainer.createBIRType(emptyList);

        assertNotNull(result);
        assertNotNull(result.getBirs());
        assertEquals(0, result.getBirs().size());
        assertNotNull(result.getBirInfo());
    }

    /**
     * Tests the createBIRType method with multiple BIR objects.
     * Verifies that the method correctly handles multiple BIRs and maintains the order.
     */
    @Test
    void createBIRTypeWithMultipleBirsShouldReturnAllBirs() {
        BIR secondBir = new BIR();
        BIR thirdBir = new BIR();
        birList.add(secondBir);
        birList.add(thirdBir);

        BIR result = cbeffContainer.createBIRType(birList);

        assertNotNull(result);
        assertEquals(3, result.getBirs().size());
        assertEquals(sampleBir, result.getBirs().get(0));
        assertEquals(secondBir, result.getBirs().get(1));
        assertEquals(thirdBir, result.getBirs().get(2));
    }

    /**
     * Tests the updateBIRType method with valid XML bytes and BIR list.
     * Verifies that the method correctly updates an existing biometric record by adding new BIRs.
     */
    @Test
    void updateBIRTypeWithValidInputShouldAddBirsToExistingRecord() throws Exception {
        BIR existingBir = new BIR();
        existingBir.setBirs(new ArrayList<>());

        try (MockedStatic<CbeffValidator> mockedValidator = mockStatic(CbeffValidator.class)) {
            mockedValidator.when(() -> CbeffValidator.getBIRFromXML(sampleXmlBytes))
                    .thenReturn(existingBir);

            BIR result = cbeffContainer.updateBIRType(birList, sampleXmlBytes);

            assertNotNull(result);
            assertEquals(1, result.getBirs().size());
            assertEquals(sampleBir, result.getBirs().get(0));
        }
    }

    /**
     * Tests the updateBIRType method when CbeffValidator throws an exception.
     * Verifies that the method properly propagates exceptions from the validator.
     */
    @Test
    void updateBIRTypeWithInvalidXmlShouldThrowException() {
        try (MockedStatic<CbeffValidator> mockedValidator = mockStatic(CbeffValidator.class)) {
            mockedValidator.when(() -> CbeffValidator.getBIRFromXML(any(byte[].class)))
                    .thenThrow(new RuntimeException("Invalid XML"));

            assertThrows(RuntimeException.class, () -> {
                cbeffContainer.updateBIRType(birList, sampleXmlBytes);
            });
        }
    }

    /**
     * Tests the updateBIRType method with multiple BIRs to add.
     * Verifies that all BIRs from the list are added to the existing biometric record.
     */
    @Test
    void updateBIRTypeWithMultipleBirsShouldAddAllBirs() throws Exception {
        BIR existingBir = new BIR();
        List<BIR> existingBirList = new ArrayList<>();
        BIR alreadyExistingBir = new BIR();
        existingBirList.add(alreadyExistingBir);
        existingBir.setBirs(existingBirList);

        BIR secondBir = new BIR();
        birList.add(secondBir);

        try (MockedStatic<CbeffValidator> mockedValidator = mockStatic(CbeffValidator.class)) {
            mockedValidator.when(() -> CbeffValidator.getBIRFromXML(sampleXmlBytes))
                    .thenReturn(existingBir);

            BIR result = cbeffContainer.updateBIRType(birList, sampleXmlBytes);

            assertNotNull(result);
            assertEquals(3, result.getBirs().size());
            assertEquals(alreadyExistingBir, result.getBirs().get(0));
            assertEquals(sampleBir, result.getBirs().get(1));
            assertEquals(secondBir, result.getBirs().get(2));
        }
    }

    /**
     * Tests the validateXML method with valid XML and XSD bytes.
     * Verifies that the method correctly validates XML against XSD and returns true for valid XML.
     */
    @Test
    void validateXMLWithValidInputShouldReturnTrue() throws Exception {
        try (MockedStatic<CbeffXSDValidator> mockedValidator = mockStatic(CbeffXSDValidator.class)) {
            mockedValidator.when(() -> CbeffXSDValidator.validateXML(sampleXsdBytes, sampleXmlBytes))
                    .thenReturn(true);

            boolean result = cbeffContainer.validateXML(sampleXmlBytes, sampleXsdBytes);

            assertTrue(result);
        }
    }

    /**
     * Tests the validateXML method with invalid XML that doesn't conform to XSD.
     * Verifies that the method returns false when XML validation fails.
     */
    @Test
    void validateXMLWithInvalidInputShouldReturnFalse() throws Exception {
        try (MockedStatic<CbeffXSDValidator> mockedValidator = mockStatic(CbeffXSDValidator.class)) {
            mockedValidator.when(() -> CbeffXSDValidator.validateXML(sampleXsdBytes, sampleXmlBytes))
                    .thenReturn(false);

            boolean result = cbeffContainer.validateXML(sampleXmlBytes, sampleXsdBytes);

            assertFalse(result);
        }
    }

    /**
     * Tests the validateXML method when validation throws an exception.
     * Verifies that the method properly propagates exceptions from the XSD validator.
     */
    @Test
    void validateXMLWithExceptionShouldPropagateException() {
        try (MockedStatic<CbeffXSDValidator> mockedValidator = mockStatic(CbeffXSDValidator.class)) {
            mockedValidator.when(() -> CbeffXSDValidator.validateXML(any(byte[].class), any(byte[].class)))
                    .thenThrow(new RuntimeException("Validation error"));

            assertThrows(RuntimeException.class, () -> {
                cbeffContainer.validateXML(sampleXmlBytes, sampleXsdBytes);
            });
        }
    }

    /**
     * Tests the createBIRType method with null BIR list.
     * Verifies that the method handles null input gracefully.
     */
    @Test
    void createBIRTypeWithNullBirListShouldHandleGracefully() {
        BIR result = cbeffContainer.createBIRType(null);

        assertNotNull(result);
        assertNotNull(result.getBirInfo());
    }
}
