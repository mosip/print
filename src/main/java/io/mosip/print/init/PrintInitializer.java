package io.mosip.print.init;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.print.constant.LoggerFileConstant;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.util.WebSubSubscriptionHelper;

@Component
public class PrintInitializer implements ApplicationListener<ApplicationReadyEvent> {

	private static Logger logger = PrintLogger.getLogger(PrintInitializer.class);

	@Value("${print-websub-resubscription-retry-count:3}")
	private int retryCount;

	/**
	 * Default is Zero which will disable the scheduling.
	 */
	@Value("${print-websub-resubscription-delay-secs:0}")
	private int reSubscriptionDelaySecs;

	@Autowired
	private WebSubSubscriptionHelper webSubSubscriptionHelper;

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		if (reSubscriptionDelaySecs > 0) {
			logger.info(LoggerFileConstant.SESSIONID.toString(), this.getClass().getSimpleName(), "onApplicationEvent",
					"Work around for web-sub notification issue after some time.");
			scheduleRetrySubscriptions();
		} else {
			logger.info(LoggerFileConstant.SESSIONID.toString(), this.getClass().getSimpleName(),
					"scheduleRetrySubscriptions",
					"Scheduling for re-subscription is Disabled as the re-subsctription delay value is: "
							+ reSubscriptionDelaySecs);
		}
	}

	private void scheduleRetrySubscriptions() {
		logger.info(LoggerFileConstant.SESSIONID.toString(), this.getClass().getSimpleName(),
				"scheduleRetrySubscriptions",
				"Scheduling re-subscription every " + reSubscriptionDelaySecs + " seconds");
		taskScheduler.scheduleAtFixedRate(this::retrySubscriptions, Instant.now().plusSeconds(reSubscriptionDelaySecs),
				Duration.ofSeconds(reSubscriptionDelaySecs));
	}

	private void retrySubscriptions() {
		// Call Init Subscriptions for the count until no error in the subscription.
		// This will execute once first for sure if retry count is 0 or more. If the
		// subscription fails it will retry subscriptions up to given retry count.
		for (int i = 0; i <= retryCount; i++) {
			if (initSubsriptions()) {
				return;
			}
		}
	}

	private boolean initSubsriptions() {
		try {
			logger.info(LoggerFileConstant.SESSIONID.toString(), "initSubsriptions", "",
					"Initializing subscribptions..");
			webSubSubscriptionHelper.initSubsriptions();
			logger.info(LoggerFileConstant.SESSIONID.toString(), "initSubsriptions", "", "Initialized subscribptions.");
			return true;
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), "initSubsriptions", "",
					"Initializing subscribptions failed: " + e.getMessage());
			return false;
		}
	}

}
