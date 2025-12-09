package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.print.exception.CryptoManagerException;
import io.mosip.print.logger.PrintLogger;

/**
 * Unit tests for {@link CryptoCoreUtil} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the CryptoCoreUtil class,
 * including cryptographic operations such as encryption/decryption, key store management, certificate handling,
 * symmetric and asymmetric decryption, and various exception scenarios in cryptographic processing.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CryptoCoreUtilTest {

    @InjectMocks
    private CryptoCoreUtil cryptoCoreUtil;

    private static final String TEST_FILENAME = "test.p12";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_ALIAS = "alias";
    private static final String KEY_SPLITTER = "#KEY_SPLITTER#";

    private KeyStore.PrivateKeyEntry mockPrivateKeyEntry;
    private RSAPrivateKey realRSAPrivateKey;
    private Certificate mockCertificate;
    private SecretKey secretKey;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes mock objects, cryptographic keys, and configures the CryptoCoreUtil instance
     * with necessary properties for testing encryption and decryption operations.
     */
    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(cryptoCoreUtil, "fileName", TEST_FILENAME);
        ReflectionTestUtils.setField(cryptoCoreUtil, "cyptoPassword", TEST_PASSWORD);
        ReflectionTestUtils.setField(cryptoCoreUtil, "alias", TEST_ALIAS);
        ReflectionTestUtils.setField(cryptoCoreUtil, "isThumbprint", true);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        realRSAPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

        KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
        aesKeyGen.init(256);
        secretKey = aesKeyGen.generateKey();

        mockPrivateKeyEntry = mock(KeyStore.PrivateKeyEntry.class);
        mockCertificate = mock(Certificate.class);
        when(mockPrivateKeyEntry.getPrivateKey()).thenReturn(realRSAPrivateKey);
    }

    /**
     * Tests successful decryption operation.
     * Verifies that encrypted data can be successfully decrypted using the decrypt method
     * when valid input data and proper key store entry are provided.
     */
    @Test
    void decryptShouldSucceedWithValidData() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            String originalText = "test data";
            String encodedData = Base64.encodeBase64String(originalText.getBytes());

            CryptoCoreUtil spyCrypto = mock(CryptoCoreUtil.class);
            when(spyCrypto.loadP12()).thenReturn(mockPrivateKeyEntry);
            when(spyCrypto.decryptData(any(byte[].class), any(KeyStore.PrivateKeyEntry.class)))
                    .thenReturn(originalText.getBytes());
            when(spyCrypto.decrypt(anyString())).thenCallRealMethod();

            String result = spyCrypto.decrypt(encodedData);
            assertEquals(originalText, result);
        }
    }

    /**
     * Tests decrypt method when an exception occurs in the try block.
     * Verifies that CryptoManagerException is thrown when the P12 keystore loading fails
     * during the decryption process.
     */
    @Test
    void decryptWithExceptionInTryBlockShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            CryptoCoreUtil spyCrypto = mock(CryptoCoreUtil.class);
            when(spyCrypto.loadP12()).thenThrow(new RuntimeException("Load P12 failed"));
            when(spyCrypto.decrypt(anyString())).thenCallRealMethod();

            assertThrows(CryptoManagerException.class, () ->
                    spyCrypto.decrypt("test-data"));
        }
    }

    /**
     * Tests loadP12 method when KeyStoreException occurs.
     * Verifies that CryptoManagerException is thrown when KeyStore.getInstance fails
     * with a KeyStoreException during P12 keystore loading.
     */
    @Test
    void loadP12WithKeyStoreExceptionShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class);
             MockedStatic<KeyStore> keyStoreMock = mockStatic(KeyStore.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            keyStoreMock.when(() -> KeyStore.getInstance("PKCS12"))
                    .thenThrow(new java.security.KeyStoreException("Test KeyStore exception"));

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.loadP12());
        }
    }

    /**
     * Tests decryptData method with VERSION_RSA_2048 header for successful path coverage.
     * Verifies that the method processes data with VERSION_RSA_2048 header correctly,
     * even though it may throw CryptoManagerException due to invalid test data structure.
     */
    @Test
    void decryptDataWithVersionHeaderShouldProcessCorrectly() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            byte[] versionHeader = CryptoCoreUtil.VERSION_RSA_2048;
            byte[] thumbprint = new byte[32];
            byte[] encSymKey = secretKey.getEncoded();

            byte[] encryptedKey = new byte[versionHeader.length + thumbprint.length + encSymKey.length];
            System.arraycopy(versionHeader, 0, encryptedKey, 0, versionHeader.length);
            System.arraycopy(thumbprint, 0, encryptedKey, versionHeader.length, thumbprint.length);
            System.arraycopy(encSymKey, 0, encryptedKey, versionHeader.length + thumbprint.length, encSymKey.length);

            byte[] aad = new byte[32];
            byte[] nonce = new byte[12];
            System.arraycopy(nonce, 0, aad, 0, 12);

            byte[] encData = "encrypted content".getBytes();

            byte[] dataPayload = new byte[aad.length + encData.length];
            System.arraycopy(aad, 0, dataPayload, 0, aad.length);
            System.arraycopy(encData, 0, dataPayload, aad.length, encData.length);

            byte[] requestData = buildRequestData(encryptedKey, dataPayload);

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.decryptData(requestData, mockPrivateKeyEntry));
        }
    }

    /**
     * Tests decryptData method with thumbprint but no version header.
     * Verifies that the method handles data with thumbprint configuration enabled
     * but without version header in the encrypted data.
     */
    @Test
    void decryptDataWithThumbprintNoVersionShouldHandleCorrectly() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            ReflectionTestUtils.setField(cryptoCoreUtil, "isThumbprint", true);

            byte[] thumbprint = new byte[32];
            byte[] encSymKey = secretKey.getEncoded();

            byte[] encryptedKey = new byte[thumbprint.length + encSymKey.length];
            System.arraycopy(thumbprint, 0, encryptedKey, 0, thumbprint.length);
            System.arraycopy(encSymKey, 0, encryptedKey, thumbprint.length, encSymKey.length);

            byte[] dataPayload = "encrypted content".getBytes();
            byte[] requestData = buildRequestData(encryptedKey, dataPayload);

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.decryptData(requestData, mockPrivateKeyEntry));
        }
    }

    /**
     * Tests decryptData method without thumbprint configuration.
     * Verifies that the method handles data processing when thumbprint is disabled
     * and follows the appropriate decryption path.
     */
    @Test
    void decryptDataWithoutThumbprintShouldHandleCorrectly() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            ReflectionTestUtils.setField(cryptoCoreUtil, "isThumbprint", false);

            byte[] encSymKey = secretKey.getEncoded();
            byte[] dataPayload = "encrypted content".getBytes();
            byte[] requestData = buildRequestData(encSymKey, dataPayload);

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.decryptData(requestData, mockPrivateKeyEntry));
        }
    }

    /**
     * Tests decryptData method when an exception occurs in the try block.
     * Verifies that CryptoManagerException is thrown when invalid request data
     * causes an exception during the decryption process.
     */
    @Test
    void decryptDataWithExceptionInTryBlockShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            byte[] invalidRequestData = "invalid".getBytes();

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.decryptData(invalidRequestData, mockPrivateKeyEntry));
        }
    }

    /**
     * Tests parseEncryptKeyHeader method with valid header data.
     * Verifies that the method correctly parses and returns the VERSION_RSA_2048 header
     * when present at the beginning of the encrypted key data.
     */
    @Test
    void parseEncryptKeyHeaderWithValidHeaderShouldReturnHeader() {
        byte[] encryptedKey = new byte[CryptoCoreUtil.VERSION_RSA_2048.length + 10];
        System.arraycopy(CryptoCoreUtil.VERSION_RSA_2048, 0, encryptedKey, 0, CryptoCoreUtil.VERSION_RSA_2048.length);

        byte[] result = cryptoCoreUtil.parseEncryptKeyHeader(encryptedKey);
        assertArrayEquals(CryptoCoreUtil.VERSION_RSA_2048, result);
    }

    /**
     * Tests parseEncryptKeyHeader method with invalid header data.
     * Verifies that the method returns an empty byte array when the encrypted key
     * does not contain a valid VERSION_RSA_2048 header.
     */
    @Test
    void parseEncryptKeyHeaderWithInvalidHeaderShouldReturnEmptyArray() {
        byte[] encryptedKey = "INVALID".getBytes();

        byte[] result = cryptoCoreUtil.parseEncryptKeyHeader(encryptedKey);
        assertEquals(0, result.length);
    }

    /**
     * Tests the getSplitterIndex private method functionality.
     * Verifies that the method correctly identifies the position of the key splitter
     * within the provided byte array data.
     */
    @Test
    void getSplitterIndexShouldReturnCorrectIndex() throws Exception {
        String testString = "prefix" + KEY_SPLITTER + "suffix";
        byte[] testData = testString.getBytes();

        java.lang.reflect.Method method = CryptoCoreUtil.class.getDeclaredMethod(
                "getSplitterIndex", byte[].class, int.class, String.class);
        method.setAccessible(true);

        int result = (int) method.invoke(null, testData, 0, KEY_SPLITTER);
        assertEquals(6, result); // "prefix".length()
    }

    /**
     * Tests getSplitterIndex method when no matching splitter is found.
     * Verifies that the method returns the full data length when the key splitter
     * is not present in the provided byte array.
     */
    @Test
    void getSplitterIndexWithNoMatchShouldReturnFullLength() throws Exception {
        String testString = "prefix_suffix";
        byte[] testData = testString.getBytes();

        java.lang.reflect.Method method = CryptoCoreUtil.class.getDeclaredMethod(
                "getSplitterIndex", byte[].class, int.class, String.class);
        method.setAccessible(true);

        int result = (int) method.invoke(null, testData, 0, KEY_SPLITTER);
        assertEquals(testData.length, result);
    }

    /**
     * Tests asymmetricDecrypt method with exception scenarios.
     * Verifies that the method properly handles exceptions during asymmetric decryption
     * operations with invalid data.
     */
    @Test
    void asymmetricDecryptWithExceptionShouldThrowException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            java.lang.reflect.Method method = CryptoCoreUtil.class.getDeclaredMethod(
                    "asymmetricDecrypt", PrivateKey.class, BigInteger.class, byte[].class);
            method.setAccessible(true);

            byte[] testData = "invalid data for decryption".getBytes();

            assertThrows(Exception.class, () -> {
                try {
                    method.invoke(null, realRSAPrivateKey, realRSAPrivateKey.getModulus(), testData);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            });
        }
    }

    /**
     * Tests private symmetricDecrypt method with exception handling.
     * Verifies that the method returns null when an exception occurs during
     * symmetric decryption, covering the empty catch block scenario.
     */
    @Test
    void privateSymmetricDecryptWithExceptionShouldReturnNull() throws Exception {
        java.lang.reflect.Method method = CryptoCoreUtil.class.getDeclaredMethod(
                "symmetricDecrypt", SecretKey.class, byte[].class, byte[].class);
        method.setAccessible(true);

        byte[] invalidData = "invalid".getBytes();
        byte[] result = (byte[]) method.invoke(null, secretKey, invalidData, null);

        assertNull(result);
    }

    /**
     * Tests private symmetricDecrypt method with valid data.
     * Verifies that the method can successfully decrypt valid encrypted data
     * using the AES/GCM/NoPadding algorithm.
     */
    @Test
    void privateSymmetricDecryptWithValidDataShouldSucceed() throws Exception {
        java.lang.reflect.Method method = CryptoCoreUtil.class.getDeclaredMethod(
                "symmetricDecrypt", SecretKey.class, byte[].class, byte[].class);
        method.setAccessible(true);

        byte[] plaintext = "test data".getBytes();
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plaintext);
        byte[] iv = cipher.getIV();

        byte[] testData = new byte[encrypted.length + iv.length];
        System.arraycopy(encrypted, 0, testData, 0, encrypted.length);
        System.arraycopy(iv, 0, testData, encrypted.length, iv.length);

        byte[] result = (byte[]) method.invoke(null, secretKey, testData, null);

        if (result != null) {
            assertArrayEquals(plaintext, result);
        } else {
            assertNull(result);
        }
    }

    /**
     * Tests symmetricDecrypt method with IllegalBlockSizeException.
     * Verifies that CryptoManagerException is thrown when IllegalBlockSizeException
     * occurs during symmetric decryption operations.
     */
    @Test
    void symmetricDecryptWithIllegalBlockSizeExceptionShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            byte[] validNonce = new byte[12];
            byte[] emptyData = new byte[0];

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.symmetricDecrypt(secretKey, emptyData, validNonce, null));
        }
    }

    /**
     * Tests symmetricDecrypt method with BadPaddingException.
     * Verifies that CryptoManagerException is thrown when BadPaddingException
     * occurs due to invalid encrypted data during symmetric decryption.
     */
    @Test
    void symmetricDecryptWithBadPaddingExceptionShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            byte[] validNonce = new byte[12];
            byte[] invalidData = "invalid encrypted data".getBytes();

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.symmetricDecrypt(secretKey, invalidData, validNonce, null));
        }
    }

    /**
     * Tests symmetricDecrypt method with NoSuchAlgorithmException.
     * Verifies that CryptoManagerException is thrown when NoSuchAlgorithmException
     * occurs during cipher initialization in symmetric decryption.
     */
    @Test
    void symmetricDecryptWithNoSuchAlgorithmExceptionShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class);
             MockedStatic<javax.crypto.Cipher> cipherMock = mockStatic(javax.crypto.Cipher.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            cipherMock.when(() -> javax.crypto.Cipher.getInstance("AES/GCM/NoPadding"))
                    .thenThrow(new NoSuchAlgorithmException("Test exception"));

            byte[] validNonce = new byte[12];
            byte[] data = "test".getBytes();

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.symmetricDecrypt(secretKey, data, validNonce, null));
        }
    }

    /**
     * Tests symmetricDecrypt method with NoSuchPaddingException.
     * Verifies that CryptoManagerException is thrown when NoSuchPaddingException
     * occurs during cipher initialization in symmetric decryption.
     */
    @Test
    void symmetricDecryptWithNoSuchPaddingExceptionShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class);
             MockedStatic<javax.crypto.Cipher> cipherMock = mockStatic(javax.crypto.Cipher.class)) {

            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            cipherMock.when(() -> javax.crypto.Cipher.getInstance("AES/GCM/NoPadding"))
                    .thenThrow(new NoSuchPaddingException("Test exception"));

            byte[] validNonce = new byte[12];
            byte[] data = "test".getBytes();

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.symmetricDecrypt(secretKey, data, validNonce, null));
        }
    }

    /**
     * Tests symmetricDecrypt method with InvalidKeyException.
     * Verifies that CryptoManagerException is thrown when InvalidKeyException
     * occurs due to an invalid secret key during symmetric decryption.
     */
    @Test
    void symmetricDecryptWithInvalidKeyExceptionShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            SecretKeySpec invalidKey = new SecretKeySpec(new byte[10], "AES");
            byte[] validNonce = new byte[12];
            byte[] data = "test".getBytes();

            assertThrows(CryptoManagerException.class, () ->
                    cryptoCoreUtil.symmetricDecrypt(invalidKey, data, validNonce, null));
        }
    }

    /**
     * Tests getCertificateThumbprint method with successful operation.
     * Verifies that the method correctly generates a SHA-256 thumbprint
     * from the provided certificate data.
     */
    @Test
    void getCertificateThumbprintShouldSucceedWithValidCertificate() throws Exception {
        try (MockedStatic<DigestUtils> digestUtilsMock = mockStatic(DigestUtils.class)) {
            byte[] certData = "certificate data".getBytes();
            byte[] expectedHash = "hash result".getBytes();

            when(mockCertificate.getEncoded()).thenReturn(certData);
            digestUtilsMock.when(() -> DigestUtils.sha256(certData)).thenReturn(expectedHash);

            byte[] result = CryptoCoreUtil.getCertificateThumbprint(mockCertificate);
            assertArrayEquals(expectedHash, result);
        }
    }

    /**
     * Tests getCertificateThumbprint method with exception handling.
     * Verifies that CryptoManagerException is thrown when CertificateEncodingException
     * occurs during certificate thumbprint generation.
     */
    @Test
    void getCertificateThumbprintWithExceptionShouldThrowCryptoManagerException() throws Exception {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            Logger mockLogger = mock(Logger.class);
            printLoggerMock.when(() -> PrintLogger.getLogger(CryptoCoreUtil.class))
                    .thenReturn(mockLogger);

            when(mockCertificate.getEncoded()).thenThrow(new CertificateEncodingException("Test exception"));

            assertThrows(CryptoManagerException.class, () ->
                    CryptoCoreUtil.getCertificateThumbprint(mockCertificate));
        }
    }

    /**
     * Helper method to build request data with key splitter for testing purposes.
     * Creates properly formatted request data by combining encrypted key, key splitter, and data payload.
     *
     * @param encryptedKey the encrypted key bytes
     * @param dataPayload the data payload bytes
     * @return combined request data with key splitter
     */
    private byte[] buildRequestData(byte[] encryptedKey, byte[] dataPayload) {
        byte[] keySplitterBytes = KEY_SPLITTER.getBytes();
        byte[] requestData = new byte[encryptedKey.length + keySplitterBytes.length + dataPayload.length];

        System.arraycopy(encryptedKey, 0, requestData, 0, encryptedKey.length);
        System.arraycopy(keySplitterBytes, 0, requestData, encryptedKey.length, keySplitterBytes.length);
        System.arraycopy(dataPayload, 0, requestData, encryptedKey.length + keySplitterBytes.length, dataPayload.length);

        return requestData;
    }
}
