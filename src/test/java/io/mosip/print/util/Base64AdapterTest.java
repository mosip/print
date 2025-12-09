package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import io.mosip.kernel.core.util.CryptoUtil;

/**
 * Unit tests for {@link Base64Adapter} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the Base64Adapter class,
 * including base64 encoding/decoding operations, null parameter handling, exception scenarios, and various
 * data size validations using marshalling and unmarshalling operations.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
class Base64AdapterTest {

    @InjectMocks
    private Base64Adapter base64Adapter;

    private static final String VALID_BASE64_STRING = "SGVsbG8gV29ybGQ=";
    private static final byte[] VALID_BYTE_ARRAY = "Hello World".getBytes();
    private static final String EMPTY_BASE64_STRING = "";
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Tests successful unmarshalling of valid base64 string.
     * Verifies that a valid base64 encoded string is correctly decoded to its original byte array.
     */
    @Test
    void unmarshalWithValidBase64StringShouldSucceed() throws Exception {
        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.decodeBase64(VALID_BASE64_STRING))
                    .thenReturn(VALID_BYTE_ARRAY);

            byte[] result = base64Adapter.unmarshal(VALID_BASE64_STRING);

            assertNotNull(result);
            assertArrayEquals(VALID_BYTE_ARRAY, result);
        }
    }

    /**
     * Tests unmarshalling with empty string input.
     * Verifies that an empty base64 string is handled correctly and returns an empty byte array.
     */
    @Test
    void unmarshalWithEmptyStringShouldReturnEmptyArray() throws Exception {
        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.decodeBase64(EMPTY_BASE64_STRING))
                    .thenReturn(EMPTY_BYTE_ARRAY);

            byte[] result = base64Adapter.unmarshal(EMPTY_BASE64_STRING);

            assertNotNull(result);
            assertArrayEquals(EMPTY_BYTE_ARRAY, result);
        }
    }

    /**
     * Tests unmarshalling with null input parameter.
     * Verifies that null input is handled gracefully and returns null without throwing exceptions.
     */
    @Test
    void unmarshalWithNullInputShouldReturnNull() throws Exception {
        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.decodeBase64(null))
                    .thenReturn(null);

            byte[] result = base64Adapter.unmarshal(null);

            assertEquals(null, result);
        }
    }

    /**
     * Tests unmarshalling when CryptoUtil throws an exception.
     * Verifies that exceptions from the underlying CryptoUtil are properly propagated.
     */
    @Test
    void unmarshalWithCryptoUtilExceptionShouldThrowException() throws Exception {
        String invalidBase64 = "invalid-base64-string";

        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.decodeBase64(invalidBase64))
                    .thenThrow(new IllegalArgumentException("Invalid base64 input"));

            assertThrows(IllegalArgumentException.class, () ->
                    base64Adapter.unmarshal(invalidBase64)
            );
        }
    }

    /**
     * Tests unmarshalling with different base64 encoded data.
     * Verifies that various base64 encoded strings are correctly decoded to their respective byte arrays.
     */
    @Test
    void unmarshalWithDifferentDataShouldDecodeCorrectly() throws Exception {
        String differentBase64 = "VGVzdCBEYXRh";
        byte[] expectedBytes = "Test Data".getBytes();

        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.decodeBase64(differentBase64))
                    .thenReturn(expectedBytes);

            byte[] result = base64Adapter.unmarshal(differentBase64);

            assertNotNull(result);
            assertArrayEquals(expectedBytes, result);
        }
    }

    /**
     * Tests successful marshalling of valid byte array.
     * Verifies that a byte array is correctly encoded to its base64 string representation.
     */
    @Test
    void marshalWithValidByteArrayShouldSucceed() throws Exception {
        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.encodeBase64String(VALID_BYTE_ARRAY))
                    .thenReturn(VALID_BASE64_STRING);

            String result = base64Adapter.marshal(VALID_BYTE_ARRAY);

            assertNotNull(result);
            assertEquals(VALID_BASE64_STRING, result);
        }
    }

    /**
     * Tests marshalling with empty byte array.
     * Verifies that an empty byte array is handled correctly and returns an empty base64 string.
     */
    @Test
    void marshalWithEmptyByteArrayShouldReturnEmptyString() throws Exception {
        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.encodeBase64String(EMPTY_BYTE_ARRAY))
                    .thenReturn(EMPTY_BASE64_STRING);

            String result = base64Adapter.marshal(EMPTY_BYTE_ARRAY);

            assertNotNull(result);
            assertEquals(EMPTY_BASE64_STRING, result);
        }
    }

    /**
     * Tests marshalling with null input parameter.
     * Verifies that null input is handled gracefully and returns null without throwing exceptions.
     */
    @Test
    void marshalWithNullInputShouldReturnNull() throws Exception {
        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.encodeBase64String(null))
                    .thenReturn(null);

            String result = base64Adapter.marshal(null);

            assertEquals(null, result);
        }
    }

    /**
     * Tests marshalling when CryptoUtil throws an exception.
     * Verifies that exceptions from the underlying CryptoUtil are properly propagated during encoding.
     */
    @Test
    void marshalWithCryptoUtilExceptionShouldThrowException() throws Exception {
        byte[] testData = "test".getBytes();

        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.encodeBase64String(testData))
                    .thenThrow(new RuntimeException("Encoding failed"));

            assertThrows(RuntimeException.class, () ->
                    base64Adapter.marshal(testData)
            );
        }
    }

    /**
     * Tests marshalling with different byte array data.
     * Verifies that various byte arrays are correctly encoded to their respective base64 string representations.
     */
    @Test
    void marshalWithDifferentDataShouldEncodeCorrectly() throws Exception {
        byte[] differentData = "Different Test Data".getBytes();
        String expectedBase64 = "RGlmZmVyZW50IFRlc3QgRGF0YQ==";

        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.encodeBase64String(differentData))
                    .thenReturn(expectedBase64);

            String result = base64Adapter.marshal(differentData);

            assertNotNull(result);
            assertEquals(expectedBase64, result);
        }
    }

    /**
     * Tests round trip conversion by marshalling then unmarshalling data.
     * Verifies that data remains consistent when encoded to base64 and then decoded back to bytes.
     */
    @Test
    void roundTripConversionShouldMaintainDataIntegrity() throws Exception {
        byte[] originalData = "Round Trip Test".getBytes();
        String base64Encoded = "Um91bmQgVHJpcCBUZXN0";

        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.encodeBase64String(originalData))
                    .thenReturn(base64Encoded);
            cryptoUtilMock.when(() -> CryptoUtil.decodeBase64(base64Encoded))
                    .thenReturn(originalData);

            String marshalled = base64Adapter.marshal(originalData);
            byte[] unmarshalled = base64Adapter.unmarshal(marshalled);

            assertEquals(base64Encoded, marshalled);
            assertArrayEquals(originalData, unmarshalled);
        }
    }

    /**
     * Tests marshalling with large byte array data.
     * Verifies that the adapter can handle large byte arrays correctly during base64 encoding.
     */
    @Test
    void marshalWithLargeByteArrayShouldHandleCorrectly() throws Exception {
        byte[] largeData = new byte[1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        String expectedLargeBase64 = "large-base64-encoded-string";

        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.encodeBase64String(largeData))
                    .thenReturn(expectedLargeBase64);

            String result = base64Adapter.marshal(largeData);

            assertNotNull(result);
            assertEquals(expectedLargeBase64, result);
        }
    }

    /**
     * Tests unmarshalling with large base64 string data.
     * Verifies that the adapter can handle large base64 strings correctly during decoding.
     */
    @Test
    void unmarshalWithLargeBase64StringShouldHandleCorrectly() throws Exception {
        String largeBase64 = "large-base64-string-with-lots-of-data";
        byte[] expectedLargeData = new byte[512];
        for (int i = 0; i < expectedLargeData.length; i++) {
            expectedLargeData[i] = (byte) (i % 128);
        }

        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.decodeBase64(largeBase64))
                    .thenReturn(expectedLargeData);

            byte[] result = base64Adapter.unmarshal(largeBase64);

            assertNotNull(result);
            assertArrayEquals(expectedLargeData, result);
        }
    }

    /**
     * Tests marshalling with single byte data.
     * Verifies that single byte arrays are correctly encoded to base64 format.
     */
    @Test
    void marshalWithSingleByteShouldEncodeCorrectly() throws Exception {
        byte[] singleByte = {42};
        String expectedSingleByteBase64 = "Kg==";

        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.encodeBase64String(singleByte))
                    .thenReturn(expectedSingleByteBase64);

            String result = base64Adapter.marshal(singleByte);

            assertNotNull(result);
            assertEquals(expectedSingleByteBase64, result);
        }
    }

    /**
     * Tests unmarshalling with padded base64 string.
     * Verifies that base64 strings with padding characters are correctly decoded.
     */
    @Test
    void unmarshalWithPaddedBase64ShouldDecodeCorrectly() throws Exception {
        String paddedBase64 = "UGFkZGVkIERhdGE=";
        byte[] expectedPaddedData = "Padded Data".getBytes();

        try (MockedStatic<CryptoUtil> cryptoUtilMock = mockStatic(CryptoUtil.class)) {
            cryptoUtilMock.when(() -> CryptoUtil.decodeBase64(paddedBase64))
                    .thenReturn(expectedPaddedData);

            byte[] result = base64Adapter.unmarshal(paddedBase64);

            assertNotNull(result);
            assertArrayEquals(expectedPaddedData, result);
        }
    }
}
