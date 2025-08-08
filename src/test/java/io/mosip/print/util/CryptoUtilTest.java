package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.print.dto.CryptoWithPinRequestDto;
import io.mosip.print.exception.CryptoManagerException;
import io.mosip.print.exception.ParseException;

/**
 * Unit tests for {@link CryptoUtil} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the CryptoUtil class,
 * including PIN-based decryption operations, cryptographic algorithm handling, exception scenarios,
 * key derivation functions, symmetric decryption with various data formats, and edge cases for
 * cryptographic operations with user authentication.</p>
 */
@ExtendWith(MockitoExtension.class)
class CryptoUtilTest {

    @InjectMocks
    private CryptoUtil cryptoUtil;

    private static final String TEST_PIN = "testPin123";
    private static final int SYMMETRIC_KEY_LENGTH = 256;
    private static final int ITERATIONS = 100000;
    private static final String PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA512";

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes the CryptoUtil instance with cryptographic configuration parameters
     * including symmetric key length, iteration count, and password hashing algorithm.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cryptoUtil, "symmetricKeyLength", SYMMETRIC_KEY_LENGTH);
        ReflectionTestUtils.setField(cryptoUtil, "iterations", ITERATIONS);
        ReflectionTestUtils.setField(cryptoUtil, "passwordAlgorithm", PASSWORD_ALGORITHM);
    }

    /**
     * Tests decryption with PIN using valid data structure but invalid encryption.
     * Verifies that the method handles properly formatted data that contains invalid
     * encryption content and throws appropriate exceptions.
     */
    @Test
    void decryptWithPinValidStructureShouldThrowExceptionForInvalidEncryption() throws Exception {
        String validStructureData = createValidStructureData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(validStructureData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption with PIN when NoSuchAlgorithmException occurs in hash method.
     * Verifies that CryptoManagerException is thrown when an invalid password hashing
     * algorithm is configured in the system.
     */
    @Test
    void decryptWithPinNoSuchAlgorithmExceptionShouldThrowCryptoManagerException() throws Exception {
        String validStructureData = createValidStructureData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(validStructureData);
        requestDto.setUserPin(TEST_PIN);

        ReflectionTestUtils.setField(cryptoUtil, "passwordAlgorithm", "INVALID_ALGORITHM");

        assertThrows(CryptoManagerException.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption with PIN when IllegalArgumentException occurs due to invalid key length.
     * Verifies that IllegalArgumentException is thrown when an invalid symmetric key length
     * is configured for the cryptographic operations.
     */
    @Test
    void decryptWithPinInvalidKeyLengthShouldThrowIllegalArgumentException() throws Exception {
        String validStructureData = createValidStructureData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(validStructureData);
        requestDto.setUserPin(TEST_PIN);

        ReflectionTestUtils.setField(cryptoUtil, "symmetricKeyLength", -1);

        assertThrows(IllegalArgumentException.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption with PIN when input data is too short.
     * Verifies that the method throws appropriate exceptions when the encrypted data
     * does not contain sufficient bytes for proper decryption processing.
     */
    @Test
    void decryptWithPinDataTooShortShouldThrowException() throws Exception {
        String shortData = Base64.encodeBase64String(new byte[30]);

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(shortData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption with PIN when InvalidKeyException occurs.
     * Verifies that the method handles invalid key scenarios appropriately during
     * the symmetric decryption process and throws expected exceptions.
     */
    @Test
    void decryptWithPinInvalidKeyShouldThrowException() throws Exception {
        String invalidKeyData = createInvalidKeyData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(invalidKeyData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption with PIN when InvalidAlgorithmParameterException occurs.
     * Verifies that the method handles invalid algorithm parameter scenarios during
     * cryptographic operations and throws appropriate exceptions.
     */
    @Test
    void decryptWithPinInvalidAlgorithmParameterShouldThrowException() throws Exception {
        String invalidParamData = createInvalidParameterData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(invalidParamData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption with PIN when IllegalBlockSizeException occurs.
     * Verifies that the method handles illegal block size scenarios during symmetric
     * decryption operations and throws appropriate exceptions.
     */
    @Test
    void decryptWithPinIllegalBlockSizeShouldThrowException() throws Exception {
        String invalidBlockData = createInvalidBlockSizeData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(invalidBlockData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption with PIN when BadPaddingException occurs.
     * Verifies that the method handles bad padding scenarios, typically caused by
     * incorrect PIN or corrupted encrypted data, and throws appropriate exceptions.
     */
    @Test
    void decryptWithPinBadPaddingShouldThrowException() throws Exception {
        String badPaddingData = createBadPaddingData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(badPaddingData);
        requestDto.setUserPin("wrongPin");

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests symmetric decrypt functionality with null IV path.
     * Verifies that the method handles null initialization vector scenarios
     * appropriately and throws expected exceptions during decryption.
     */
    @Test
    void symmetricDecryptNullIvPathShouldThrowException() throws Exception {
        String nullIvData = createNullIvTestData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(nullIvData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests symmetric decrypt when NoSuchAlgorithmException occurs in cipher creation.
     * Verifies that CryptoManagerException is thrown when the cryptographic algorithm
     * is not available during cipher initialization.
     */
    @Test
    void symmetricDecryptCipherNoSuchAlgorithmShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<javax.crypto.Cipher> cipherMock = mockStatic(javax.crypto.Cipher.class)) {
            cipherMock.when(() -> javax.crypto.Cipher.getInstance("AES/GCM/NoPadding"))
                    .thenThrow(new NoSuchAlgorithmException("Algorithm not found"));

            String validStructureData = createValidStructureData();

            CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
            requestDto.setData(validStructureData);
            requestDto.setUserPin(TEST_PIN);

            assertThrows(CryptoManagerException.class, () ->
                    cryptoUtil.decryptWithPin(requestDto)
            );
        }
    }

    /**
     * Tests symmetric decrypt when NoSuchPaddingException occurs in cipher creation.
     * Verifies that CryptoManagerException is thrown when the specified padding scheme
     * is not available during cipher initialization.
     */
    @Test
    void symmetricDecryptCipherNoSuchPaddingShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<javax.crypto.Cipher> cipherMock = mockStatic(javax.crypto.Cipher.class)) {
            cipherMock.when(() -> javax.crypto.Cipher.getInstance("AES/GCM/NoPadding"))
                    .thenThrow(new NoSuchPaddingException("Padding not found"));

            String validStructureData = createValidStructureData();

            CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
            requestDto.setData(validStructureData);
            requestDto.setUserPin(TEST_PIN);

            assertThrows(CryptoManagerException.class, () ->
                    cryptoUtil.decryptWithPin(requestDto)
            );
        }
    }

    /**
     * Tests static symmetric decrypt method with exception scenarios.
     * Verifies that the static decryption method handles various exception conditions
     * appropriately during cryptographic operations.
     */
    @Test
    void staticSymmetricDecryptExceptionShouldThrowException() throws Exception {
        String staticDecryptData = createStaticDecryptExceptionData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(staticDecryptData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests static symmetric decrypt with null AAD (Additional Authenticated Data).
     * Verifies that the method handles null AAD scenarios appropriately during
     * authenticated encryption decryption operations.
     */
    @Test
    void staticSymmetricDecryptNullAadShouldThrowException() throws Exception {
        String nullAadData = createNullAadData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(nullAadData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests static symmetric decrypt with empty AAD (Additional Authenticated Data).
     * Verifies that the method handles empty AAD scenarios appropriately during
     * authenticated encryption decryption operations.
     */
    @Test
    void staticSymmetricDecryptEmptyAadShouldThrowException() throws Exception {
        String emptyAadData = createEmptyAadData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(emptyAadData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests hash method functionality with invalid algorithm configuration.
     * Verifies that CryptoManagerException with NoSuchAlgorithmException cause is thrown
     * when an invalid password hashing algorithm is specified.
     */
    @Test
    void hashWithInvalidAlgorithmShouldThrowCryptoManagerExceptionWithNoSuchAlgorithmCause() throws Exception {
        ReflectionTestUtils.setField(cryptoUtil, "passwordAlgorithm", "INVALID_ALGORITHM");

        String validStructureData = createValidStructureData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(validStructureData);
        requestDto.setUserPin(TEST_PIN);

        CryptoManagerException exception = assertThrows(CryptoManagerException.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );

        assertTrue(exception.getCause() instanceof NoSuchAlgorithmException);
    }

    /**
     * Tests hash method with invalid iterations configuration.
     * Verifies that the method throws appropriate exceptions when invalid iteration
     * count is specified for the password-based key derivation function.
     */
    @Test
    void hashWithInvalidIterationsShouldThrowException() throws Exception {
        ReflectionTestUtils.setField(cryptoUtil, "iterations", 0);

        String validStructureData = createValidStructureData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(validStructureData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests hexDecode functionality with odd length string causing ParseException.
     * Verifies that ParseException is thrown when attempting to decode hexadecimal
     * strings with odd character lengths that cannot be properly parsed.
     */
    @Test
    void hexDecodeOddLengthShouldThrowParseException() throws Exception {
        try (MockedStatic<javax.xml.bind.DatatypeConverter> converterMock = mockStatic(javax.xml.bind.DatatypeConverter.class)) {
            converterMock.when(() -> javax.xml.bind.DatatypeConverter.printHexBinary(org.mockito.ArgumentMatchers.any()))
                    .thenReturn("ABC"); // Odd length hex string

            String validStructureData = createValidStructureData();

            CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
            requestDto.setData(validStructureData);
            requestDto.setUserPin(TEST_PIN);

            assertThrows(ParseException.class, () ->
                    cryptoUtil.decryptWithPin(requestDto)
            );
        }
    }

    /**
     * Tests decryption functionality with different symmetric key lengths.
     * Verifies that the method handles various key length configurations correctly
     * and throws appropriate exceptions for unsupported or invalid key sizes.
     */
    @Test
    void decryptWithDifferentKeyLengthShouldThrowException() throws Exception {
        ReflectionTestUtils.setField(cryptoUtil, "symmetricKeyLength", 128);

        String validStructureData = createValidStructureData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(validStructureData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption functionality with different iteration count configurations.
     * Verifies that the method handles various iteration count settings for
     * password-based key derivation and throws appropriate exceptions.
     */
    @Test
    void decryptWithDifferentIterationCountShouldThrowException() throws Exception {
        ReflectionTestUtils.setField(cryptoUtil, "iterations", 50000);

        String validStructureData = createValidStructureData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(validStructureData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests symmetric decrypt functionality with non-null AAD.
     * Verifies that the method handles Additional Authenticated Data scenarios
     * appropriately during authenticated encryption decryption operations.
     */
    @Test
    void symmetricDecryptWithNonNullAadShouldThrowException() throws Exception {
        String nonNullAadData = createNonNullAadData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(nonNullAadData);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption functionality with null input data.
     * Verifies that the method handles null data input appropriately and throws
     * expected exceptions when no encrypted data is provided.
     */
    @Test
    void decryptWithNullDataShouldThrowException() throws Exception {
        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(null);
        requestDto.setUserPin(TEST_PIN);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Tests decryption functionality with null PIN parameter.
     * Verifies that the method handles null PIN input appropriately and throws
     * expected exceptions when no user authentication PIN is provided.
     */
    @Test
    void decryptWithNullPinShouldThrowException() throws Exception {
        String validStructureData = createValidStructureData();

        CryptoWithPinRequestDto requestDto = new CryptoWithPinRequestDto();
        requestDto.setData(validStructureData);
        requestDto.setUserPin(null);

        assertThrows(Exception.class, () ->
                cryptoUtil.decryptWithPin(requestDto)
        );
    }

    /**
     * Helper method to create test data with valid structure for testing purposes.
     * Creates properly formatted encrypted data containing salt, nonce, and encrypted content
     * for use in cryptographic operation testing.
     *
     * @return Base64-encoded string containing valid structure test data
     */
    private String createValidStructureData() {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[32];

        Arrays.fill(salt, (byte) 1);
        Arrays.fill(nonce, (byte) 2);
        Arrays.fill(encryptedData, (byte) 3);

        byte[] combined = new byte[salt.length + nonce.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(nonce, 0, combined, salt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }

    /**
     * Helper method to create test data that would cause invalid key scenarios.
     * Creates data designed to trigger invalid key exceptions during cryptographic operations.
     *
     * @return Base64-encoded string containing invalid key test data
     */
    private String createInvalidKeyData() {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[16];

        Arrays.fill(salt, (byte) 255);
        Arrays.fill(nonce, (byte) 255);
        Arrays.fill(encryptedData, (byte) 255);

        byte[] combined = new byte[salt.length + nonce.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(nonce, 0, combined, salt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }

    /**
     * Helper method to create test data that would cause invalid algorithm parameter exceptions.
     * Creates data designed to trigger invalid algorithm parameter exceptions during operations.
     *
     * @return Base64-encoded string containing invalid parameter test data
     */
    private String createInvalidParameterData() {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[8];

        Arrays.fill(salt, (byte) 0);
        Arrays.fill(nonce, (byte) 0);
        Arrays.fill(encryptedData, (byte) 0);

        byte[] combined = new byte[salt.length + nonce.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(nonce, 0, combined, salt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }

    /**
     * Helper method to create test data that would cause invalid block size exceptions.
     * Creates data designed to trigger illegal block size exceptions during decryption.
     *
     * @return Base64-encoded string containing invalid block size test data
     */
    private String createInvalidBlockSizeData() {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[1];

        Arrays.fill(salt, (byte) 1);
        Arrays.fill(nonce, (byte) 1);
        Arrays.fill(encryptedData, (byte) 1);

        byte[] combined = new byte[salt.length + nonce.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(nonce, 0, combined, salt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }

    /**
     * Helper method to create test data that would cause bad padding exceptions.
     * Creates data designed to trigger bad padding exceptions during decryption operations.
     *
     * @return Base64-encoded string containing bad padding test data
     */
    private String createBadPaddingData() {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[64];

        Arrays.fill(salt, (byte) 128);
        Arrays.fill(nonce, (byte) 128);
        Arrays.fill(encryptedData, (byte) 128);

        byte[] combined = new byte[salt.length + nonce.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(nonce, 0, combined, salt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }

    /**
     * Helper method to create test data for null IV testing scenarios.
     * Creates data designed to test null initialization vector handling.
     *
     * @return Base64-encoded string containing null IV test data
     */
    private String createNullIvTestData() {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[24];

        Arrays.fill(nonce, (byte) 0);
        Arrays.fill(encryptedData, (byte) 4);

        byte[] combined = new byte[salt.length + nonce.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(nonce, 0, combined, salt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }

    /**
     * Helper method to create test data for static decrypt exception scenarios.
     * Creates data designed to trigger exceptions in static decryption methods.
     *
     * @return Base64-encoded string containing static decrypt exception test data
     */
    private String createStaticDecryptExceptionData() {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[48];

        Arrays.fill(salt, (byte) 200);
        Arrays.fill(nonce, (byte) 200);
        Arrays.fill(encryptedData, (byte) 200);

        byte[] combined = new byte[salt.length + nonce.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(nonce, 0, combined, salt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }

    /**
     * Helper method to create test data for null AAD testing scenarios.
     * Creates data designed to test null Additional Authenticated Data handling.
     *
     * @return Base64-encoded string containing null AAD test data
     */
    private String createNullAadData() {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[20];

        Arrays.fill(nonce, (byte) 50);
        Arrays.fill(encryptedData, (byte) 50);

        byte[] combined = new byte[salt.length + nonce.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(nonce, 0, combined, salt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }

    /**
     * Helper method to create test data for empty AAD testing scenarios.
     * Creates data designed to test empty Additional Authenticated Data handling.
     *
     * @return Base64-encoded string containing empty AAD test data
     */
    private String createEmptyAadData() {
        byte[] emptySalt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[36];

        Arrays.fill(nonce, (byte) 60);
        Arrays.fill(encryptedData, (byte) 60);

        byte[] combined = new byte[emptySalt.length + nonce.length + encryptedData.length];
        System.arraycopy(emptySalt, 0, combined, 0, emptySalt.length);
        System.arraycopy(nonce, 0, combined, emptySalt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, emptySalt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }

    /**
     * Helper method to create test data for non-null AAD testing scenarios.
     * Creates data designed to test non-null Additional Authenticated Data handling.
     *
     * @return Base64-encoded string containing non-null AAD test data
     */
    private String createNonNullAadData() {
        byte[] salt = new byte[32];
        byte[] nonce = new byte[12];
        byte[] encryptedData = new byte[28];

        Arrays.fill(salt, (byte) 70);
        Arrays.fill(nonce, (byte) 70);
        Arrays.fill(encryptedData, (byte) 70);

        byte[] combined = new byte[salt.length + nonce.length + encryptedData.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(nonce, 0, combined, salt.length, nonce.length);
        System.arraycopy(encryptedData, 0, combined, salt.length + nonce.length, encryptedData.length);

        return Base64.encodeBase64String(combined);
    }
}
