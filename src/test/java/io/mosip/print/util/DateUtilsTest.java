package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.mosip.print.exception.IllegalArgumentException;
import io.mosip.print.exception.NullPointerException;
import io.mosip.print.exception.ParseException;

/**
 * Unit tests for {@link DateUtils} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the DateUtils class,
 * including date arithmetic operations, date formatting with various patterns and locales, timezone conversions,
 * date comparison operations, ISO string conversions, UTC date-time handling, and various parsing operations
 * with exception handling scenarios for date and time utility methods.</p>
 */
@ExtendWith(MockitoExtension.class)
class DateUtilsTest {

    private Date testDate;
    private Date futureDate;
    private Date pastDate;
    private LocalDateTime testLocalDateTime;
    private LocalDateTime futureLocalDateTime;
    private LocalDateTime pastLocalDateTime;
    private Calendar testCalendar;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes various Date, LocalDateTime, and Calendar objects for comprehensive
     * testing of date utility operations including past, present, and future time instances.
     */
    @BeforeEach
    void setUp() {
        testDate = new Date();
        futureDate = new Date(testDate.getTime() + 86400000); // +1 day
        pastDate = new Date(testDate.getTime() - 86400000); // -1 day

        testLocalDateTime = LocalDateTime.now();
        futureLocalDateTime = testLocalDateTime.plusDays(1);
        pastLocalDateTime = testLocalDateTime.minusDays(1);

        testCalendar = Calendar.getInstance();
        testCalendar.setTime(testDate);
    }

    /**
     * Tests successful addition of days to a date.
     * Verifies that the addDays method correctly adds the specified number of days
     * to a given date and returns a future date.
     */
    @Test
    void addDaysShouldSucceedWithValidInput() {
        Date result = DateUtils.addDays(testDate, 5);

        assertNotNull(result);
        assertTrue(result.after(testDate));
    }

    /**
     * Tests addition of days when null date is provided.
     * Verifies that the method throws appropriate exception when null date is passed,
     * as Apache Commons DateUtils throws NullPointerException for null input.
     */
    @Test
    void addDaysWithNullDateShouldThrowException() {
        assertThrows(Exception.class, () ->
                DateUtils.addDays(null, 5)
        );
    }

    /**
     * Tests addition of negative days to a date.
     * Verifies that the addDays method correctly handles negative day values
     * and returns a past date when negative days are added.
     */
    @Test
    void addDaysWithNegativeDaysShouldReturnPastDate() {
        Date result = DateUtils.addDays(testDate, -3);

        assertNotNull(result);
        assertTrue(result.before(testDate));
    }

    /**
     * Tests successful addition of hours to a date.
     * Verifies that the addHours method correctly adds the specified number of hours
     * to a given date and returns a future time.
     */
    @Test
    void addHoursShouldSucceedWithValidInput() {
        Date result = DateUtils.addHours(testDate, 2);

        assertNotNull(result);
        assertTrue(result.after(testDate));
    }

    /**
     * Tests addition of hours when null date is provided.
     * Verifies that the method throws appropriate exception when null date is passed
     * to the addHours operation.
     */
    @Test
    void addHoursWithNullDateShouldThrowException() {
        assertThrows(Exception.class, () ->
                DateUtils.addHours(null, 2)
        );
    }

    /**
     * Tests successful addition of minutes to a date.
     * Verifies that the addMinutes method correctly adds the specified number of minutes
     * to a given date and returns a future time.
     */
    @Test
    void addMinutesShouldSucceedWithValidInput() {
        Date result = DateUtils.addMinutes(testDate, 30);

        assertNotNull(result);
        assertTrue(result.after(testDate));
    }

    /**
     * Tests addition of minutes when null date is provided.
     * Verifies that the method throws appropriate exception when null date is passed
     * to the addMinutes operation.
     */
    @Test
    void addMinutesWithNullDateShouldThrowException() {
        assertThrows(Exception.class, () ->
                DateUtils.addMinutes(null, 30)
        );
    }

    /**
     * Tests successful addition of seconds to a date.
     * Verifies that the addSeconds method correctly adds the specified number of seconds
     * to a given date and returns a future time.
     */
    @Test
    void addSecondsShouldSucceedWithValidInput() {
        Date result = DateUtils.addSeconds(testDate, 45);

        assertNotNull(result);
        assertTrue(result.after(testDate));
    }

    /**
     * Tests addition of seconds when null date is provided.
     * Verifies that the method throws appropriate exception when null date is passed
     * to the addSeconds operation.
     */
    @Test
    void addSecondsWithNullDateShouldThrowException() {
        assertThrows(Exception.class, () ->
                DateUtils.addSeconds(null, 45)
        );
    }

    /**
     * Tests successful date formatting with specified pattern.
     * Verifies that the formatDate method correctly formats a date using the provided
     * pattern and returns a properly formatted string.
     */
    @Test
    void formatDateWithPatternShouldSucceedWithValidInput() {
        String result = DateUtils.formatDate(testDate, "yyyy-MM-dd");

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Tests date formatting when null date is provided.
     * Verifies that IllegalArgumentException is thrown when null date is passed
     * to the formatDate method.
     */
    @Test
    void formatDateWithNullDateShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.formatDate(null, "yyyy-MM-dd")
        );
    }

    /**
     * Tests date formatting when null pattern is provided.
     * Verifies that IllegalArgumentException is thrown when null pattern is passed
     * to the formatDate method.
     */
    @Test
    void formatDateWithNullPatternShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.formatDate(testDate, null)
        );
    }

    /**
     * Tests successful date formatting with pattern and timezone.
     * Verifies that the formatDate method correctly formats a date using the provided
     * pattern and timezone, returning a properly formatted string.
     */
    @Test
    void formatDateWithTimezoneShouldSucceedWithValidInput() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        String result = DateUtils.formatDate(testDate, "yyyy-MM-dd HH:mm:ss", utc);

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    /**
     * Tests date formatting with null timezone parameter.
     * Verifies that the method handles null timezone gracefully as timezone is optional
     * and still produces a properly formatted date string.
     */
    @Test
    void formatDateWithNullTimezoneShouldHandleGracefully() {
        String result = DateUtils.formatDate(testDate, "yyyy-MM-dd", (TimeZone) null);

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Tests successful date formatting with pattern, timezone, and locale.
     * Verifies that the formatDate method correctly formats a date using all provided
     * parameters including locale-specific formatting rules.
     */
    @Test
    void formatDateWithTimezoneAndLocaleShouldSucceedWithValidInput() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Locale locale = Locale.US;
        String result = DateUtils.formatDate(testDate, "yyyy-MM-dd", utc, locale);

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Tests successful calendar formatting with specified pattern.
     * Verifies that the formatCalendar method correctly formats a calendar using
     * the provided pattern and returns a properly formatted string.
     */
    @Test
    void formatCalendarWithPatternShouldSucceedWithValidInput() {
        String result = DateUtils.formatCalendar(testCalendar, "yyyy-MM-dd");

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Tests calendar formatting when null calendar is provided.
     * Verifies that IllegalArgumentException is thrown when null calendar is passed
     * to the formatCalendar method.
     */
    @Test
    void formatCalendarWithNullCalendarShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.formatCalendar(null, "yyyy-MM-dd")
        );
    }

    /**
     * Tests successful calendar formatting with pattern and timezone.
     * Verifies that the formatCalendar method correctly formats a calendar using
     * the provided pattern and timezone parameters.
     */
    @Test
    void formatCalendarWithTimezoneShouldSucceedWithValidInput() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        String result = DateUtils.formatCalendar(testCalendar, "yyyy-MM-dd", utc);

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Tests successful calendar formatting with pattern and locale.
     * Verifies that the formatCalendar method correctly formats a calendar using
     * the provided pattern and locale-specific formatting rules.
     */
    @Test
    void formatCalendarWithLocaleShouldSucceedWithValidInput() {
        Locale locale = Locale.US;
        String result = DateUtils.formatCalendar(testCalendar, "yyyy-MM-dd", locale);

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Tests successful calendar formatting with pattern, timezone, and locale.
     * Verifies that the formatCalendar method correctly formats a calendar using all
     * provided parameters including timezone and locale-specific formatting.
     */
    @Test
    void formatCalendarWithTimezoneAndLocaleShouldSucceedWithValidInput() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Locale locale = Locale.US;
        String result = DateUtils.formatCalendar(testCalendar, "yyyy-MM-dd", utc, locale);

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Tests date after comparison returning true.
     * Verifies that the after method correctly identifies when the first date
     * comes after the second date in chronological order.
     */
    @Test
    void afterDateShouldReturnTrueForFutureDate() {
        boolean result = DateUtils.after(futureDate, testDate);
        assertTrue(result);
    }

    /**
     * Tests date after comparison returning false.
     * Verifies that the after method correctly returns false when the first date
     * comes before the second date in chronological order.
     */
    @Test
    void afterDateShouldReturnFalseForPastDate() {
        boolean result = DateUtils.after(pastDate, testDate);
        assertFalse(result);
    }

    /**
     * Tests date after comparison with null date parameter.
     * Verifies that IllegalArgumentException is thrown when null date is passed
     * to the after comparison method.
     */
    @Test
    void afterDateWithNullShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.after(null, testDate)
        );
    }

    /**
     * Tests date before comparison returning true.
     * Verifies that the before method correctly identifies when the first date
     * comes before the second date in chronological order.
     */
    @Test
    void beforeDateShouldReturnTrueForPastDate() {
        boolean result = DateUtils.before(pastDate, testDate);
        assertTrue(result);
    }

    /**
     * Tests date before comparison returning false.
     * Verifies that the before method correctly returns false when the first date
     * comes after the second date in chronological order.
     */
    @Test
    void beforeDateShouldReturnFalseForFutureDate() {
        boolean result = DateUtils.before(futureDate, testDate);
        assertFalse(result);
    }

    /**
     * Tests date before comparison with null date parameter.
     * Verifies that IllegalArgumentException is thrown when null date is passed
     * to the before comparison method.
     */
    @Test
    void beforeDateWithNullShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.before(null, testDate)
        );
    }

    /**
     * Tests same day comparison returning false for different days.
     * Verifies that the isSameDay method correctly identifies when two dates
     * are not on the same calendar day.
     */
    @Test
    void isSameDayShouldReturnFalseForDifferentDays() {
        boolean result = DateUtils.isSameDay(testDate, futureDate);
        assertFalse(result);
    }

    /**
     * Tests same day comparison with null date parameter.
     * Verifies that IllegalArgumentException is thrown when null date is passed
     * to the isSameDay comparison method.
     */
    @Test
    void isSameDayWithNullShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.isSameDay(null, testDate)
        );
    }

    /**
     * Tests same instant comparison returning true for identical times.
     * Verifies that the isSameInstant method correctly identifies when two dates
     * represent the exact same moment in time.
     */
    @Test
    void isSameInstantShouldReturnTrueForIdenticalTimes() {
        Date sameInstant = new Date(testDate.getTime());
        boolean result = DateUtils.isSameInstant(testDate, sameInstant);
        assertTrue(result);
    }

    /**
     * Tests same instant comparison returning false for different times.
     * Verifies that the isSameInstant method correctly returns false when two dates
     * represent different moments in time.
     */
    @Test
    void isSameInstantShouldReturnFalseForDifferentTimes() {
        boolean result = DateUtils.isSameInstant(testDate, futureDate);
        assertFalse(result);
    }

    /**
     * Tests same instant comparison with null date parameter.
     * Verifies that IllegalArgumentException is thrown when null date is passed
     * to the isSameInstant comparison method.
     */
    @Test
    void isSameInstantWithNullShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.isSameInstant(null, testDate)
        );
    }

    /**
     * Tests LocalDateTime after comparison returning true.
     * Verifies that the after method correctly identifies when the first LocalDateTime
     * comes after the second LocalDateTime in chronological order.
     */
    @Test
    void afterLocalDateTimeShouldReturnTrueForFutureDateTime() {
        boolean result = DateUtils.after(futureLocalDateTime, testLocalDateTime);
        assertTrue(result);
    }

    /**
     * Tests LocalDateTime after comparison with null parameter.
     * Verifies that IllegalArgumentException is thrown when null LocalDateTime is passed
     * to the after comparison method.
     */
    @Test
    void afterLocalDateTimeWithNullShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.after(null, testLocalDateTime)
        );
    }

    /**
     * Tests LocalDateTime before comparison returning true.
     * Verifies that the before method correctly identifies when the first LocalDateTime
     * comes before the second LocalDateTime in chronological order.
     */
    @Test
    void beforeLocalDateTimeShouldReturnTrueForPastDateTime() {
        boolean result = DateUtils.before(pastLocalDateTime, testLocalDateTime);
        assertTrue(result);
    }

    /**
     * Tests LocalDateTime before comparison with null parameter.
     * Verifies that IllegalArgumentException is thrown when null LocalDateTime is passed
     * to the before comparison method.
     */
    @Test
    void beforeLocalDateTimeWithNullShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.before(null, testLocalDateTime)
        );
    }

    /**
     * Tests LocalDateTime same day comparison returning true for same day.
     * Verifies that the isSameDay method correctly identifies when two LocalDateTime objects
     * represent the same calendar day, avoiding invalid hour values.
     */
    @Test
    void isSameDayLocalDateTimeShouldReturnTrueForSameDay() {
        int currentHour = testLocalDateTime.getHour();
        int newHour = currentHour < 22 ? currentHour + 2 : currentHour - 2;

        LocalDateTime sameDay = LocalDateTime.of(
                testLocalDateTime.getYear(),
                testLocalDateTime.getMonth(),
                testLocalDateTime.getDayOfMonth(),
                newHour,
                testLocalDateTime.getMinute()
        );

        boolean result = DateUtils.isSameDay(testLocalDateTime, sameDay);
        assertTrue(result);
    }

    /**
     * Tests LocalDateTime same day comparison with null parameter.
     * Verifies that IllegalArgumentException is thrown when null LocalDateTime is passed
     * to the isSameDay comparison method.
     */
    @Test
    void isSameDayLocalDateTimeWithNullShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.isSameDay(null, testLocalDateTime)
        );
    }

    /**
     * Tests LocalDateTime same instant comparison returning true.
     * Verifies that the isSameInstant method correctly identifies when two LocalDateTime objects
     * represent the exact same moment in time.
     */
    @Test
    void isSameInstantLocalDateTimeShouldReturnTrueForIdenticalTimes() {
        boolean result = DateUtils.isSameInstant(testLocalDateTime, testLocalDateTime);
        assertTrue(result);
    }

    /**
     * Tests LocalDateTime same instant comparison with null parameter.
     * Verifies that IllegalArgumentException is thrown when null LocalDateTime is passed
     * to the isSameInstant comparison method.
     */
    @Test
    void isSameInstantLocalDateTimeWithNullShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                DateUtils.isSameInstant(null, testLocalDateTime)
        );
    }

    /**
     * Tests LocalDateTime to ISO string conversion.
     * Verifies that the toISOString method correctly converts LocalDateTime objects
     * to ISO-formatted strings with proper 'T' separator.
     */
    @Test
    void toISOStringWithLocalDateTimeShouldReturnIsoFormattedString() {
        String result = DateUtils.toISOString(testLocalDateTime);

        assertNotNull(result);
        assertTrue(result.contains("T"));
    }

    /**
     * Tests Date to ISO string conversion.
     * Verifies that the toISOString method correctly converts Date objects
     * to ISO-formatted strings with UTC timezone designation.
     */
    @Test
    void toISOStringWithDateShouldReturnUtcFormattedString() {
        String result = DateUtils.toISOString(testDate);

        assertNotNull(result);
        assertTrue(result.endsWith("Z"));
    }

    /**
     * Tests LocalDateTime formatting to ISO string.
     * Verifies that the formatToISOString method correctly formats LocalDateTime objects
     * to ISO-formatted strings with UTC timezone designation.
     */
    @Test
    void formatToISOStringShouldReturnUtcFormattedString() {
        String result = DateUtils.formatToISOString(testLocalDateTime);

        assertNotNull(result);
        assertTrue(result.endsWith("Z"));
    }

    /**
     * Tests retrieval of current UTC date-time.
     * Verifies that the getUTCCurrentDateTime method returns a valid LocalDateTime
     * representing the current UTC time.
     */
    @Test
    void getUTCCurrentDateTimeShouldReturnCurrentUtcDateTime() {
        LocalDateTime result = DateUtils.getUTCCurrentDateTime();

        assertNotNull(result);
    }

    /**
     * Tests retrieval of current UTC date-time as string.
     * Verifies that the getUTCCurrentDateTimeString method returns a properly formatted
     * string representation of the current UTC date-time.
     */
    @Test
    void getUTCCurrentDateTimeStringShouldReturnFormattedString() {
        String result = DateUtils.getUTCCurrentDateTimeString();

        assertNotNull(result);
        assertTrue(result.contains("T"));
    }

    /**
     * Tests retrieval of current UTC date-time string with custom pattern.
     * Verifies that the getUTCCurrentDateTimeString method correctly formats the current
     * UTC date-time using the specified pattern.
     */
    @Test
    void getUTCCurrentDateTimeStringWithPatternShouldReturnCustomFormattedString() {
        String result = DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd");

        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    /**
     * Tests retrieval of current date-time string.
     * Verifies that the getCurrentDateTimeString method returns a properly formatted
     * string representation of the current date-time.
     */
    @Test
    void getCurrentDateTimeStringShouldReturnFormattedString() {
        String result = DateUtils.getCurrentDateTimeString();

        assertNotNull(result);
        assertTrue(result.contains("T"));
    }

    /**
     * Tests conversion of UTC string to LocalDateTime.
     * Verifies that the convertUTCToLocalDateTime method correctly parses UTC-formatted
     * strings and returns LocalDateTime with accurate date components.
     */
    @Test
    void convertUTCToLocalDateTimeShouldParseUtcString() {
        String utcString = "2025-08-03T10:30:45Z";
        LocalDateTime result = DateUtils.convertUTCToLocalDateTime(utcString);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
    }

    /**
     * Tests parsing of UTC string to LocalDateTime.
     * Verifies that the parseUTCToLocalDateTime method correctly processes UTC-formatted
     * strings and returns valid LocalDateTime objects.
     */
    @Test
    void parseUTCToLocalDateTimeShouldParseUtcString() {
        String utcString = "2025-08-03T10:30:45Z";
        LocalDateTime result = DateUtils.parseUTCToLocalDateTime(utcString);

        assertNotNull(result);
    }

    /**
     * Tests parsing to LocalDateTime with UTC pattern.
     * Verifies that the parseToLocalDateTime method correctly handles UTC-formatted
     * date-time strings with millisecond precision.
     */
    @Test
    void parseToLocalDateTimeWithUTCPatternShouldParseWithMilliseconds() {
        String dateTimeString = "2025-08-03T10:30:45.123Z";
        LocalDateTime result = DateUtils.parseToLocalDateTime(dateTimeString);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
    }

    /**
     * Tests parsing to LocalDateTime with ISO pattern fallback.
     * Verifies that the parseToLocalDateTime method correctly handles ISO-formatted
     * date-time strings using fallback pattern matching.
     */
    @Test
    void parseToLocalDateTimeWithISOPatternFallbackShouldParseIsoString() {
        String dateTimeString = "2025-08-03T10:30:45";
        LocalDateTime result = DateUtils.parseToLocalDateTime(dateTimeString);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
    }

    /**
     * Tests parsing UTC to LocalDateTime with custom pattern.
     * Verifies that the parseUTCToLocalDateTime method correctly processes date-time strings
     * using the specified custom pattern format.
     */
    @Test
    void parseUTCToLocalDateTimeWithPatternShouldParseCustomFormat() {
        String dateTimeString = "2025-08-03 10:30:45";
        String pattern = "yyyy-MM-dd HH:mm:ss";

        LocalDateTime result = DateUtils.parseUTCToLocalDateTime(dateTimeString, pattern);

        assertNotNull(result);
        assertEquals(2025, result.getYear());
    }

    /**
     * Tests parsing UTC to LocalDateTime with invalid pattern.
     * Verifies that ParseException is thrown when the date-time string does not match
     * the specified pattern format.
     */
    @Test
    void parseUTCToLocalDateTimeWithInvalidPatternShouldThrowParseException() {
        String dateTimeString = "invalid-date";
        String pattern = "yyyy-MM-dd HH:mm:ss";

        assertThrows(ParseException.class, () ->
                DateUtils.parseUTCToLocalDateTime(dateTimeString, pattern)
        );
    }

    /**
     * Tests parsing Date to LocalDateTime conversion.
     * Verifies that the parseDateToLocalDateTime method correctly converts Date objects
     * to LocalDateTime representations.
     */
    @Test
    void parseDateToLocalDateTimeShouldConvertDateToLocalDateTime() {
        LocalDateTime result = DateUtils.parseDateToLocalDateTime(testDate);

        assertNotNull(result);
    }

    /**
     * Tests parsing UTC string to Date object.
     * Verifies that the parseUTCToDate method correctly parses UTC-formatted strings
     * and returns valid Date objects.
     */
    @Test
    void parseUTCToDateShouldParseUtcStringToDate() {
        String utcString = "2025-08-03T10:30:45.123Z";
        Date result = DateUtils.parseUTCToDate(utcString);

        assertNotNull(result);
    }

    /**
     * Tests parsing UTC to Date with invalid string.
     * Verifies that ParseException is thrown when an invalid date-time string
     * is provided for UTC to Date parsing.
     */
    @Test
    void parseUTCToDateWithInvalidStringShouldThrowParseException() {
        String invalidString = "invalid-date";

        assertThrows(ParseException.class, () ->
                DateUtils.parseUTCToDate(invalidString)
        );
    }

    /**
     * Tests parsing UTC to Date with custom pattern.
     * Verifies that the parseUTCToDate method correctly processes date strings
     * using the specified custom pattern format.
     */
    @Test
    void parseUTCToDateWithPatternShouldParseCustomFormat() {
        String dateString = "2025-08-03 10:30:45";
        String pattern = "yyyy-MM-dd HH:mm:ss";

        Date result = DateUtils.parseUTCToDate(dateString, pattern);

        assertNotNull(result);
    }

    /**
     * Tests parsing UTC to Date with invalid pattern.
     * Verifies that ParseException is thrown when the date string does not match
     * the specified pattern during UTC to Date parsing.
     */
    @Test
    void parseUTCToDateWithInvalidPatternShouldThrowParseException() {
        String dateString = "invalid-date";
        String pattern = "yyyy-MM-dd HH:mm:ss";

        assertThrows(ParseException.class, () ->
                DateUtils.parseUTCToDate(dateString, pattern)
        );
    }

    /**
     * Tests parsing to Date with timezone specification.
     * Verifies that the parseToDate method correctly processes date strings
     * with the specified pattern and timezone parameters.
     */
    @Test
    void parseToDateWithTimezoneShouldParseWithTimezoneInfo() {
        String dateString = "2025-08-03 10:30:45";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        TimeZone timezone = TimeZone.getTimeZone("UTC");

        Date result = DateUtils.parseToDate(dateString, pattern, timezone);

        assertNotNull(result);
    }

    /**
     * Tests parsing to Date with timezone and invalid string.
     * Verifies that ParseException is thrown when an invalid date string is provided
     * for timezone-aware date parsing.
     */
    @Test
    void parseToDateWithTimezoneAndInvalidStringShouldThrowParseException() {
        String dateString = "invalid-date";
        String pattern = "yyyy-MM-dd HH:mm:ss";
        TimeZone timezone = TimeZone.getTimeZone("UTC");

        assertThrows(ParseException.class, () ->
                DateUtils.parseToDate(dateString, pattern, timezone)
        );
    }

    /**
     * Tests parsing to Date with specified pattern.
     * Verifies that the parseToDate method correctly processes date strings
     * using the specified pattern format.
     */
    @Test
    void parseToDateWithPatternShouldParseCorrectly() {
        String dateString = "2025-08-03";
        String pattern = "yyyy-MM-dd";

        Date result = DateUtils.parseToDate(dateString, pattern);

        assertNotNull(result);
    }

    /**
     * Tests parsing to Date with null date string.
     * Verifies that NullPointerException is thrown when null date string
     * is provided for date parsing operations.
     */
    @Test
    void parseToDateWithNullDateStringShouldThrowNullPointerException() {
        String pattern = "yyyy-MM-dd";

        assertThrows(NullPointerException.class, () ->
                DateUtils.parseToDate(null, pattern)
        );
    }

    /**
     * Tests parsing to Date with null pattern.
     * Verifies that NullPointerException is thrown when null pattern
     * is provided for date parsing operations.
     */
    @Test
    void parseToDateWithNullPatternShouldThrowNullPointerException() {
        String dateString = "2025-08-03";

        assertThrows(NullPointerException.class, () ->
                DateUtils.parseToDate(dateString, null)
        );
    }

    /**
     * Tests parsing to Date with invalid date string.
     * Verifies that ParseException is thrown when an invalid date string
     * is provided for date parsing operations.
     */
    @Test
    void parseToDateWithInvalidDateStringShouldThrowParseException() {
        String dateString = "invalid-date";
        String pattern = "yyyy-MM-dd";

        assertThrows(ParseException.class, () ->
                DateUtils.parseToDate(dateString, pattern)
        );
    }

    /**
     * Tests retrieval of UTC time string from Date object.
     * Verifies that the getUTCTimeFromDate method correctly converts Date objects
     * to UTC-formatted strings with proper timezone and separator indicators.
     */
    @Test
    void getUTCTimeFromDateShouldReturnUtcFormattedString() {
        String result = DateUtils.getUTCTimeFromDate(testDate);

        assertNotNull(result);
        assertTrue(result.endsWith("Z"));
        assertTrue(result.contains("T"));
    }

    /**
     * Tests private constructor accessibility for code coverage.
     * Verifies that the private constructor can be accessed via reflection
     * and creates a valid instance of DateUtils.
     */
    @Test
    void privateConstructorShouldCreateInstance() throws Exception {
        java.lang.reflect.Constructor<DateUtils> constructor = DateUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        DateUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
