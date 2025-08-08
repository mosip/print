package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.gson.JsonSyntaxException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.mosip.print.dto.JsonValue;
import io.mosip.print.exception.FieldNotFoundException;
import io.mosip.print.exception.InstantanceCreationException;

/**
 * Unit tests for {@link JsonUtil} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the JsonUtil class,
 * including JSON parsing, object mapping, data conversion between different formats, exception handling,
 * and various edge cases for JSON processing operations.</p>
 */
@ExtendWith(MockitoExtension.class)
class JsonUtilTest {

    private JSONObject testJsonObject;
    private JSONArray testJsonArray;
    private LinkedHashMap<String, Object> testMap;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes JSON objects, arrays, and maps with test data for comprehensive testing
     * of JSON utility operations.
     */
    @BeforeEach
    void setUp() {
        testJsonObject = new JSONObject();
        testJsonArray = new JSONArray();
        testMap = new LinkedHashMap<>();

        testMap.put("name", "Test Name");
        testMap.put("age", 30);
        testJsonObject.put("identity", testMap);

        LinkedHashMap<String, Object> arrayItem1 = new LinkedHashMap<>();
        arrayItem1.put("language", "eng");
        arrayItem1.put("value", "English Value");

        LinkedHashMap<String, Object> arrayItem2 = new LinkedHashMap<>();
        arrayItem2.put("language", "ara");
        arrayItem2.put("value", "Arabic Value");

        ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(arrayItem1);
        arrayList.add(arrayItem2);

        testJsonObject.put("fullName", arrayList);

        testJsonArray.add(arrayItem1);
        testJsonArray.add(arrayItem2);
    }

    /**
     * Tests successful conversion of InputStream to Java Object.
     * Verifies that the method correctly parses JSON from InputStream and converts it
     * to the specified Java object type.
     */
    @Test
    void inputStreamToJavaObjectShouldSucceedWithValidJson() throws Exception {
        String jsonString = "{\"name\":\"Test\",\"value\":\"TestValue\"}";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes("UTF-8"));

        TestDto result = (TestDto) JsonUtil.inputStreamtoJavaObject(inputStream, TestDto.class);

        assertNotNull(result);
        assertEquals("Test", result.getName());
        assertEquals("TestValue", result.getValue());
    }

    /**
     * Tests InputStream to Java Object conversion with null class parameter.
     * Verifies that the method throws UnsupportedEncodingException when null class is provided.
     */
    @Test
    void inputStreamToJavaObjectWithNullClassShouldThrowException() throws Exception {
        String jsonString = "{\"name\":\"Test\"}";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes("UTF-8"));

        assertThrows(UnsupportedEncodingException.class, () ->
                JsonUtil.inputStreamtoJavaObject(inputStream, null)
        );
    }

    /**
     * Tests successful retrieval of JSON object from nested structure.
     * Verifies that the method correctly extracts a JSONObject from a parent JSONObject
     * using the specified key.
     */
    @Test
    void getJsonObjectShouldSucceedWithValidKey() {
        JSONObject result = JsonUtil.getJSONObject(testJsonObject, "identity");

        assertNotNull(result);
        assertEquals("Test Name", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    /**
     * Tests getJSONObject method with null input parameter.
     * Verifies that the method handles null JSONObject input gracefully and returns null.
     */
    @Test
    void getJsonObjectWithNullInputShouldReturnNull() {
        JSONObject result = JsonUtil.getJSONObject(null, "identity");

        assertNull(result);
    }

    /**
     * Tests getJSONObject method with non-existent key.
     * Verifies that the method returns null when the specified key is not found
     * in the JSONObject.
     */
    @Test
    void getJsonObjectWithNonExistentKeyShouldReturnNull() {
        JSONObject result = JsonUtil.getJSONObject(testJsonObject, "nonExistent");

        assertNull(result);
    }

    /**
     * Tests getJSONObject method with null key value.
     * Verifies that the method returns null when the key exists but its value is null.
     */
    @Test
    void getJsonObjectWithNullKeyValueShouldReturnNull() {
        testJsonObject.put("nullKey", null);
        JSONObject result = JsonUtil.getJSONObject(testJsonObject, "nullKey");

        assertNull(result);
    }

    /**
     * Tests successful retrieval of JSON array from JSONObject.
     * Verifies that the method correctly extracts a JSONArray from a JSONObject
     * using the specified key.
     */
    @Test
    void getJsonArrayShouldSucceedWithValidKey() {
        JSONArray result = JsonUtil.getJSONArray(testJsonObject, "fullName");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    /**
     * Tests getJSONArray method with null key.
     * Verifies that the method returns null when the specified key is not found
     * in the JSONObject.
     */
    @Test
    void getJsonArrayWithNullKeyShouldReturnNull() {
        JSONArray result = JsonUtil.getJSONArray(testJsonObject, "nonExistent");

        assertNull(result);
    }

    /**
     * Tests successful conversion of object to JSON string.
     * Verifies that the method correctly serializes a Java object to JSON string format.
     */
    @Test
    void writeValueAsStringShouldSucceedWithValidObject() throws IOException {
        TestDto testDto = new TestDto();
        testDto.setName("Test");
        testDto.setValue("TestValue");

        String result = JsonUtil.writeValueAsString(testDto);

        assertNotNull(result);
        assertTrue(result.contains("Test"));
        assertTrue(result.contains("TestValue"));
    }

    /**
     * Tests writeValueAsString method with null object.
     * Verifies that the method handles null input and returns "null" string.
     */
    @Test
    void writeValueAsStringWithNullShouldReturnNullString() throws IOException {
        String result = JsonUtil.writeValueAsString(null);

        assertEquals("null", result);
    }

    /**
     * Tests successful JSON string parsing to Java object.
     * Verifies that the method correctly deserializes JSON string to the specified Java object type.
     */
    @Test
    void readValueShouldSucceedWithValidJsonString() throws IOException {
        String jsonString = "{\"name\":\"Test\",\"value\":\"TestValue\"}";

        TestDto result = JsonUtil.readValue(jsonString, TestDto.class);

        assertNotNull(result);
        assertEquals("Test", result.getName());
        assertEquals("TestValue", result.getValue());
    }

    /**
     * Tests readValue method with invalid JSON string.
     * Verifies that the method throws IOException when provided with malformed JSON.
     */
    @Test
    void readValueWithInvalidJsonShouldThrowIOException() {
        String invalidJsonString = "{invalid json}";

        assertThrows(IOException.class, () ->
                JsonUtil.readValue(invalidJsonString, TestDto.class)
        );
    }

    /**
     * Tests getJSONValue method with null JSONObject parameter.
     * Verifies that the method handles null input gracefully and returns null.
     */
    @Test
    void getJsonValueWithNullJsonObjectShouldReturnNull() {
        Object result = JsonUtil.getJSONValue(null, "key");

        assertNull(result);
    }

    /**
     * Tests getJSONValue method with non-existent key.
     * Verifies that the method returns null when the specified key is not found.
     */
    @Test
    void getJsonValueWithNonExistentKeyShouldReturnNull() {
        Object result = JsonUtil.getJSONValue(testJsonObject, "nonExistent");

        assertNull(result);
    }

    /**
     * Tests successful extraction of JSONObject from JSONArray with LinkedHashMap.
     * Verifies that the method correctly converts LinkedHashMap to JSONObject
     * when extracting from JSONArray at specified index.
     */
    @Test
    void getJsonObjectFromArrayWithLinkedHashMapShouldSucceed() {
        JSONObject result = JsonUtil.getJSONObjectFromArray(testJsonArray, 0);

        assertNotNull(result);
        assertEquals("eng", result.get("language"));
        assertEquals("English Value", result.get("value"));
    }

    /**
     * Tests getJSONObjectFromArray method with existing JSONObject in array.
     * Verifies that the method correctly returns JSONObject when the array element
     * is already a JSONObject.
     */
    @Test
    void getJsonObjectFromArrayWithJsonObjectShouldReturnObject() {
        JSONArray jsonArrayWithObjects = new JSONArray();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("test", "value");
        jsonArrayWithObjects.add(jsonObj);

        JSONObject result = JsonUtil.getJSONObjectFromArray(jsonArrayWithObjects, 0);

        assertNotNull(result);
        assertEquals("value", result.get("test"));
    }

    /**
     * Tests successful object mapper read value operation.
     * Verifies that the method correctly deserializes JSON string using ObjectMapper.
     */
    @Test
    void objectMapperReadValueShouldSucceedWithValidJson() throws IOException {
        String jsonString = "{\"name\":\"Test\",\"value\":\"TestValue\"}";

        TestDto result = JsonUtil.objectMapperReadValue(jsonString, TestDto.class);

        assertNotNull(result);
        assertEquals("Test", result.getName());
        assertEquals("TestValue", result.getValue());
    }

    /**
     * Tests objectMapperReadValue method with invalid JSON.
     * Verifies that the method throws IOException when provided with malformed JSON.
     */
    @Test
    void objectMapperReadValueWithInvalidJsonShouldThrowIOException() {
        String invalidJsonString = "{invalid json}";

        assertThrows(IOException.class, () ->
                JsonUtil.objectMapperReadValue(invalidJsonString, TestDto.class)
        );
    }

    /**
     * Tests successful extraction of JsonValue array from JSONObject.
     * Verifies that the method correctly converts JSONArray to JsonValue array
     * with proper language and value mappings.
     */
    @Test
    void getJsonValuesShouldSucceedWithValidData() {
        JsonValue[] result = JsonUtil.getJsonValues(testJsonObject, "fullName");

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("eng", result[0].getLanguage());
        assertEquals("English Value", result[0].getValue());
        assertEquals("ara", result[1].getLanguage());
        assertEquals("Arabic Value", result[1].getValue());
    }

    /**
     * Tests getJsonValues method with null demographic identity.
     * Verifies that the method handles null JSONObject input and returns null.
     */
    @Test
    void getJsonValuesWithNullDemographicIdentityShouldReturnNull() {
        JsonValue[] result = JsonUtil.getJsonValues(null, "fullName");

        assertNull(result);
    }

    /**
     * Tests getJsonValues method with null JSONArray.
     * Verifies that the method returns null when the specified key does not exist
     * or contains null value.
     */
    @Test
    void getJsonValuesWithNullJsonArrayShouldReturnNull() {
        JsonValue[] result = JsonUtil.getJsonValues(testJsonObject, "nonExistent");

        assertNull(result);
    }

    /**
     * Tests successful mapping of JSONArray to Java object array.
     * Verifies that the method correctly converts JSONArray elements to JsonValue objects.
     */
    @Test
    void mapJsonNodeToJavaObjectShouldSucceedWithValidData() {
        JsonValue[] result = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, testJsonArray);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("eng", result[0].getLanguage());
        assertEquals("English Value", result[0].getValue());
    }

    /**
     * Tests mapJsonNodeToJavaObject method with instantiation exception.
     * Verifies that the method throws InstantanceCreationException when unable
     * to instantiate the target class (abstract class in this case).
     */
    @Test
    void mapJsonNodeToJavaObjectWithInstantiationExceptionShouldThrowException() {
        JSONArray jsonArray = new JSONArray();
        LinkedHashMap<String, Object> item = new LinkedHashMap<>();
        item.put("language", "eng");
        item.put("value", "test");
        jsonArray.add(item);

        assertThrows(InstantanceCreationException.class, () ->
                JsonUtil.mapJsonNodeToJavaObject(AbstractTestClass.class, jsonArray)
        );
    }

    /**
     * Tests mapJsonNodeToJavaObject method with field not found exception.
     * Verifies that the method throws FieldNotFoundException when target class
     * does not have the required fields for mapping.
     */
    @Test
    void mapJsonNodeToJavaObjectWithFieldNotFoundExceptionShouldThrowException() {
        JSONArray jsonArray = new JSONArray();
        LinkedHashMap<String, Object> item = new LinkedHashMap<>();
        item.put("language", "eng");
        item.put("value", "test");
        jsonArray.add(item);

        assertThrows(FieldNotFoundException.class, () ->
                JsonUtil.mapJsonNodeToJavaObject(TestDtoWithoutFields.class, jsonArray)
        );
    }

    /**
     * Tests mapJsonNodeToJavaObject method with null JSONObject in array.
     * Verifies that the method handles null elements in JSONArray and creates
     * corresponding null objects in the result array.
     */
    @Test
    void mapJsonNodeToJavaObjectWithNullJsonObjectShouldHandleGracefully() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(null);

        JsonValue[] result = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, jsonArray);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertNull(result[0]);
    }

    /**
     * Tests successful object to JSON conversion using ObjectMapper.
     * Verifies that the method correctly serializes Java object to JSON string
     * using ObjectMapper functionality.
     */
    @Test
    void objectMapperObjectToJsonShouldSucceedWithValidObject() throws IOException {
        TestDto testDto = new TestDto();
        testDto.setName("Test");
        testDto.setValue("TestValue");

        String result = JsonUtil.objectMapperObjectToJson(testDto);

        assertNotNull(result);
        assertTrue(result.contains("Test"));
        assertTrue(result.contains("TestValue"));
    }

    /**
     * Tests objectMapperObjectToJson method with null object.
     * Verifies that the method handles null input and returns "null" string.
     */
    @Test
    void objectMapperObjectToJsonWithNullShouldReturnNullString() throws IOException {
        String result = JsonUtil.objectMapperObjectToJson(null);

        assertEquals("null", result);
    }

    /**
     * Tests objectMapperObjectToJson method with complex nested object.
     * Verifies that the method correctly serializes complex objects with nested properties.
     */
    @Test
    void objectMapperObjectToJsonWithComplexObjectShouldSucceed() throws IOException {
        ComplexTestDto complexDto = new ComplexTestDto();
        complexDto.setName("Complex");
        complexDto.setNestedDto(new TestDto("Nested", "NestedValue"));

        String result = JsonUtil.objectMapperObjectToJson(complexDto);

        assertNotNull(result);
        assertTrue(result.contains("Complex"));
        assertTrue(result.contains("Nested"));
    }

    /**
     * Tests private constructor accessibility for code coverage.
     * Verifies that the private constructor can be accessed via reflection
     * and creates a valid instance of JsonUtil.
     */
    @Test
    void privateConstructorShouldCreateInstance() throws Exception {
        java.lang.reflect.Constructor<JsonUtil> constructor = JsonUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        JsonUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }

    /**
     * Tests getJSONArray method with empty ArrayList.
     * Verifies that the method correctly handles empty ArrayList and returns
     * an empty JSONArray.
     */
    @Test
    void getJsonArrayWithEmptyArrayListShouldReturnEmptyArray() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("emptyArray", new ArrayList<>());

        JSONArray result = JsonUtil.getJSONArray(jsonObj, "emptyArray");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Tests inputStreamtoJavaObject method with invalid JSON that throws JsonSyntaxException.
     * Verifies that the method throws JsonSyntaxException when provided with malformed JSON.
     */
    @Test
    void inputStreamToJavaObjectWithInvalidJsonShouldThrowJsonSyntaxException() throws Exception {
        String invalidJsonString = "{invalid json}";
        InputStream inputStream = new ByteArrayInputStream(invalidJsonString.getBytes("UTF-8"));

        assertThrows(JsonSyntaxException.class, () ->
                JsonUtil.inputStreamtoJavaObject(inputStream, TestDto.class)
        );
    }

    /**
     * Tests inputStreamtoJavaObject method with malformed JSON causing Gson exception.
     * Verifies that the method throws JsonSyntaxException when Gson cannot parse the JSON.
     */
    @Test
    void inputStreamToJavaObjectWithGsonExceptionShouldThrowJsonSyntaxException() throws Exception {
        String malformedJson = "{\"name\":\"Test\",\"invalidField\":}";
        InputStream inputStream = new ByteArrayInputStream(malformedJson.getBytes("UTF-8"));

        assertThrows(JsonSyntaxException.class, () ->
                JsonUtil.inputStreamtoJavaObject(inputStream, TestDto.class)
        );
    }

    /**
     * Tests successful getJSONValue method with string value.
     * Verifies that the method correctly retrieves string values from JSONObject.
     */
    @Test
    void getJsonValueShouldSucceedWithStringValue() {
        testJsonObject.put("stringKey", "stringValue");

        String result = JsonUtil.getJSONValue(testJsonObject, "stringKey");

        assertNotNull(result);
        assertEquals("stringValue", result);
    }

    /**
     * Tests getJSONValue method with complex object (LinkedHashMap).
     * Verifies that the method correctly retrieves and returns complex objects
     * like LinkedHashMap from JSONObject.
     */
    @Test
    void getJsonValueWithComplexObjectShouldReturnObject() {
        LinkedHashMap<String, Object> complexValue = JsonUtil.getJSONValue(testJsonObject, "identity");

        assertNotNull(complexValue);
        assertEquals("Test Name", complexValue.get("name"));
        assertEquals(30, complexValue.get("age"));
    }

    /**
     * Tests inputStreamtoJavaObject method with exception handling.
     * Verifies that the method handles type conversion scenarios gracefully
     * when Gson encounters type mismatches.
     */
    @Test
    void inputStreamToJavaObjectWithWrappedExceptionShouldHandleGracefully() throws Exception {
        String jsonString = "{\"name\":\"Test\",\"value\":123}";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes("UTF-8"));

        TestDto result = (TestDto) JsonUtil.inputStreamtoJavaObject(inputStream, TestDto.class);

        assertNotNull(result);
    }

    /**
     * Helper class for testing non-serializable objects and exception scenarios.
     * Contains a non-serializable field that may cause Gson processing issues.
     */
    public static class NonSerializableClass {
        private final Object nonSerializableField = new Object() {
            private transient String value = "test";
        };

        public Object getNonSerializableField() {
            return nonSerializableField;
        }
    }

    /**
     * Helper class for testing basic JSON serialization and deserialization.
     * Contains simple string fields for name and value properties.
     */
    public static class TestDto {
        private String name;
        private String value;

        public TestDto() {}

        public TestDto(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    /**
     * Helper class for testing complex nested object serialization.
     * Contains a nested TestDto object to verify complex JSON processing.
     */
    public static class ComplexTestDto {
        private String name;
        private TestDto nestedDto;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public TestDto getNestedDto() { return nestedDto; }
        public void setNestedDto(TestDto nestedDto) { this.nestedDto = nestedDto; }
    }

    /**
     * Abstract helper class for testing instantiation exception scenarios.
     * Used to verify that the mapping method throws appropriate exceptions
     * when unable to instantiate abstract classes.
     */
    public abstract static class AbstractTestClass {
        private String language;
        private String value;
    }

    /**
     * Helper class for testing field not found exception scenarios.
     * Intentionally lacks 'language' and 'value' fields to trigger FieldNotFoundException.
     */
    public static class TestDtoWithoutFields {
        private String differentField;

        public String getDifferentField() { return differentField; }
        public void setDifferentField(String differentField) { this.differentField = differentField; }
    }
}