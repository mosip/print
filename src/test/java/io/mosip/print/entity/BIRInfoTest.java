package io.mosip.print.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link BIRInfo}
 */
public class BIRInfoTest {

    /**
     * Verifies creation via builder, Lombok methods, and constructor.
     */
    @Test
    void verifyBIRInfoMethods() {
        LocalDateTime now = LocalDateTime.now();
        byte[] payload = new byte[]{1, 2, 3};

        BIRInfo.BIRInfoBuilder builder = new BIRInfo.BIRInfoBuilder()
                .withCreator("creator1")
                .withIndex("index1")
                .withPayload(payload)
                .withIntegrity(true)
                .withCreationDate(now)
                .withNotValidBefore(now.minusDays(1))
                .withNotValidAfter(now.plusDays(1));

        BIRInfo info1 = builder.build();

        assertEquals("creator1", info1.getCreator());
        assertEquals("index1", info1.getIndex());
        assertArrayEquals(payload, info1.getPayload());
        assertEquals(true, info1.getIntegrity());
        assertEquals(now, info1.getCreationDate());
        assertEquals(now.minusDays(1), info1.getNotValidBefore());
        assertEquals(now.plusDays(1), info1.getNotValidAfter());

        BIRInfo info2 = new BIRInfo.BIRInfoBuilder()
                .withCreator("creator1")
                .withIndex("index1")
                .withPayload(payload)
                .withIntegrity(true)
                .withCreationDate(now)
                .withNotValidBefore(now.minusDays(1))
                .withNotValidAfter(now.plusDays(1))
                .build();

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
        assertNotNull(info1.toString());

        info2.setCreator("different");
        assertNotEquals(info1, info2);
        assertNotEquals(info1, null);
        assertNotEquals(info1, "string");
    }

    /**
     * Ensures setters and default constructor work as expected.
     */
    @Test
    void validateLombokSettersAndGetters() {
        BIRInfo info = new BIRInfo();
        LocalDateTime now = LocalDateTime.now();
        byte[] payload = new byte[]{9, 8, 7};

        info.setCreator("testCreator");
        info.setIndex("testIndex");
        info.setPayload(payload);
        info.setIntegrity(false);
        info.setCreationDate(now);
        info.setNotValidBefore(now);
        info.setNotValidAfter(now.plusDays(5));

        assertEquals("testCreator", info.getCreator());
        assertEquals("testIndex", info.getIndex());
        assertArrayEquals(payload, info.getPayload());
        assertFalse(info.getIntegrity());
        assertEquals(now, info.getCreationDate());
        assertEquals(now, info.getNotValidBefore());
        assertEquals(now.plusDays(5), info.getNotValidAfter());
    }
}
