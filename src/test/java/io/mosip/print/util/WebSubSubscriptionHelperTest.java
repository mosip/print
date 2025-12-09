package io.mosip.print.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.print.model.CredentialStatusEvent;

/**
 * Unit tests for {@link WebSubSubscriptionHelper} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the WebSubSubscriptionHelper class,
 * including WebSub subscription initialization, event publishing operations, exception handling scenarios,
 * configuration management, and various edge cases for WebSub subscription and publishing operations.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebSubSubscriptionHelperTest {

    @Mock
    private SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> sb;

    @Mock
    private PublisherClient<String, CredentialStatusEvent, HttpHeaders> pb;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Logger mockLogger;

    @InjectMocks
    private WebSubSubscriptionHelper webSubSubscriptionHelper;

    private static final String WEB_SUB_HUB_URL = "http://websub-hub.test.com";
    private static final String WEB_SUB_SECRET = "test-secret";
    private static final String CALL_BACK_URL = "http://callback.test.com";
    private static final String TOPIC = "test-topic";

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes the WebSubSubscriptionHelper instance with WebSub configuration parameters
     * including hub URL, secret, callback URL, topic, and logger for comprehensive testing.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(webSubSubscriptionHelper, "webSubHubUrl", WEB_SUB_HUB_URL);
        ReflectionTestUtils.setField(webSubSubscriptionHelper, "webSubSecret", WEB_SUB_SECRET);
        ReflectionTestUtils.setField(webSubSubscriptionHelper, "callBackUrl", CALL_BACK_URL);
        ReflectionTestUtils.setField(webSubSubscriptionHelper, "topic", TOPIC);
        ReflectionTestUtils.setField(webSubSubscriptionHelper, "LOGGER", mockLogger);
    }

    /**
     * Tests successful WebSub subscription initialization.
     * Verifies that the method correctly initializes subscriptions with proper request parameters
     * and logs appropriate information messages during the subscription process.
     */
    @Test
    void initSubscriptionsShouldSucceedWithValidConfiguration() throws Exception {
        when(sb.subscribe(any(SubscriptionChangeRequest.class))).thenReturn(mock(SubscriptionChangeResponse.class));

        webSubSubscriptionHelper.initSubsriptions();

        verify(sb).subscribe(any(SubscriptionChangeRequest.class));
        verify(mockLogger).info(eq("Initializing subscribptions... {} {}"),
                eq("WebSubSubscriptionHelper"), eq("initSubsriptions"));
        verify(mockLogger).info(eq("subscription request : {}"), any(SubscriptionChangeRequest.class));
    }

    /**
     * Tests subscription initialization when WebSubClientException occurs.
     * Verifies that the method handles WebSubClientException gracefully during subscription
     * and logs appropriate error messages without propagating the exception.
     */
    @Test
    void initSubscriptionsWithWebSubClientExceptionShouldHandleGracefully() throws Exception {
        doThrow(new WebSubClientException("ERR-001", "Subscription failed"))
                .when(sb).subscribe(any(SubscriptionChangeRequest.class));

        webSubSubscriptionHelper.initSubsriptions();

        verify(sb).subscribe(any(SubscriptionChangeRequest.class));
        verify(mockLogger).info(eq("Initializing subscribptions... {} {}"),
                eq("WebSubSubscriptionHelper"), eq("initSubsriptions"));
        verify(mockLogger).info(eq("subscription request : {}"), any(SubscriptionChangeRequest.class));
        verify(mockLogger).info(eq("websub subscription error {} {}"),
                eq("WebSubSubscriptionHelper"), eq("initSubsriptions"));
    }

    /**
     * Tests successful print status update event publishing.
     * Verifies that the method correctly publishes credential status events to the specified topic
     * with proper headers and WebSub hub URL configuration.
     */
    @Test
    void printStatusUpdateEventShouldSucceedWithValidParameters() throws Exception {
        String testTopic = "print-status-topic";
        CredentialStatusEvent credentialStatusEvent = new CredentialStatusEvent();

        webSubSubscriptionHelper.printStatusUpdateEvent(testTopic, credentialStatusEvent);

        verify(pb).publishUpdate(eq(testTopic), eq(credentialStatusEvent),
                anyString(), any(HttpHeaders.class), eq(WEB_SUB_HUB_URL));
    }

    /**
     * Tests print status update event publishing when WebSubClientException occurs.
     * Verifies that the method handles WebSubClientException gracefully during publishing
     * and logs appropriate error messages for troubleshooting.
     */
    @Test
    void printStatusUpdateEventWithWebSubClientExceptionShouldHandleGracefully() throws Exception {
        String testTopic = "print-status-topic";
        CredentialStatusEvent credentialStatusEvent = new CredentialStatusEvent();

        doThrow(new WebSubClientException("ERR-002", "Publish failed"))
                .when(pb).publishUpdate(anyString(), any(CredentialStatusEvent.class),
                        anyString(), any(HttpHeaders.class), anyString());

        webSubSubscriptionHelper.printStatusUpdateEvent(testTopic, credentialStatusEvent);

        verify(pb).publishUpdate(eq(testTopic), eq(credentialStatusEvent),
                anyString(), any(HttpHeaders.class), eq(WEB_SUB_HUB_URL));
        verify(mockLogger).info(eq("websub publish update error {} {}"),
                eq("WebSubSubscriptionHelper"), eq("initSubsriptions"));
    }

    /**
     * Tests print status update event publishing with null topic parameter.
     * Verifies that the method handles null topic gracefully and still attempts
     * to publish the event without causing exceptions.
     */
    @Test
    void printStatusUpdateEventWithNullTopicShouldHandleGracefully() throws Exception {
        CredentialStatusEvent credentialStatusEvent = new CredentialStatusEvent();

        webSubSubscriptionHelper.printStatusUpdateEvent(null, credentialStatusEvent);

        verify(pb).publishUpdate(eq(null), eq(credentialStatusEvent),
                anyString(), any(HttpHeaders.class), eq(WEB_SUB_HUB_URL));
    }

    /**
     * Tests print status update event publishing with null credential status event.
     * Verifies that the method handles null credential status event gracefully
     * and still attempts the publishing operation.
     */
    @Test
    void printStatusUpdateEventWithNullCredentialStatusEventShouldHandleGracefully() throws Exception {
        String testTopic = "print-status-topic";

        webSubSubscriptionHelper.printStatusUpdateEvent(testTopic, null);

        verify(pb).publishUpdate(eq(testTopic), eq(null),
                anyString(), any(HttpHeaders.class), eq(WEB_SUB_HUB_URL));
    }

    /**
     * Tests scheduled method annotation functionality.
     * Verifies that the scheduled subscription initialization method executes correctly
     * and logs appropriate initialization messages.
     */
    @Test
    void scheduledMethodExecutionShouldInitializeCorrectly() throws Exception {
        when(sb.subscribe(any(SubscriptionChangeRequest.class))).thenReturn(mock(SubscriptionChangeResponse.class));

        webSubSubscriptionHelper.initSubsriptions();

        verify(mockLogger).info(eq("Initializing subscribptions... {} {}"),
                eq("WebSubSubscriptionHelper"), eq("initSubsriptions"));
    }

    /**
     * Tests subscription functionality with different configuration values.
     * Verifies that the method works correctly when WebSub configuration parameters
     * are changed including hub URL, secret, callback URL, and topic.
     */
    @Test
    void subscriptionWithDifferentConfigurationShouldWork() throws Exception {
        ReflectionTestUtils.setField(webSubSubscriptionHelper, "webSubHubUrl", "http://different-hub.com");
        ReflectionTestUtils.setField(webSubSubscriptionHelper, "webSubSecret", "different-secret");
        ReflectionTestUtils.setField(webSubSubscriptionHelper, "callBackUrl", "http://different-callback.com");
        ReflectionTestUtils.setField(webSubSubscriptionHelper, "topic", "different-topic");

        when(sb.subscribe(any(SubscriptionChangeRequest.class))).thenReturn(mock(SubscriptionChangeResponse.class));

        webSubSubscriptionHelper.initSubsriptions();

        verify(sb).subscribe(any(SubscriptionChangeRequest.class));
        verify(mockLogger).info(eq("subscription request : {}"), any(SubscriptionChangeRequest.class));
    }

    /**
     * Tests HttpHeaders creation in printStatusUpdateEvent method.
     * Verifies that the method correctly creates and passes HttpHeaders during
     * the event publishing process to the WebSub hub.
     */
    @Test
    void printStatusUpdateEventHttpHeadersCreationShouldWork() throws Exception {
        String testTopic = "test-topic";
        CredentialStatusEvent credentialStatusEvent = new CredentialStatusEvent();

        webSubSubscriptionHelper.printStatusUpdateEvent(testTopic, credentialStatusEvent);

        verify(pb).publishUpdate(eq(testTopic), eq(credentialStatusEvent),
                anyString(), any(HttpHeaders.class), eq(WEB_SUB_HUB_URL));
    }

    /**
     * Tests multiple subscription attempts with mixed success and failure scenarios.
     * Verifies that the method handles multiple subscription attempts correctly,
     * processing both successful and failed subscription operations appropriately.
     */
    @Test
    void multipleSubscriptionAttemptsShouldHandleMixedResults() throws Exception {
        when(sb.subscribe(any(SubscriptionChangeRequest.class))).thenReturn(mock(SubscriptionChangeResponse.class));
        webSubSubscriptionHelper.initSubsriptions();

        doThrow(new WebSubClientException("ERR-003", "Subscription failed"))
                .when(sb).subscribe(any(SubscriptionChangeRequest.class));
        webSubSubscriptionHelper.initSubsriptions();

        verify(mockLogger).info(eq("websub subscription error {} {}"),
                eq("WebSubSubscriptionHelper"), eq("initSubsriptions"));
    }
}
