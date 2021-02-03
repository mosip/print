package io.mosip.print.websub;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import io.mosip.print.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.print.constant.WebSubClientConstants;
import io.mosip.print.constant.WebSubClientErrorCode;
import io.mosip.print.exception.WebSubClientException;
import io.swagger.models.HttpMethod;
import lombok.Setter;

/**
 * This filter is used for handle intent verification request by hub after subscribe and unsubscribe
 * operations with help of metadata collected by {@link PreAuthenticateContentAndVerifyIntent} and
 * {@link IntentVerificationConfig} class.
 * 
 * @author Urvil Joshi
 *
 */
public class IntentVerificationFilter extends OncePerRequestFilter {

	private IntentVerifier intentVerifier;

	@Setter
	private Map<String, String> mappings = null;

	public IntentVerificationFilter(IntentVerifier intentVerifier) {
		this.intentVerifier = intentVerifier;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String topic=matchCallbackURL(request.getRequestURI());
		if (request.getMethod().equals(HttpMethod.GET.name()) && topic!=null) {
			String topicReq = request.getParameter(WebSubClientConstants.HUB_TOPIC);
			String modeReq = request.getParameter(WebSubClientConstants.HUB_MODE);
			if (intentVerifier.isIntentVerified(topic,
					request.getParameter("intentMode"), topicReq, modeReq)) {
				response.setStatus(HttpServletResponse.SC_ACCEPTED);
				try {
					response.getWriter().write(request.getParameter(WebSubClientConstants.HUB_CHALLENGE));
					response.getWriter().flush();
					response.getWriter().close();
				} catch (IOException exception) {
					throw new WebSubClientException(WebSubClientErrorCode.IO_ERROR.getErrorCode(),
							WebSubClientErrorCode.IO_ERROR.getErrorMessage().concat(exception.getMessage()));
				}

			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}

		} else {
			filterChain.doFilter(request, response);
		}
	}

	private String matchCallbackURL(String requestURI) {
		if(mappings.containsKey(requestURI)) {
			return mappings.get(requestURI);
		}else {
			Set<String> mappingKeys=mappings.keySet();
			for(String keys:mappingKeys){
				int pathParamIndex=keys.indexOf("/{");
				if(pathParamIndex==-1) {
					continue;
				}
				String url =keys.substring(0,pathParamIndex );
				if(requestURI.contains(url)) {
					int pathParamCount=keys.split("\\/\\{").length - 1;
					if(requestURI.substring(pathParamIndex).split("\\/").length-1==pathParamCount) {
						return mappings.get(keys);
					};
				}
			}
		}
		return null;
	}
}
