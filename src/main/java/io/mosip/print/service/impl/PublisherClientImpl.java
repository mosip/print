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
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.print.constant.HubMode;
import io.mosip.print.constant.LoggerFileConstant;
import io.mosip.print.constant.WebSubClientConstants;
import io.mosip.print.constant.WebSubClientErrorCode;
import io.mosip.print.exception.WebSubClientException;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.spi.PublisherClient;

/** This class is responsible for all the specification stated in {@link PublisherClient} interface.
 * 
 * @author Urvil Joshi
 *
 * @param <P> Type of payload.
 */
@Component
public class PublisherClientImpl<P> implements PublisherClient<String, P, HttpHeaders> {

	private Logger logger = PrintLogger.getLogger(PublisherClientImpl.class);


	@Autowired
	Environment environment;

	@Autowired
	RestTemplateBuilder builder;

	@Override
	public void registerTopic(String topic, String hubURL) {
		RestTemplate restTemplate;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.REGISTER.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = null;
		try {
			restTemplate = getRestTemplate();
			response = restTemplate.exchange(hubURL, HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException | KeyManagementException | NoSuchAlgorithmException
				| KeyStoreException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + exception.getMessage());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			logger.info("topic {} registered at hub", topic);
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + response.getBody());
		}
	}

	@Override
	public void unregisterTopic(String topic,String hubURL) {
		RestTemplate restTemplate;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.UNREGISTER.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = null;
		try {
			restTemplate = getRestTemplate();
			response = restTemplate.exchange(hubURL, HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException | KeyManagementException | NoSuchAlgorithmException
				| KeyStoreException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.UNREGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.UNREGISTER_ERROR.getErrorMessage() + exception.getMessage());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			logger.info("topic {} unregistered at hub", topic);
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.UNREGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.UNREGISTER_ERROR.getErrorMessage() + response.getBody());
		}

	}

	@Override
	public void publishUpdate(String topic, P payload, String contentType, HttpHeaders headers,String hubURL) {
		if (headers == null) {
			headers = new HttpHeaders();
		}
		RestTemplate restTemplate;
		headers.setContentType(MediaType.parseMediaType(contentType));

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hubURL)
				.queryParam(WebSubClientConstants.HUB_MODE, HubMode.PUBLISH.gethubModeValue())
				.queryParam(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<P> entity = new HttpEntity<>(payload,headers);
		ResponseEntity<String> response = null;
		try {
			restTemplate = getRestTemplate();
			response= restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity,
				String.class);
		} catch (HttpClientErrorException | HttpServerErrorException | KeyManagementException | NoSuchAlgorithmException
				| KeyStoreException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.PUBLISH_ERROR.getErrorCode(),
					WebSubClientErrorCode.PUBLISH_ERROR.getErrorMessage() + exception.getMessage());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			logger.info("published topic {} update at hub", topic);
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.PUBLISH_ERROR.getErrorCode(),
					WebSubClientErrorCode.PUBLISH_ERROR.getErrorMessage() + response.getBody());
		}
	}

	@Override
	public void notifyUpdate(String topic, HttpHeaders headers,String hubURL) {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hubURL)
				.queryParam(WebSubClientConstants.HUB_MODE, HubMode.PUBLISH.gethubModeValue())
				.queryParam(WebSubClientConstants.HUB_TOPIC, topic);
		RestTemplate restTemplate;
		HttpEntity<P> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = null;
		try {
			restTemplate = getRestTemplate();
			response= restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity,
				String.class);
		} catch (HttpClientErrorException | HttpServerErrorException | KeyManagementException | NoSuchAlgorithmException
				| KeyStoreException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorCode(),
					WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorMessage() + exception.getMessage());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			logger.info("notify topic {} update at hub", topic);
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorCode(),
					WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorMessage() + response.getBody());
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
