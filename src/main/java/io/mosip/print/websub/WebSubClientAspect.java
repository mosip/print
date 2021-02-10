package io.mosip.print.websub;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import io.mosip.print.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.print.constant.WebSubClientErrorCode;
import io.mosip.print.exception.WebSubClientException;

/**
 * This class comprises of aspects used for this library. Currently it is used
 * for verifying the content notified by Hub.
 * 
 * @author Urvil Joshi
 *
 */
@Component
public class WebSubClientAspect implements EmbeddedValueResolverAware {

	
	private StringValueResolver resolver = null;

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.resolver = resolver;
	}
	
	@Autowired
	private AuthenticatedContentVerifier authenticatedContentVerifier;

	@Autowired(required = true)
	private HttpServletRequest request;

	/**
	 * This aspect is for Preauthentication of content notified by hub.
	 * 
	 * @param preAuthenticateContent annotation used at method level for this aspect
	 *                               as a decorator and metadata provider.
	 */
	@Before("@annotation(preAuthenticateContent)")
	public void preAuthenticateContent(PreAuthenticateContentAndVerifyIntent preAuthenticateContent) {
		if (!(request instanceof HttpServletRequest)) {
			throw new WebSubClientException(WebSubClientErrorCode.INSTANCE_ERROR.getErrorCode(),
					WebSubClientErrorCode.INSTANCE_ERROR.getErrorMessage());
		}
		String secret = null;
		if (preAuthenticateContent.secret().startsWith("${")
				&& preAuthenticateContent.topic().endsWith("}")) {
			secret = resolver.resolveStringValue(preAuthenticateContent.secret());
		} else {
			secret = preAuthenticateContent.secret();
		}
		
		if (!secret.trim().isEmpty()) {
			if (!authenticatedContentVerifier.verifyAuthorizedContentVerified(request, secret)) {
				throw new WebSubClientException(WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ERROR.getErrorCode(),
						WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ERROR.getErrorMessage());
			}
		} else {
			throw new WebSubClientException(
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ANNOTATION_SECRET_ERROR.getErrorCode(),
					WebSubClientErrorCode.AUTHENTTICATED_CONTENT_ANNOTATION_SECRET_ERROR.getErrorMessage());
		}

	}
}