package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link DateAdapter} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the DateAdapter class,
 * including ISO date-time string parsing, timezone conversion operations, LocalDateTime marshalling,
 * unmarshalling processes, precision handling for various time formats, and edge cases for
 * date-time adapter operations with different timezone offsets and formatting scenarios.</p>
 */
@ExtendWith(MockitoExtension.class)
class DateAdapterTest {

    @InjectMocks
    private DateAdapter dateAdapter;

    private static final String ISO_DATE_TIME_STRING = "2025-08-03T10:30:45Z";
    private static final String ISO_DATE_TIME_WITH_OFFSET = "2025-08-03T10:30:45+05:30";
    private static final String ISO_DATE_TIME_WITH_NEGATIVE_OFFSET = "2025-08-03T10:30:45-05:00";
    private static final LocalDateTime TEST_LOCAL_DATE_TIME = LocalDateTime.of(2025, 8, 3, 10, 30, 45);
    private static final String EXPECTED_MARSHAL_OUTPUT = "2025-08-03T10:30:45Z";

    /**
     * Tests successful unmarshalling of ISO date-time string with UTC timezone.
     * Verifies that the adapter correctly parses UTC timezone formatted date-time strings
     * and returns LocalDateTime with accurate year, month, day, hour, minute, and second values.
     */
    @Test
    void unmarshalShouldSucceedWithUtcTimezone() throws Exception {
        LocalDateTime result = dateAdapter.unmarshal(ISO_DATE_TIME_STRING);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    /**
     * Tests successful unmarshalling of ISO date-time string with positive timezone offset.
     * Verifies that the adapter correctly handles positive timezone offsets and converts
     * the time to UTC, adjusting the hour and minute values appropriately.
     */
    @Test
    void unmarshalShouldSucceedWithPositiveOffset() throws Exception {
        LocalDateTime result = dateAdapter.unmarshal(ISO_DATE_TIME_WITH_OFFSET);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
        assertEquals(5, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    /**
     * Tests successful unmarshalling of ISO date-time string with negative timezone offset.
     * Verifies that the adapter correctly handles negative timezone offsets and converts
     * the time to UTC, adjusting the hour and minute values appropriately.
     */
    @Test
    void unmarshalShouldSucceedWithNegativeOffset() throws Exception {
        LocalDateTime result = dateAdapter.unmarshal(ISO_DATE_TIME_WITH_NEGATIVE_OFFSET);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
        assertEquals(15, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    /**
     * Tests unmarshalling functionality with different date format including milliseconds.
     * Verifies that the adapter correctly parses date-time strings with millisecond precision
     * and preserves the nanosecond component accurately.
     */
    @Test
    void unmarshalWithDifferentDateFormatShouldHandleMilliseconds() throws Exception {
        String dateWithMilliseconds = "2025-08-03T10:30:45.123Z";

        LocalDateTime result = dateAdapter.unmarshal(dateWithMilliseconds);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
        assertEquals(123000000, result.getNano());
    }

    /**
     * Tests unmarshalling functionality with microseconds precision.
     * Verifies that the adapter correctly handles date-time strings with microsecond precision
     * and accurately preserves the nanosecond component with microsecond granularity.
     */
    @Test
    void unmarshalWithMicrosecondsPrecisionShouldPreservePrecision() throws Exception {
        String dateWithMicroseconds = "2025-08-03T10:30:45.123456Z";

        LocalDateTime result = dateAdapter.unmarshal(dateWithMicroseconds);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
        assertEquals(123456000, result.getNano());
    }

    /**
     * Tests unmarshalling functionality with nanoseconds precision.
     * Verifies that the adapter correctly handles date-time strings with full nanosecond precision
     * and accurately preserves all nanosecond digits in the LocalDateTime object.
     */
    @Test
    void unmarshalWithNanosecondsPrecisionShouldPreserveFullPrecision() throws Exception {
        String dateWithNanoseconds = "2025-08-03T10:30:45.123456789Z";

        LocalDateTime result = dateAdapter.unmarshal(dateWithNanoseconds);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
        assertEquals(123456789, result.getNano());
    }

    /**
     * Tests unmarshalling functionality with invalid date format input.
     * Verifies that DateTimeParseException is thrown when the adapter encounters
     * malformed or invalid date-time string formats.
     */
    @Test
    void unmarshalWithInvalidDateFormatShouldThrowDateTimeParseException() {
        String invalidDateString = "invalid-date-format";

        assertThrows(DateTimeParseException.class, () ->
                dateAdapter.unmarshal(invalidDateString)
        );
    }

    /**
     * Tests unmarshalling functionality with null input parameter.
     * Verifies that the adapter throws an appropriate exception when null input
     * is provided for date-time string unmarshalling.
     */
    @Test
    void unmarshalWithNullInputShouldThrowException() {
        assertThrows(Exception.class, () ->
                dateAdapter.unmarshal(null)
        );
    }

    /**
     * Tests unmarshalling functionality with empty string input.
     * Verifies that DateTimeParseException is thrown when an empty string is provided
     * as input for date-time parsing operations.
     */
    @Test
    void unmarshalWithEmptyStringShouldThrowDateTimeParseException() {
        assertThrows(DateTimeParseException.class, () ->
                dateAdapter.unmarshal("")
        );
    }

    /**
     * Tests unmarshalling functionality with malformed ISO date values.
     * Verifies that DateTimeParseException is thrown when the date-time string contains
     * invalid values for month, day, hour, minute, or second components.
     */
    @Test
    void unmarshalWithMalformedIsoDateShouldThrowDateTimeParseException() {
        String malformedDate = "2025-13-45T25:70:70Z";

        assertThrows(DateTimeParseException.class, () ->
                dateAdapter.unmarshal(malformedDate)
        );
    }

    /**
     * Tests successful marshalling of LocalDateTime to ISO string format.
     * Verifies that the adapter correctly converts LocalDateTime objects to
     * ISO-formatted date-time strings with UTC timezone designation.
     */
    @Test
    void marshalShouldSucceedWithValidLocalDateTime() throws Exception {
        String result = dateAdapter.marshal(TEST_LOCAL_DATE_TIME);

        assertNotNull(result);
        assertEquals(EXPECTED_MARSHAL_OUTPUT, result);
    }

    /**
     * Tests marshalling functionality with different LocalDateTime values.
     * Verifies that the adapter correctly handles various LocalDateTime objects
     * and produces properly formatted ISO date-time strings.
     */
    @Test
    void marshalWithDifferentDateTimeShouldReturnCorrectFormat() throws Exception {
        LocalDateTime differentDateTime = LocalDateTime.of(2024, 12, 25, 23, 59, 59);

        String result = dateAdapter.marshal(differentDateTime);

        assertNotNull(result);
        assertEquals("2024-12-25T23:59:59Z", result);
    }

    /**
     * Tests marshalling functionality with LocalDateTime containing nanoseconds.
     * Verifies that the adapter preserves nanosecond precision when converting
     * LocalDateTime objects to ISO-formatted strings.
     */
    @Test
    void marshalWithNanosecondsShouldPreservePrecision() throws Exception {
        LocalDateTime dateTimeWithNanos = LocalDateTime.of(2025, 8, 3, 10, 30, 45, 123456789);

        String result = dateAdapter.marshal(dateTimeWithNanos);

        assertNotNull(result);
        assertEquals("2025-08-03T10:30:45.123456789Z", result);
    }

    /**
     * Tests marshalling functionality with LocalDateTime containing milliseconds.
     * Verifies that the adapter correctly formats LocalDateTime objects with millisecond
     * precision into ISO-formatted date-time strings.
     */
    @Test
    void marshalWithMillisecondsShouldPreserveMillisecondPrecision() throws Exception {
        LocalDateTime dateTimeWithMillis = LocalDateTime.of(2025, 8, 3, 10, 30, 45, 123000000);

        String result = dateAdapter.marshal(dateTimeWithMillis);

        assertNotNull(result);
        assertEquals("2025-08-03T10:30:45.123Z", result);
    }

    /**
     * Tests marshalling functionality with minimum LocalDateTime value.
     * Verifies that the adapter correctly handles epoch time (1970-01-01) and other
     * minimum date-time values in the marshalling process.
     */
    @Test
    void marshalWithMinDateTimeShouldHandleEpochTime() throws Exception {
        LocalDateTime minDateTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

        String result = dateAdapter.marshal(minDateTime);

        assertNotNull(result);
        assertEquals("1970-01-01T00:00:00Z", result);
    }

    /**
     * Tests marshalling functionality with leap year date values.
     * Verifies that the adapter correctly handles leap year dates (February 29th)
     * in the marshalling process without validation errors.
     */
    @Test
    void marshalWithLeapYearDateShouldHandleLeapYearCorrectly() throws Exception {
        LocalDateTime leapYearDateTime = LocalDateTime.of(2024, 2, 29, 12, 0, 0);

        String result = dateAdapter.marshal(leapYearDateTime);

        assertNotNull(result);
        assertEquals("2024-02-29T12:00:00Z", result);
    }

    /**
     * Tests marshalling functionality with null LocalDateTime parameter.
     * Verifies that the adapter throws an appropriate exception when null LocalDateTime
     * is provided for marshalling operations.
     */
    @Test
    void marshalWithNullDateTimeShouldThrowException() {
        assertThrows(Exception.class, () ->
                dateAdapter.marshal(null)
        );
    }

    /**
     * Tests round-trip conversion functionality (unmarshal then marshal).
     * Verifies that the adapter maintains data integrity when performing sequential
     * unmarshalling and marshalling operations on the same date-time value.
     */
    @Test
    void roundTripConversionShouldPreserveOriginalValue() throws Exception {
        String originalDateString = "2025-08-03T10:30:45.123Z";

        LocalDateTime unmarshalled = dateAdapter.unmarshal(originalDateString);
        String marshalled = dateAdapter.marshal(unmarshalled);

        assertEquals(originalDateString, marshalled);
    }

    /**
     * Tests round-trip conversion functionality with timezone offset.
     * Verifies that the adapter correctly converts timezone offset dates to UTC format
     * and maintains consistency in round-trip operations.
     */
    @Test
    void roundTripConversionWithOffsetShouldConvertToUtc() throws Exception {
        String originalDateString = "2025-08-03T15:30:45+05:00";

        LocalDateTime unmarshalled = dateAdapter.unmarshal(originalDateString);
        String marshalled = dateAdapter.marshal(unmarshalled);

        assertEquals("2025-08-03T10:30:45Z", marshalled);
    }

    /**
     * Tests that unmarshalling then marshalling preserves time precision.
     * Verifies that the adapter maintains full nanosecond precision throughout
     * the complete unmarshalling and marshalling cycle.
     */
    @Test
    void preserveTimePrecisionShouldMaintainNanosecondAccuracy() throws Exception {
        String originalWithNanos = "2025-08-03T10:30:45.123456789Z";

        LocalDateTime unmarshalled = dateAdapter.unmarshal(originalWithNanos);
        String marshalled = dateAdapter.marshal(unmarshalled);

        assertEquals(originalWithNanos, marshalled);
    }
}
