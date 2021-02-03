package io.mosip.print.util;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.mosip.print.constant.LoggerFileConstant;
import io.mosip.print.exception.WebSubClientException;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.model.CredentialStatusEvent;
import io.mosip.print.model.SubscriptionChangeRequest;
import io.mosip.print.model.SubscriptionChangeResponse;
import io.mosip.print.model.UnsubscriptionRequest;
import io.mosip.print.spi.PublisherClient;
import io.mosip.print.spi.SubscriptionClient;

@Component
public class WebSubSubscriptionHelper {

	@Autowired
	SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> sb;

	@Value("${mosip.event.hubURL}")
	private String webSubHubUrl;

	@Value("${mosip.event.secret}")
	private String webSubSecret;

	@Value("${mosip.event.callBackUrl}")
	private String callBackUrl;

	@Value("${mosip.event.topic}")
	private String topic;

	@Autowired
	private PublisherClient<String, CredentialStatusEvent, HttpHeaders> pb;

	/** The Constant BIOMETRICS. */
	private static final String WEBSUBSUBSCRIPTIONHEPLER = "WebSubSubscriptionHelper";

	/** The Constant ID_REPO_SERVICE_IMPL. */
	private static final String INITSUBSCRIPTION = "initSubsriptions";

	private Logger logger = PrintLogger.getLogger(WebSubSubscriptionHelper.class);

	public void initSubsriptions() {
		logger.info(LoggerFileConstant.SESSIONID.toString(), WEBSUBSUBSCRIPTIONHEPLER, INITSUBSCRIPTION,
				"Initializing subscribptions..");
		registerTopic(topic);
		subscribeForPrintServiceEvents();
	}

	private void subscribeForPrintServiceEvents() {
		try {
			SubscriptionChangeRequest subscriptionRequest = new SubscriptionChangeRequest();
			subscriptionRequest.setCallbackURL(callBackUrl);
			subscriptionRequest.setHubURL(webSubHubUrl + "/hub");
			subscriptionRequest.setSecret(webSubSecret);
			subscriptionRequest.setTopic(topic);
			sb.subscribe(subscriptionRequest);
		} catch (WebSubClientException e) {
			logger.info(LoggerFileConstant.SESSIONID.toString(), WEBSUBSUBSCRIPTIONHEPLER, INITSUBSCRIPTION,
					"websub subscription error");
		}
	}

	public void printStatusUpdateEvent(String topic, CredentialStatusEvent credentialStatusEvent) {
		try {
		HttpHeaders headers = new HttpHeaders();
		registerTopic(topic);
		pb.publishUpdate(topic, credentialStatusEvent, MediaType.APPLICATION_JSON_UTF8_VALUE, headers,
				webSubHubUrl + "/publish");
	} catch (WebSubClientException e) {
		logger.info(LoggerFileConstant.SESSIONID.toString(), WEBSUBSUBSCRIPTIONHEPLER, INITSUBSCRIPTION,
				"websub publish update error");
	}

	}

	private void registerTopic(String topic) {
		try {
			pb.registerTopic(topic, webSubHubUrl + "/publish");
		} catch (WebSubClientException e) {
			logger.info(LoggerFileConstant.SESSIONID.toString(), WEBSUBSUBSCRIPTIONHEPLER, INITSUBSCRIPTION,
					"topic already registered");
		}

	}
}
