package io.mosip.print.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link VersionType}.
 */
class VersionTypeTest {

    /**
     * Verifies the no-args constructor creates an object with default values.
     */
    @Test
    void verifyDefaultConstructor() {
        VersionType version = new VersionType();
        assertEquals(0, version.getMajor());
        assertEquals(0, version.getMinor());
    }

    /**
     * Verifies the all-args constructor assigns fields correctly.
     */
    @Test
    void verifyAllArgsConstructor() {
        VersionType version = new VersionType(1, 2);
        assertEquals(1, version.getMajor());
        assertEquals(2, version.getMinor());
    }

    /**
     * Verifies the builder correctly sets fields and builds an object.
     */
    @Test
    void verifyBuilderConstructor() {
        VersionType version = new VersionType.VersionTypeBuilder()
                .withMajor(5)
                .withMinor(9)
                .build();
        assertEquals(5, version.getMajor());
        assertEquals(9, version.getMinor());
    }

    /**
     * Verifies getters and setters work as expected.
     */
    @Test
    void verifyGettersAndSetters() {
        VersionType version = new VersionType();
        version.setMajor(7);
        version.setMinor(8);
        assertEquals(7, version.getMajor());
        assertEquals(8, version.getMinor());
    }

    /**
     * Verifies equals and hashCode behavior across different scenarios.
     */
    @Test
    void verifyEqualsAndHashCode() {
        VersionType v1 = new VersionType(1, 2);
        VersionType v2 = new VersionType(1, 2);
        VersionType v3 = new VersionType(3, 4);

        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
        assertEquals(v1, v1);
        assertNotEquals(v1, null);
        assertNotEquals(v1, "string");
        assertNotEquals(v1, v3);
        assertNotEquals(v1.hashCode(), v3.hashCode());
    }

    /**
     * Verifies toString returns a non-null and informative string.
     */
    @Test
    void verifyToString() {
        VersionType version = new VersionType(11, 22);
        String out = version.toString();
        assertNotNull(out);
        assertTrue(out.contains("11"));
        assertTrue(out.contains("22"));
    }
}