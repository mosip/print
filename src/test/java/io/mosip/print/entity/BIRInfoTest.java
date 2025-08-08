package io.mosip.print.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Test class for {@link BIRInfo}
 */
class BIRInfoTest {

    private static final String TEST_CREATOR = "TestCreator";
    private static final String TEST_INDEX = "1";
    private static final byte[] TEST_PAYLOAD = "test payload".getBytes();
    private static final LocalDateTime TEST_DATE = LocalDateTime.now();

    /**
     * Tests that the builder creates a complete BIRInfo object with all fields set correctly.
     * Verifies that all provided field values are properly assigned during object construction.
     */
    @Test
    void builderWithAllFieldsShouldCreateCompleteBIRInfo() {
        BIRInfo birInfo = new BIRInfo.BIRInfoBuilder()
                .withCreator(TEST_CREATOR)
                .withIndex(TEST_INDEX)
                .withPayload(TEST_PAYLOAD)
                .withIntegrity(true)
                .withCreationDate(TEST_DATE)
                .withNotValidBefore(TEST_DATE.plusDays(1))
                .withNotValidAfter(TEST_DATE.plusYears(1))
                .build();

        assertAll("All fields should be set correctly",
                () -> assertEquals(TEST_CREATOR, birInfo.getCreator()),
                () -> assertEquals(TEST_INDEX, birInfo.getIndex()),
                () -> assertArrayEquals(TEST_PAYLOAD, birInfo.getPayload()),
                () -> assertTrue(birInfo.getIntegrity()),
                () -> assertEquals(TEST_DATE, birInfo.getCreationDate()),
                () -> assertEquals(TEST_DATE.plusDays(1), birInfo.getNotValidBefore()),
                () -> assertEquals(TEST_DATE.plusYears(1), birInfo.getNotValidAfter())
        );
    }

    /**
     * Tests that the builder creates a partial BIRInfo object with only required fields.
     * Verifies that unspecified fields remain null while specified fields are properly set.
     */
    @Test
    void builderWithRequiredFieldsShouldCreatePartialBIRInfo() {
        BIRInfo birInfo = new BIRInfo.BIRInfoBuilder()
                .withCreator(TEST_CREATOR)
                .withIndex(TEST_INDEX)
                .build();

        assertAll("Only specified fields should be set",
                () -> assertEquals(TEST_CREATOR, birInfo.getCreator()),
                () -> assertEquals(TEST_INDEX, birInfo.getIndex()),
                () -> assertNull(birInfo.getPayload()),
                () -> assertNull(birInfo.getIntegrity()),
                () -> assertNull(birInfo.getCreationDate()),
                () -> assertNull(birInfo.getNotValidBefore()),
                () -> assertNull(birInfo.getNotValidAfter())
        );
    }

    /**
     * Tests that the builder handles null values gracefully for all fields.
     * Verifies that explicitly setting null values doesn't cause errors and fields remain null.
     */
    @Test
    void builderWithNullValuesShouldHandleThemGracefully() {
        BIRInfo birInfo = new BIRInfo.BIRInfoBuilder()
                .withCreator(null)
                .withIndex(null)
                .withPayload(null)
                .withIntegrity(null)
                .withCreationDate(null)
                .withNotValidBefore(null)
                .withNotValidAfter(null)
                .build();

        assertAll("Null values should be handled gracefully",
                () -> assertNull(birInfo.getCreator()),
                () -> assertNull(birInfo.getIndex()),
                () -> assertNull(birInfo.getPayload()),
                () -> assertNull(birInfo.getIntegrity()),
                () -> assertNull(birInfo.getCreationDate()),
                () -> assertNull(birInfo.getNotValidBefore()),
                () -> assertNull(birInfo.getNotValidAfter())
        );
    }

    /**
     * Tests that the builder properly handles empty byte arrays for payload field.
     * Verifies that empty arrays are stored correctly without causing null pointer exceptions.
     */
    @Test
    void builderWithEmptyByteArrayShouldHandleIt() {
        byte[] emptyPayload = new byte[0];
        BIRInfo birInfo = new BIRInfo.BIRInfoBuilder()
                .withPayload(emptyPayload)
                .build();

        assertNotNull(birInfo.getPayload());
        assertEquals(0, birInfo.getPayload().length);
    }

    /**
     * Tests that multiple builds from the same builder create independent instances.
     * Verifies that modifying builder state doesn't affect previously created objects.
     */
    @Test
    void builderWithMultipleBuildsShouldCreateIndependentInstances() {
        BIRInfo.BIRInfoBuilder builder = new BIRInfo.BIRInfoBuilder()
                .withCreator("FirstCreator");

        BIRInfo first = builder.build();
        BIRInfo second = builder.withCreator("SecondCreator").build();

        assertNotSame(first, second);
        assertEquals("FirstCreator", first.getCreator());
        assertEquals("SecondCreator", second.getCreator());
    }

    /**
     * Tests that the no-args constructor initializes all fields as null.
     * Verifies the default state of a newly created BIRInfo object without using the builder.
     */
    @Test
    void noArgsConstructorShouldInitializeAllFieldsAsNull() {
        BIRInfo birInfo = new BIRInfo();

        assertAll("All fields should be null in no-args constructor",
                () -> assertNull(birInfo.getCreator()),
                () -> assertNull(birInfo.getIndex()),
                () -> assertNull(birInfo.getPayload()),
                () -> assertNull(birInfo.getIntegrity()),
                () -> assertNull(birInfo.getCreationDate()),
                () -> assertNull(birInfo.getNotValidBefore()),
                () -> assertNull(birInfo.getNotValidAfter())
        );
    }

    /**
     * Tests that the builder handles extreme date boundary values correctly.
     * Verifies that minimum and maximum LocalDateTime values can be set without errors.
     */
    @Test
    void builderShouldHandleDateBoundaries() {
        LocalDateTime minDate = LocalDateTime.MIN;
        LocalDateTime maxDate = LocalDateTime.MAX;

        BIRInfo birInfo = new BIRInfo.BIRInfoBuilder()
                .withCreationDate(minDate)
                .withNotValidBefore(minDate.plusDays(1))
                .withNotValidAfter(maxDate)
                .build();

        assertAll("Date boundaries should be handled correctly",
                () -> assertEquals(minDate, birInfo.getCreationDate()),
                () -> assertEquals(minDate.plusDays(1), birInfo.getNotValidBefore()),
                () -> assertEquals(maxDate, birInfo.getNotValidAfter())
        );
    }

    /**
     * Tests that equals() and hashCode() methods work correctly for BIRInfo objects.
     * Verifies object equality based on field values and proper hashCode implementation.
     */
    @Test
    void equalsAndHashCodeShouldWorkCorrectly() {
        BIRInfo birInfo1 = createTestBIRInfo();
        BIRInfo birInfo2 = createTestBIRInfo();
        BIRInfo different = new BIRInfo.BIRInfoBuilder()
                .withCreator("Different")
                .withIndex(TEST_INDEX)
                .withPayload(TEST_PAYLOAD)
                .withIntegrity(true)
                .withCreationDate(TEST_DATE)
                .withNotValidBefore(TEST_DATE.plusDays(1))
                .withNotValidAfter(TEST_DATE.plusYears(1))
                .build();

        assertAll("Equals and HashCode should work correctly",
                () -> assertEquals(birInfo1, birInfo2),
                () -> assertEquals(birInfo1.hashCode(), birInfo2.hashCode()),
                () -> assertNotEquals(birInfo1, different),
                () -> assertNotEquals(birInfo1.hashCode(), different.hashCode())
        );
    }

    /**
     * Tests that the toString() method contains all field information.
     * Verifies that the string representation includes all relevant field values.
     */
    @Test
    void toStringShouldContainAllFields() {
        BIRInfo birInfo = createTestBIRInfo();
        String toString = birInfo.toString();

        assertAll("toString should contain all fields",
                () -> assertTrue(toString.contains("creator=" + birInfo.getCreator())),
                () -> assertTrue(toString.contains("index=" + birInfo.getIndex())),
                () -> assertTrue(toString.contains("integrity=" + birInfo.getIntegrity())),
                () -> assertTrue(toString.contains("creationDate=" + birInfo.getCreationDate()))
        );
    }

    /**
     * Helper method to create a fully populated test BIRInfo object.
     *
     * @return BIRInfo object with all test values set
     */
    private BIRInfo createTestBIRInfo() {
        return new BIRInfo.BIRInfoBuilder()
                .withCreator(TEST_CREATOR)
                .withIndex(TEST_INDEX)
                .withPayload(TEST_PAYLOAD)
                .withIntegrity(true)
                .withCreationDate(TEST_DATE)
                .withNotValidBefore(TEST_DATE.plusDays(1))
                .withNotValidAfter(TEST_DATE.plusYears(1))
                .build();
    }
}
