package io.mosip.print.service.impl;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import io.mosip.print.constant.HubMode;
import io.mosip.print.constant.LoggerFileConstant;
import io.mosip.print.constant.WebSubClientConstants;
import io.mosip.print.constant.WebSubClientErrorCode;
import io.mosip.print.exception.WebSubClientException;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.model.SubscriptionChangeRequest;
import io.mosip.print.model.SubscriptionChangeResponse;
import io.mosip.print.model.UnsubscriptionRequest;
import io.mosip.print.spi.SubscriptionClient;
import io.mosip.print.util.EmptyCheckUtils;

/**
 * This class is responsible for all the specification stated in
 * {@link SubscriptionClient} interface.
 * 
 * @author Urvil Joshi
 *
 */
@Component
public class SubscriberClientImpl implements SubscriptionClient<SubscriptionChangeRequest,UnsubscriptionRequest, SubscriptionChangeResponse> {

	private Logger logger = PrintLogger.getLogger(SubscriberClientImpl.class);

	@Autowired
	Environment environment;

	@Autowired
	RestTemplateBuilder builder;

	@Override
	public SubscriptionChangeResponse subscribe(SubscriptionChangeRequest subscriptionRequest) {
		//TODO code duplicacy remove
		// TODO retries on redirect
		RestTemplate restTemplate;
        verifySubscribeModel(subscriptionRequest);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.SUBSCRIBE.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, subscriptionRequest.getTopic());
		map.add(WebSubClientConstants.HUB_CALLBACK, subscriptionRequest.getCallbackURL().concat("?intentMode=")
				.concat(HubMode.SUBSCRIBE.gethubModeValue()));
		map.add(WebSubClientConstants.HUB_SECRET, subscriptionRequest.getSecret());

		if (subscriptionRequest.getLeaseSeconds() > 0) {
			map.add(WebSubClientConstants.HUB_LEASE_SECONDS, Integer.toString(subscriptionRequest.getLeaseSeconds()));
		}
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = null;
		try {
			restTemplate = getRestTemplate();
			response = restTemplate.exchange(subscriptionRequest.getHubURL(), HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException | KeyManagementException | NoSuchAlgorithmException
				| KeyStoreException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + exception.getMessage());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			logger.info("subscribed for topic {} at hub", subscriptionRequest.getTopic());
			SubscriptionChangeResponse subscriptionChangeResponse = new SubscriptionChangeResponse();
			subscriptionChangeResponse.setHubURL(subscriptionRequest.getHubURL());
			subscriptionChangeResponse.setTopic(subscriptionRequest.getTopic());
			return subscriptionChangeResponse;
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + response.getBody());
		}
	}

	
	

	private void verifySubscribeModel(SubscriptionChangeRequest subscriptionRequest) {
		if(EmptyCheckUtils.isNullEmpty(subscriptionRequest.getCallbackURL())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("callback url is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(subscriptionRequest.getHubURL())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("HUB url is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(subscriptionRequest.getSecret())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("secret is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(subscriptionRequest.getTopic())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("topic is null or empty"));
		}
		
	}

	
	private void verifyUnsubscribeModel(UnsubscriptionRequest unsubscriptionRequest) {
		if(EmptyCheckUtils.isNullEmpty(unsubscriptionRequest.getCallbackURL())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("callback url is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(unsubscriptionRequest.getHubURL())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("HUB url is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(unsubscriptionRequest.getTopic())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("topic is null or empty"));
		}
		
	}



	@Override
	public SubscriptionChangeResponse unSubscribe(UnsubscriptionRequest unsubscriptionRequest) {
		//TODO code duplicacy remove
		// TODO retries on redirect
		RestTemplate restTemplate;
		verifyUnsubscribeModel(unsubscriptionRequest);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.UNSUBSCRIBE.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, unsubscriptionRequest.getTopic());
		map.add(WebSubClientConstants.HUB_CALLBACK, unsubscriptionRequest.getCallbackURL().concat("?intentMode=")
				.concat(HubMode.UNSUBSCRIBE.gethubModeValue()));
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = null;
		try {
			restTemplate = getRestTemplate();
			response = restTemplate.exchange(unsubscriptionRequest.getHubURL(), HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException | KeyManagementException | NoSuchAlgorithmException
				| KeyStoreException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + exception.getMessage());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			logger.info("unsubscribed for topic {} at hub", unsubscriptionRequest.getTopic());
			SubscriptionChangeResponse subscriptionChangeResponse = new SubscriptionChangeResponse();
			subscriptionChangeResponse.setHubURL(unsubscriptionRequest.getHubURL());
			subscriptionChangeResponse.setTopic(unsubscriptionRequest.getTopic());
			return subscriptionChangeResponse;
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + response.getBody());
		}
	}

	public RestTemplate getRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), Arrays.asList(environment.getActiveProfiles()).toString());
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch("dev-k8"::equals)) {
			logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					Arrays.asList(environment.getActiveProfiles()).toString());
			return new RestTemplate();
		} else {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();

			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

			requestFactory.setHttpClient(httpClient);
			return new RestTemplate(requestFactory);
		}

	}

}
