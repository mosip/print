package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Test class for {@link EmptyCheckUtils}.
 *
 * This class provides comprehensive unit tests for the EmptyCheckUtils utility class,
 * covering all overloaded methods for null and empty checking across different data types
 * including Object, String, Collection, and Map. The tests ensure proper validation
 * of null values, empty values, and edge cases for each supported data type.
 *
 * The utility class provides static methods to safely check for null or empty values
 * without throwing NullPointerException, making it essential for defensive programming
 * and input validation scenarios.
 *
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmptyCheckUtilsTest {

    /**
     * Tests null object validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(Object)} returns true
     * when the provided object is null.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyObjectWithNull() throws Exception {
        assertTrue(EmptyCheckUtils.isNullEmpty((Object) null));
    }

    /**
     * Tests non-null object validation.
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(Object)} returns false
     * when the provided object is not null, regardless of the object type.
     * Note: This specifically tests the Object overload, not Collection overload.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyObjectWithNonNull() throws Exception {
        assertFalse(EmptyCheckUtils.isNullEmpty(new Object()));
        assertFalse(EmptyCheckUtils.isNullEmpty("test"));
        assertFalse(EmptyCheckUtils.isNullEmpty(123));
        assertFalse(EmptyCheckUtils.isNullEmpty(Boolean.TRUE));
        assertFalse(EmptyCheckUtils.isNullEmpty(new StringBuilder()));

        assertFalse(EmptyCheckUtils.isNullEmpty((Object) new ArrayList<>()));
        assertFalse(EmptyCheckUtils.isNullEmpty((Object) new HashMap<>()));
    }


    /**
     * Tests null string validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(String)} returns true
     * when the provided string is null.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyStringWithNull() throws Exception {
        assertTrue(EmptyCheckUtils.isNullEmpty((String) null));
    }

    /**
     * Tests empty string validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(String)} returns true
     * when the provided string is empty (zero length after trim).
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyStringWithEmpty() throws Exception {
        assertTrue(EmptyCheckUtils.isNullEmpty(""));
    }

    /**
     * Tests whitespace-only string validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(String)} returns true
     * when the provided string contains only whitespace characters, as they
     * are trimmed and result in zero length.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyStringWithWhitespace() throws Exception {
        assertTrue(EmptyCheckUtils.isNullEmpty("   "));
        assertTrue(EmptyCheckUtils.isNullEmpty("\t"));
        assertTrue(EmptyCheckUtils.isNullEmpty("\n"));
        assertTrue(EmptyCheckUtils.isNullEmpty("\r"));
        assertTrue(EmptyCheckUtils.isNullEmpty("  \t  \n  "));
    }

    /**
     * Tests non-empty string validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(String)} returns false
     * when the provided string contains actual content after trimming.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyStringWithContent() throws Exception {
        assertFalse(EmptyCheckUtils.isNullEmpty("test"));
        assertFalse(EmptyCheckUtils.isNullEmpty("  test  "));
        assertFalse(EmptyCheckUtils.isNullEmpty("a"));
        assertFalse(EmptyCheckUtils.isNullEmpty("123"));
    }

    /**
     * Tests null collection validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(Collection)} returns true
     * when the provided collection is null.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyCollectionWithNull() throws Exception {
        assertTrue(EmptyCheckUtils.isNullEmpty((Collection<?>) null));
    }

    /**
     * Tests empty collection validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(Collection)} returns true
     * when the provided collection is empty (contains no elements).
     * Tests with different collection types including List and Set.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyCollectionWithEmpty() throws Exception {
        assertTrue(EmptyCheckUtils.isNullEmpty(new ArrayList<>()));
        assertTrue(EmptyCheckUtils.isNullEmpty(new HashSet<>()));

        List<String> emptyList = new ArrayList<>();
        assertTrue(EmptyCheckUtils.isNullEmpty(emptyList));

        Set<Integer> emptySet = new HashSet<>();
        assertTrue(EmptyCheckUtils.isNullEmpty(emptySet));
    }

    /**
     * Tests non-empty collection validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(Collection)} returns false
     * when the provided collection contains at least one element.
     * Tests with different collection types and various content types.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyCollectionWithContent() throws Exception {
        assertFalse(EmptyCheckUtils.isNullEmpty(Arrays.asList("test")));
        assertFalse(EmptyCheckUtils.isNullEmpty(Arrays.asList(1, 2, 3)));

        List<String> nonEmptyList = new ArrayList<>();
        nonEmptyList.add("element");
        assertFalse(EmptyCheckUtils.isNullEmpty(nonEmptyList));

        Set<String> nonEmptySet = new HashSet<>();
        nonEmptySet.add("element");
        assertFalse(EmptyCheckUtils.isNullEmpty(nonEmptySet));
    }

    /**
     * Tests null map validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(Map)} returns true
     * when the provided map is null.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyMapWithNull() throws Exception {
        assertTrue(EmptyCheckUtils.isNullEmpty((Map<?, ?>) null));
    }

    /**
     * Tests empty map validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(Map)} returns true
     * when the provided map is empty (contains no key-value pairs).
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyMapWithEmpty() throws Exception {
        assertTrue(EmptyCheckUtils.isNullEmpty(new HashMap<>()));

        Map<String, String> emptyMap = new HashMap<>();
        assertTrue(EmptyCheckUtils.isNullEmpty(emptyMap));
    }

    /**
     * Tests non-empty map validation.
     *
     * Verifies that {@link EmptyCheckUtils#isNullEmpty(Map)} returns false
     * when the provided map contains at least one key-value pair.
     * Tests with different key-value types.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void isNullEmptyMapWithContent() throws Exception {
        Map<String, String> nonEmptyMap = new HashMap<>();
        nonEmptyMap.put("key", "value");
        assertFalse(EmptyCheckUtils.isNullEmpty(nonEmptyMap));

        Map<Integer, String> intStringMap = new HashMap<>();
        intStringMap.put(1, "one");
        assertFalse(EmptyCheckUtils.isNullEmpty(intStringMap));

        Map<String, Object> mixedMap = new HashMap<>();
        mixedMap.put("test", new Object());
        assertFalse(EmptyCheckUtils.isNullEmpty(mixedMap));
    }

    /**
     * Tests edge case scenarios across all method overloads.
     *
     * Verifies behavior with edge cases such as collections containing null elements,
     * maps with null keys or values, and special string characters.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void edgeCaseScenarios() throws Exception {
        List<String> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        assertFalse(EmptyCheckUtils.isNullEmpty(listWithNull));

        Map<String, String> mapWithNullValue = new HashMap<>();
        mapWithNullValue.put("key", null);
        assertFalse(EmptyCheckUtils.isNullEmpty(mapWithNullValue));

        Map<String, String> mapWithNullKey = new HashMap<>();
        mapWithNullKey.put(null, "value");
        assertFalse(EmptyCheckUtils.isNullEmpty(mapWithNullKey));

        assertFalse(EmptyCheckUtils.isNullEmpty("@#$%"));
        assertFalse(EmptyCheckUtils.isNullEmpty("测试")); // Unicode characters
    }

    /**
     * Tests method overloading disambiguation.
     *
     * Verifies that the correct overloaded method is called based on the parameter type,
     * ensuring that method resolution works as expected for different data types.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void methodOverloadingTest() throws Exception {
        String nullString = null;
        assertTrue(EmptyCheckUtils.isNullEmpty(nullString));
        Collection<String> nullCollection = null;
        assertTrue(EmptyCheckUtils.isNullEmpty(nullCollection));

        Map<String, String> nullMap = null;
        assertTrue(EmptyCheckUtils.isNullEmpty(nullMap));

        Integer nullInteger = null;
        assertTrue(EmptyCheckUtils.isNullEmpty(nullInteger));
    }
}
