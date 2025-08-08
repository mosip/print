package io.mosip.print.entity;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BiometricRecord}
 */
class BiometricRecordTest {

    /**
     * Test default constructor initializes segments list.
     */
    @Test
    void defaultConstructorInitializesSegmentsList() {
        BiometricRecord record = new BiometricRecord();
        assertNotNull(record);
        assertNotNull(record.getSegments());
        assertTrue(record.getSegments().isEmpty());
        assertNull(record.getVersion());
        assertNull(record.getCbeffversion());
        assertNull(record.getBirInfo());
    }

    /**
     * Test parameterized constructor sets fields correctly.
     */
    @Test
    void parameterizedConstructorSetsAllFieldsCorrectly() {
        VersionType version = new VersionType();
        VersionType cbeffVersion = new VersionType();
        BIRInfo birInfo = new BIRInfo();

        BiometricRecord record = new BiometricRecord(version, cbeffVersion, birInfo);

        assertEquals(version, record.getVersion());
        assertEquals(cbeffVersion, record.getCbeffversion());
        assertEquals(birInfo, record.getBirInfo());
        assertNotNull(record.getSegments());
        assertTrue(record.getSegments().isEmpty());
    }

    /**
     * Test setters and getters for all fields.
     */
    @Test
    void settersAndGettersAssignAndRetrieveValues() {
        BiometricRecord record = new BiometricRecord();

        VersionType version = new VersionType();
        VersionType cbeffVersion = new VersionType();
        BIRInfo birInfo = new BIRInfo();
        BIR bir = new BIR();

        record.setVersion(version);
        record.setCbeffversion(cbeffVersion);
        record.setBirInfo(birInfo);
        record.setSegments(Collections.singletonList(bir));

        assertEquals(version, record.getVersion());
        assertEquals(cbeffVersion, record.getCbeffversion());
        assertEquals(birInfo, record.getBirInfo());
        assertEquals(1, record.getSegments().size());
        assertEquals(bir, record.getSegments().get(0));
    }

    /**
     * Test toString(), equals(), and hashCode() methods for consistency.
     */
    @Test
    void toStringEqualsHashCodeMethodsAreConsistent() {
        VersionType version = new VersionType();
        VersionType cbeffVersion = new VersionType();
        BIRInfo birInfo = new BIRInfo();

        BiometricRecord record1 = new BiometricRecord(version, cbeffVersion, birInfo);
        BiometricRecord record2 = new BiometricRecord(version, cbeffVersion, birInfo);

        assertEquals(record1, record2);
        assertEquals(record1.hashCode(), record2.hashCode());
        assertNotNull(record1.toString());
    }
}