package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link TemplateManagerUtil} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the TemplateManagerUtil class,
 * including input binding to VelocityContext, null and empty input handling, different data types processing,
 * complex object binding, and various edge cases for template context management operations.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
class TemplateManagerUtilTest {

    /**
     * Tests successful binding of input map to VelocityContext.
     * Verifies that the method correctly converts a map of key-value pairs into a VelocityContext
     * and all values are accessible through the context.
     */
    @Test
    void bindInputToContextShouldSucceedWithValidInputMap() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("name", "John Doe");
        inputMap.put("age", 30);
        inputMap.put("city", "New York");

        VelocityContext result = TemplateManagerUtil.bindInputToContext(inputMap);

        assertNotNull(result);
        assertEquals("John Doe", result.get("name"));
        assertEquals(30, result.get("age"));
        assertEquals("New York", result.get("city"));
    }

    /**
     * Tests binding functionality when null input map is provided.
     * Verifies that the method handles null input gracefully and returns null
     * when no input data is available.
     */
    @Test
    void bindInputToContextWithNullInputShouldReturnNull() {
        VelocityContext result = TemplateManagerUtil.bindInputToContext(null);

        assertNull(result);
    }

    /**
     * Tests binding functionality when empty input map is provided.
     * Verifies that the method returns null when an empty map is provided,
     * indicating no context data is available for template processing.
     */
    @Test
    void bindInputToContextWithEmptyInputShouldReturnNull() {
        Map<String, Object> emptyMap = new HashMap<>();

        VelocityContext result = TemplateManagerUtil.bindInputToContext(emptyMap);

        assertNull(result);
    }

    /**
     * Tests binding functionality with single key-value pair.
     * Verifies that the method correctly handles maps containing only one entry
     * and creates a proper VelocityContext with the single value.
     */
    @Test
    void bindInputToContextWithSingleEntryShouldCreateContext() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("title", "Mr.");

        VelocityContext result = TemplateManagerUtil.bindInputToContext(inputMap);

        assertNotNull(result);
        assertEquals("Mr.", result.get("title"));
    }

    /**
     * Tests binding functionality with different object types.
     * Verifies that the method correctly handles various data types including
     * strings, integers, booleans, doubles, and null values in the context.
     */
    @Test
    void bindInputToContextWithDifferentTypesShouldHandleAllTypes() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stringValue", "test");
        inputMap.put("intValue", 42);
        inputMap.put("boolValue", true);
        inputMap.put("doubleValue", 3.14);
        inputMap.put("nullValue", null);

        VelocityContext result = TemplateManagerUtil.bindInputToContext(inputMap);

        assertNotNull(result);
        assertEquals("test", result.get("stringValue"));
        assertEquals(42, result.get("intValue"));
        assertEquals(true, result.get("boolValue"));
        assertEquals(3.14, result.get("doubleValue"));
        assertNull(result.get("nullValue"));
    }

    /**
     * Tests binding functionality with complex objects.
     * Verifies that the method correctly handles complex data structures including
     * nested maps and arrays, preserving their structure in the VelocityContext.
     */
    @Test
    void bindInputToContextWithComplexObjectsShouldPreserveStructure() {
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("nestedKey", "nestedValue");

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("mapObject", nestedMap);
        inputMap.put("arrayObject", new String[]{"item1", "item2", "item3"});

        VelocityContext result = TemplateManagerUtil.bindInputToContext(inputMap);

        assertNotNull(result);
        assertEquals(nestedMap, result.get("mapObject"));
        assertNotNull(result.get("arrayObject"));
    }

    /**
     * Tests that binding preserves all map entries.
     * Verifies that the method correctly transfers all key-value pairs from the input map
     * to the VelocityContext without losing any data.
     */
    @Test
    void bindInputToContextShouldPreserveAllEntries() {
        Map<String, Object> inputMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            inputMap.put("key" + i, "value" + i);
        }

        VelocityContext result = TemplateManagerUtil.bindInputToContext(inputMap);

        assertNotNull(result);
        for (int i = 0; i < 10; i++) {
            assertEquals("value" + i, result.get("key" + i));
        }
    }

    /**
     * Tests binding functionality with special characters in keys and values.
     * Verifies that the method correctly handles keys and values containing
     * special characters, underscores, dashes, and alphanumeric combinations.
     */
    @Test
    void bindInputToContextWithSpecialCharactersShouldHandleCorrectly() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("key_with_underscore", "value with spaces");
        inputMap.put("key-with-dash", "value@with#special$characters");
        inputMap.put("keyWithNumbers123", "value123");

        VelocityContext result = TemplateManagerUtil.bindInputToContext(inputMap);

        assertNotNull(result);
        assertEquals("value with spaces", result.get("key_with_underscore"));
        assertEquals("value@with#special$characters", result.get("key-with-dash"));
        assertEquals("value123", result.get("keyWithNumbers123"));
    }

    /**
     * Tests that VelocityContext is properly initialized with input map.
     * Verifies the correct initialization of VelocityContext and proper handling
     * of both existing and non-existing keys in the context.
     */
    @Test
    void bindInputToContextShouldInitializeProperlyWithInputMap() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("template", "user-card");
        inputMap.put("version", "1.0");

        VelocityContext result = TemplateManagerUtil.bindInputToContext(inputMap);

        assertNotNull(result);
        assertEquals("user-card", result.get("template"));
        assertEquals("1.0", result.get("version"));
        assertNull(result.get("nonExistentKey"));
    }

    /**
     * Tests binding functionality with large input map.
     * Verifies that the method can handle large datasets efficiently and correctly
     * transfer all entries from a substantial input map to VelocityContext.
     */
    @Test
    void bindInputToContextWithLargeMapShouldHandleEfficiently() {
        Map<String, Object> inputMap = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            inputMap.put("largeKey" + i, "largeValue" + i);
        }

        VelocityContext result = TemplateManagerUtil.bindInputToContext(inputMap);

        assertNotNull(result);
        assertEquals("largeValue0", result.get("largeKey0"));
        assertEquals("largeValue999", result.get("largeKey999"));
        assertEquals("largeValue500", result.get("largeKey500"));
    }

    /**
     * Tests private constructor accessibility for code coverage.
     * Verifies that the private constructor can be accessed via reflection
     * and creates a valid instance of TemplateManagerUtil.
     */
    @Test
    void privateConstructorShouldCreateInstance() throws Exception {
        java.lang.reflect.Constructor<TemplateManagerUtil> constructor =
                TemplateManagerUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        TemplateManagerUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
