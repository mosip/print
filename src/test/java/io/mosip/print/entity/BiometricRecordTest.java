package io.mosip.print.entity;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BiometricRecord}.
 */
public class BiometricRecordTest {

    /**
     * Validates constructors, accessors, mutators, equality, hash code, and string representation.
     */
    @Test
    void verifyBiometricRecordMethods() {
        VersionType version1 = new VersionType();
        VersionType cbeff1 = new VersionType();
        BIRInfo birInfo1 = new BIRInfo();
        BIR bir = new BIR();

        BiometricRecord record1 = new BiometricRecord();
        assertNotNull(record1.getSegments());
        assertTrue(record1.getSegments().isEmpty());

        record1.setVersion(version1);
        record1.setCbeffversion(cbeff1);
        record1.setBirInfo(birInfo1);
        record1.setSegments(Collections.singletonList(bir));

        assertEquals(version1, record1.getVersion());
        assertEquals(cbeff1, record1.getCbeffversion());
        assertEquals(birInfo1, record1.getBirInfo());
        assertEquals(1, record1.getSegments().size());
        assertEquals(bir, record1.getSegments().get(0));

        BiometricRecord record2 = new BiometricRecord(version1, cbeff1, birInfo1);
        assertNotNull(record2.getSegments());
        assertTrue(record2.getSegments().isEmpty());
        record2.setSegments(Collections.singletonList(bir));

        assertEquals(record1, record2);
        assertEquals(record1, record1);
        assertEquals(record1.hashCode(), record2.hashCode());
        assertNotNull(record1.toString());;

        VersionType diffVersion = new VersionType();
        diffVersion.setMajor(1);
        diffVersion.setMinor(0);
        BiometricRecord record3 = new BiometricRecord(diffVersion, cbeff1, birInfo1);
        record3.setSegments(Collections.singletonList(bir));
        assertNotEquals(record1, record3);
        assertNotEquals(record1.hashCode(), record3.hashCode());
    }
}