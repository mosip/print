package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.print.exception.ExceptionUtils;

/**
 * Unit tests for {@link TokenHandlerUtil} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the TokenHandlerUtil class,
 * including JWT token validation, issuer verification, client ID validation, token expiration checking,
 * exception handling scenarios, and various edge cases for bearer token authentication operations.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
class TokenHandlerUtilTest {

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.jwt.token";
    private static final String ISSUER_URL = "https://issuer.example.com";
    private static final String CLIENT_ID = "test-client-id";
    private static final String DIFFERENT_ISSUER = "https://different-issuer.com";
    private static final String DIFFERENT_CLIENT_ID = "different-client-id";

    private DecodedJWT mockDecodedJWT;
    private Claim mockClaim;
    private Map<String, Claim> mockClaims;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes mock objects for JWT decoding, claims processing, and token validation
     * operations required for comprehensive token handler testing.
     */
    @BeforeEach
    void setUp() {
        mockDecodedJWT = mock(DecodedJWT.class);
        mockClaim = mock(Claim.class);
        mockClaims = new HashMap<>();
        mockClaims.put("clientId", mockClaim);
    }

    /**
     * Tests successful token validation with all valid parameters.
     * Verifies that the method correctly validates a JWT token when the issuer,
     * client ID, and expiration time are all valid and meet the expected criteria.
     */
    @Test
    void isValidBearerTokenShouldSucceedWithAllValidParameters() {
        Date futureDate = new Date(System.currentTimeMillis() + 3600000); // +1 hour
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        LocalDateTime currentTime = LocalDateTime.now();

        try (MockedStatic<JWT> jwtMock = mockStatic(JWT.class);
             MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class);
             MockedStatic<ExceptionUtils> exceptionUtilsMock = mockStatic(ExceptionUtils.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(TokenHandlerUtil.class))
                    .thenReturn(mockLogger);

            jwtMock.when(() -> JWT.decode(VALID_TOKEN)).thenReturn(mockDecodedJWT);
            when(mockDecodedJWT.getClaims()).thenReturn(mockClaims);
            when(mockDecodedJWT.getExpiresAt()).thenReturn(futureDate);
            when(mockDecodedJWT.getIssuer()).thenReturn(ISSUER_URL);
            when(mockClaim.asString()).thenReturn(CLIENT_ID);

            dateUtilsMock.when(() -> DateUtils.getUTCTimeFromDate(futureDate))
                    .thenReturn("2025-08-03T11:30:45.123Z");
            dateUtilsMock.when(() -> DateUtils.convertUTCToLocalDateTime("2025-08-03T11:30:45.123Z"))
                    .thenReturn(futureTime);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTime())
                    .thenReturn(currentTime);
            dateUtilsMock.when(() -> DateUtils.before(currentTime, futureTime))
                    .thenReturn(true);

            boolean result = TokenHandlerUtil.isValidBearerToken(VALID_TOKEN, ISSUER_URL, CLIENT_ID);

            assertTrue(result);
        }
    }

    /**
     * Tests token validation with invalid issuer.
     * Verifies that the method correctly rejects tokens when the issuer in the JWT
     * does not match the expected issuer URL.
     */
    @Test
    void isValidBearerTokenWithInvalidIssuerShouldReturnFalse() {
        Date futureDate = new Date(System.currentTimeMillis() + 3600000);

        try (MockedStatic<JWT> jwtMock = mockStatic(JWT.class);
             MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(TokenHandlerUtil.class))
                    .thenReturn(mockLogger);

            jwtMock.when(() -> JWT.decode(VALID_TOKEN)).thenReturn(mockDecodedJWT);
            when(mockDecodedJWT.getClaims()).thenReturn(mockClaims);
            when(mockDecodedJWT.getExpiresAt()).thenReturn(futureDate);
            when(mockDecodedJWT.getIssuer()).thenReturn(DIFFERENT_ISSUER);

            boolean result = TokenHandlerUtil.isValidBearerToken(VALID_TOKEN, ISSUER_URL, CLIENT_ID);

            assertFalse(result);
        }
    }

    /**
     * Tests token validation with expired token.
     * Verifies that the method correctly rejects tokens when the expiration time
     * is in the past, indicating the token has expired.
     */
    @Test
    void isValidBearerTokenWithExpiredTokenShouldReturnFalse() {
        Date pastDate = new Date(System.currentTimeMillis() - 3600000); // -1 hour
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        LocalDateTime currentTime = LocalDateTime.now();

        try (MockedStatic<JWT> jwtMock = mockStatic(JWT.class);
             MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(TokenHandlerUtil.class))
                    .thenReturn(mockLogger);

            jwtMock.when(() -> JWT.decode(VALID_TOKEN)).thenReturn(mockDecodedJWT);
            when(mockDecodedJWT.getClaims()).thenReturn(mockClaims);
            when(mockDecodedJWT.getExpiresAt()).thenReturn(pastDate);
            when(mockDecodedJWT.getIssuer()).thenReturn(ISSUER_URL);

            dateUtilsMock.when(() -> DateUtils.getUTCTimeFromDate(pastDate))
                    .thenReturn("2025-08-03T09:30:45.123Z");
            dateUtilsMock.when(() -> DateUtils.convertUTCToLocalDateTime("2025-08-03T09:30:45.123Z"))
                    .thenReturn(pastTime);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTime())
                    .thenReturn(currentTime);
            dateUtilsMock.when(() -> DateUtils.before(currentTime, pastTime))
                    .thenReturn(false);

            boolean result = TokenHandlerUtil.isValidBearerToken(VALID_TOKEN, ISSUER_URL, CLIENT_ID);

            assertFalse(result);
        }
    }

    /**
     * Tests token validation with invalid client ID.
     * Verifies that the method correctly rejects tokens when the client ID in the JWT
     * claims does not match the expected client ID.
     */
    @Test
    void isValidBearerTokenWithInvalidClientIdShouldReturnFalse() {
        Date futureDate = new Date(System.currentTimeMillis() + 3600000);
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        LocalDateTime currentTime = LocalDateTime.now();

        try (MockedStatic<JWT> jwtMock = mockStatic(JWT.class);
             MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(TokenHandlerUtil.class))
                    .thenReturn(mockLogger);

            jwtMock.when(() -> JWT.decode(VALID_TOKEN)).thenReturn(mockDecodedJWT);
            when(mockDecodedJWT.getClaims()).thenReturn(mockClaims);
            when(mockDecodedJWT.getExpiresAt()).thenReturn(futureDate);
            when(mockDecodedJWT.getIssuer()).thenReturn(ISSUER_URL);
            when(mockClaim.asString()).thenReturn(DIFFERENT_CLIENT_ID);

            dateUtilsMock.when(() -> DateUtils.getUTCTimeFromDate(futureDate))
                    .thenReturn("2025-08-03T11:30:45.123Z");
            dateUtilsMock.when(() -> DateUtils.convertUTCToLocalDateTime("2025-08-03T11:30:45.123Z"))
                    .thenReturn(futureTime);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTime())
                    .thenReturn(currentTime);
            dateUtilsMock.when(() -> DateUtils.before(currentTime, futureTime))
                    .thenReturn(true);

            boolean result = TokenHandlerUtil.isValidBearerToken(VALID_TOKEN, ISSUER_URL, CLIENT_ID);

            assertFalse(result);
        }
    }

    /**
     * Tests token validation when JWTDecodeException occurs.
     * Verifies that the method handles JWTDecodeException gracefully when JWT decoding
     * fails due to malformed token format and returns false.
     */
    @Test
    void isValidBearerTokenWithJwtDecodeExceptionShouldReturnFalse() {
        try (MockedStatic<JWT> jwtMock = mockStatic(JWT.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class);
             MockedStatic<ExceptionUtils> exceptionUtilsMock = mockStatic(ExceptionUtils.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(TokenHandlerUtil.class))
                    .thenReturn(mockLogger);

            JWTDecodeException jwtDecodeException = new JWTDecodeException("Invalid JWT token");
            jwtMock.when(() -> JWT.decode(INVALID_TOKEN)).thenThrow(jwtDecodeException);

            exceptionUtilsMock.when(() -> ExceptionUtils.getStackTrace(jwtDecodeException))
                    .thenReturn("\n\tat com.auth0.jwt.JWT.decode(JWT.java:123)");

            boolean result = TokenHandlerUtil.isValidBearerToken(INVALID_TOKEN, ISSUER_URL, CLIENT_ID);

            assertFalse(result);
        }
    }

    /**
     * Tests token validation when generic Exception occurs.
     * Verifies that the method handles unexpected exceptions gracefully during token
     * validation and returns false for any runtime errors.
     */
    @Test
    void isValidBearerTokenWithGenericExceptionShouldReturnFalse() {
        try (MockedStatic<JWT> jwtMock = mockStatic(JWT.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class);
             MockedStatic<ExceptionUtils> exceptionUtilsMock = mockStatic(ExceptionUtils.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(TokenHandlerUtil.class))
                    .thenReturn(mockLogger);

            RuntimeException genericException = new RuntimeException("Generic error");
            jwtMock.when(() -> JWT.decode(INVALID_TOKEN)).thenThrow(genericException);

            exceptionUtilsMock.when(() -> ExceptionUtils.getStackTrace(genericException))
                    .thenReturn("\n\tat io.mosip.print.util.TokenHandlerUtil.isValidBearerToken(TokenHandlerUtil.java:45)");

            boolean result = TokenHandlerUtil.isValidBearerToken(INVALID_TOKEN, ISSUER_URL, CLIENT_ID);

            assertFalse(result);
        }
    }

    /**
     * Tests token validation with null token parameter.
     * Verifies that the method handles null token input gracefully and returns false
     * when attempting to validate a null token.
     */
    @Test
    void isValidBearerTokenWithNullTokenShouldReturnFalse() {
        try (MockedStatic<JWT> jwtMock = mockStatic(JWT.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class);
             MockedStatic<ExceptionUtils> exceptionUtilsMock = mockStatic(ExceptionUtils.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(TokenHandlerUtil.class))
                    .thenReturn(mockLogger);

            IllegalArgumentException nullException = new IllegalArgumentException("Token cannot be null");
            jwtMock.when(() -> JWT.decode(null)).thenThrow(nullException);

            exceptionUtilsMock.when(() -> ExceptionUtils.getStackTrace(nullException))
                    .thenReturn("\n\tat com.auth0.jwt.JWT.decode(JWT.java:98)");

            boolean result = TokenHandlerUtil.isValidBearerToken(null, ISSUER_URL, CLIENT_ID);

            assertFalse(result);
        }
    }

    /**
     * Tests token validation with null clientId claim.
     * Verifies that the method handles NullPointerException gracefully when accessing
     * the clientId claim and returns false when claim processing fails.
     */
    @Test
    void isValidBearerTokenWithNullClientIdClaimShouldReturnFalse() {
        Date futureDate = new Date(System.currentTimeMillis() + 3600000);
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        LocalDateTime currentTime = LocalDateTime.now();

        try (MockedStatic<JWT> jwtMock = mockStatic(JWT.class);
             MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class);
             MockedStatic<ExceptionUtils> exceptionUtilsMock = mockStatic(ExceptionUtils.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(TokenHandlerUtil.class))
                    .thenReturn(mockLogger);

            jwtMock.when(() -> JWT.decode(VALID_TOKEN)).thenReturn(mockDecodedJWT);
            when(mockDecodedJWT.getClaims()).thenReturn(mockClaims);
            when(mockDecodedJWT.getExpiresAt()).thenReturn(futureDate);
            when(mockDecodedJWT.getIssuer()).thenReturn(ISSUER_URL);
            when(mockClaim.asString()).thenThrow(new NullPointerException("Claim is null"));

            dateUtilsMock.when(() -> DateUtils.getUTCTimeFromDate(futureDate))
                    .thenReturn("2025-08-03T11:30:45.123Z");
            dateUtilsMock.when(() -> DateUtils.convertUTCToLocalDateTime("2025-08-03T11:30:45.123Z"))
                    .thenReturn(futureTime);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTime())
                    .thenReturn(currentTime);
            dateUtilsMock.when(() -> DateUtils.before(currentTime, futureTime))
                    .thenReturn(true);

            NullPointerException npe = new NullPointerException("Claim is null");
            exceptionUtilsMock.when(() -> ExceptionUtils.getStackTrace(any(NullPointerException.class)))
                    .thenReturn("\n\tat io.mosip.print.util.TokenHandlerUtil.isValidBearerToken(TokenHandlerUtil.java:52)");

            boolean result = TokenHandlerUtil.isValidBearerToken(VALID_TOKEN, ISSUER_URL, CLIENT_ID);

            assertFalse(result);
        }
    }

    /**
     * Tests token validation with all conditions passing validation chain.
     * Verifies that the method correctly validates tokens when all conditions including
     * issuer, expiration, and client ID are valid and meet the requirements.
     */
    @Test
    void isValidBearerTokenWithAllValidConditionsShouldReturnTrue() {
        Date futureDate = new Date(System.currentTimeMillis() + 7200000); // +2 hours
        LocalDateTime futureTime = LocalDateTime.now().plusHours(2);
        LocalDateTime currentTime = LocalDateTime.now();

        try (MockedStatic<JWT> jwtMock = mockStatic(JWT.class);
             MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(TokenHandlerUtil.class))
                    .thenReturn(mockLogger);

            jwtMock.when(() -> JWT.decode(VALID_TOKEN)).thenReturn(mockDecodedJWT);
            when(mockDecodedJWT.getClaims()).thenReturn(mockClaims);
            when(mockDecodedJWT.getExpiresAt()).thenReturn(futureDate);
            when(mockDecodedJWT.getIssuer()).thenReturn(ISSUER_URL);
            when(mockClaim.asString()).thenReturn(CLIENT_ID);

            dateUtilsMock.when(() -> DateUtils.getUTCTimeFromDate(futureDate))
                    .thenReturn("2025-08-03T12:30:45.123Z");
            dateUtilsMock.when(() -> DateUtils.convertUTCToLocalDateTime("2025-08-03T12:30:45.123Z"))
                    .thenReturn(futureTime);
            dateUtilsMock.when(() -> DateUtils.getUTCCurrentDateTime())
                    .thenReturn(currentTime);
            dateUtilsMock.when(() -> DateUtils.before(currentTime, futureTime))
                    .thenReturn(true);

            boolean result = TokenHandlerUtil.isValidBearerToken(VALID_TOKEN, ISSUER_URL, CLIENT_ID);

            assertTrue(result);
        }
    }

    /**
     * Tests private constructor accessibility for code coverage.
     * Verifies that the private constructor can be accessed via reflection
     * and creates a valid instance of TokenHandlerUtil.
     */
    @Test
    void privateConstructorShouldCreateInstance() throws Exception {
        java.lang.reflect.Constructor<TokenHandlerUtil> constructor =
                TokenHandlerUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        TokenHandlerUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }
}
