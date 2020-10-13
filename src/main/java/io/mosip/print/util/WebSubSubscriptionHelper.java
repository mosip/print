package io.mosip.print.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.model.EventModel;

@Component
public class WebSubSubscriptionHelper {

	@Autowired
	SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> sb;

	@Value("${mosip.event.hubURL}")
	private String webSubHubUrl;

	@Value("${mosip.event.secret}")
	private String webSubSecret;

	@Value("${CALLBACKURL}")
	private String callBackUrl;

	/** The config server file storage URL. */
	@Value("${mosip.partnerhuburl}")
	private String partnerhuburl;

	@Autowired
	private PublisherClient<String, EventModel, HttpHeaders> pb;

	/** The Constant BIOMETRICS. */
	private static final String WEBSUBSUBSCRIPTIONHEPLER = "WebSubSubscriptionHelper";

	/** The Constant ID_REPO_SERVICE_IMPL. */
	private static final String INITSUBSCRIPTION = "initSubsriptions";

	private static final Logger LOGGER = PrintLogger.getLogger(WebSubSubscriptionHelper.class);

	public void initSubsriptions() {
		LOGGER.info("NotifyPrint", WEBSUBSUBSCRIPTIONHEPLER, INITSUBSCRIPTION, "Initializing subscribptions..");
		subscribeForPrintServiceEvents();
	}

	private void subscribeForPrintServiceEvents() {
		try {
			SubscriptionChangeRequest subscriptionRequest = new SubscriptionChangeRequest();
			subscriptionRequest.setCallbackURL(callBackUrl);
			subscriptionRequest.setHubURL(webSubHubUrl);
			subscriptionRequest.setSecret(webSubSecret);
			subscriptionRequest.setTopic("CREDENTIAL_STATUS_UPDATE");
			sb.subscribe(subscriptionRequest);
		} catch (WebSubClientException e) {
			LOGGER.info("NotifyPrint", WEBSUBSUBSCRIPTIONHEPLER, INITSUBSCRIPTION, "websub subscription error");
		}
	}

	private void registerTopic() {
		try {
			pb.registerTopic("CREDENTIAL_STATUS_UPDATE", partnerhuburl);
		} catch (WebSubClientException e) {
			LOGGER.info("NotifyPrint", WEBSUBSUBSCRIPTIONHEPLER, INITSUBSCRIPTION, "topic already registered");
		}

	}
}
