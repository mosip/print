package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Test class for {@link ServerUtil}.
 *
 * This class provides comprehensive unit tests for the ServerUtil singleton utility class,
 * covering all methods including success and exception scenarios. Tests ensure proper
 * singleton behavior, server IP retrieval, and server name retrieval functionality.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServerUtilTest {

    /**
     * Sets up the test environment before each test method execution.
     *
     * Resets the singleton instance of ServerUtil to null using reflection
     * to ensure test isolation and prevent test interference.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ServerUtil.class, "serverInstance", null);
    }

    /**
     * Tests the singleton instance creation on first call.
     *
     * Verifies that {@link ServerUtil#getServerUtilInstance()} creates and returns
     * a non-null instance when called for the first time.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getServerUtilInstanceFirstCall() {
        ServerUtil instance = ServerUtil.getServerUtilInstance();

        assertNotNull(instance);
    }

    /**
     * Tests the singleton behavior on subsequent calls.
     *
     * Verifies that {@link ServerUtil#getServerUtilInstance()} returns the same
     * instance on multiple calls, ensuring proper singleton pattern implementation.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getServerUtilInstanceSecondCall() {
        ServerUtil instance1 = ServerUtil.getServerUtilInstance();
        ServerUtil instance2 = ServerUtil.getServerUtilInstance();

        assertSame(instance1, instance2);
    }

    /**
     * Tests successful server IP retrieval.
     *
     * Mocks {@link InetAddress#getLocalHost()} to return a valid InetAddress
     * and verifies that {@link ServerUtil#getServerIp()} returns the correct
     * IP address string.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getServerIpSuccess() throws Exception {
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
            InetAddress mockAddress = mock(InetAddress.class);

            inetAddressMock.when(InetAddress::getLocalHost).thenReturn(mockAddress);
            when(mockAddress.getHostAddress()).thenReturn("192.168.1.1");

            ServerUtil serverUtil = ServerUtil.getServerUtilInstance();
            String result = serverUtil.getServerIp();

            assertEquals("192.168.1.1", result);
        }
    }

    /**
     * Tests server IP retrieval when UnknownHostException occurs.
     *
     * Mocks {@link InetAddress#getLocalHost()} to throw an {@link UnknownHostException}
     * and verifies that {@link ServerUtil#getServerIp()} handles the exception gracefully
     * by returning "UNKNOWN-HOST" and logging the error.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getServerIpWithException() throws Exception {
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(ServerUtil.class))
                    .thenReturn(mockLogger);

            inetAddressMock.when(InetAddress::getLocalHost)
                    .thenThrow(new UnknownHostException("Test exception"));

            ServerUtil serverUtil = ServerUtil.getServerUtilInstance();
            String result = serverUtil.getServerIp();

            assertEquals("UNKNOWN-HOST", result);
        }
    }

    /**
     * Tests successful server name retrieval.
     *
     * Mocks {@link InetAddress#getLocalHost()} to return a valid InetAddress
     * and verifies that {@link ServerUtil#getServerName()} returns the correct
     * hostname string.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getServerNameSuccess() throws Exception {
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class)) {
            InetAddress mockAddress = mock(InetAddress.class);

            inetAddressMock.when(InetAddress::getLocalHost).thenReturn(mockAddress);
            when(mockAddress.getHostName()).thenReturn("test-server");

            ServerUtil serverUtil = ServerUtil.getServerUtilInstance();
            String result = serverUtil.getServerName();

            assertEquals("test-server", result);
        }
    }

    /**
     * Tests server name retrieval when UnknownHostException occurs.
     *
     * Mocks {@link InetAddress#getLocalHost()} to throw an {@link UnknownHostException}
     * and verifies that {@link ServerUtil#getServerName()} handles the exception gracefully
     * by returning "UNKNOWN-HOST" and logging the error.
     *
     * @throws Exception if test execution fails
     */
    @Test
    void getServerNameWithException() throws Exception {
        try (MockedStatic<InetAddress> inetAddressMock = mockStatic(InetAddress.class);
             MockedStatic<LoggerFactory> loggerFactoryMock = mockStatic(LoggerFactory.class)) {

            Logger mockLogger = mock(Logger.class);
            loggerFactoryMock.when(() -> LoggerFactory.getLogger(ServerUtil.class))
                    .thenReturn(mockLogger);

            inetAddressMock.when(InetAddress::getLocalHost)
                    .thenThrow(new UnknownHostException("Test exception"));

            ServerUtil serverUtil = ServerUtil.getServerUtilInstance();
            String result = serverUtil.getServerName();

            assertEquals("UNKNOWN-HOST", result);
        }
    }
}
