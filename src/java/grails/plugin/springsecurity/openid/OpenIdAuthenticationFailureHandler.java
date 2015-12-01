/* Copyright 2006-2013 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springsecurity.openid;

import grails.plugin.springsecurity.ReflectionUtils;
import grails.plugin.springsecurity.web.authentication.AjaxAwareAuthenticationFailureHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class OpenIdAuthenticationFailureHandler extends AjaxAwareAuthenticationFailureHandler {

	/** Session key for the Open ID username/uri. */
	public static final String LAST_OPENID_USERNAME = "LAST_OPENID_USERNAME";

	/** Session key for the attributes that were returned. */
	public static final String LAST_OPENID_ATTRIBUTES = "LAST_OPENID_ATTRIBUTES";

	@Override
	public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException exception) throws IOException, ServletException {

		if (exception.getMessage().contains("Unable to process claimed identity")) { //TODO Not the best way
			super.onAuthenticationFailure(request, response, new InvalidOpenidEndpoint(exception));
			return;
		}

		boolean createMissingUsers = Boolean.TRUE.equals(
				ReflectionUtils.getConfigProperty("openid.registration.autocreate"));

		if (!createMissingUsers || !isSuccessfulLoginUnknownUser(exception)) {
			super.onAuthenticationFailure(request, response, exception);
			return;
		}

		OpenIDAuthenticationToken authentication = (OpenIDAuthenticationToken)exception.getAuthentication();
		request.getSession().setAttribute(LAST_OPENID_USERNAME, authentication.getPrincipal().toString());
		request.getSession().setAttribute(LAST_OPENID_ATTRIBUTES, extractAttrsWithValues(authentication));

		String createAccountUri = (String)ReflectionUtils.getConfigProperty("openid.registration.createAccountUri");
		getRedirectStrategy().sendRedirect(request, response, createAccountUri);
	}

	protected boolean isSuccessfulLoginUnknownUser(AuthenticationException exception) {
		if (!(exception instanceof UsernameNotFoundException)) {
			return false;
		}

		Authentication authentication = exception.getAuthentication();
		if (!(authentication instanceof OpenIDAuthenticationToken)) {
			return false;
		}

		return OpenIDAuthenticationStatus.SUCCESS.equals(
				((OpenIDAuthenticationToken)authentication).getStatus());
	}

	protected List<OpenIDAttribute> extractAttrsWithValues(final OpenIDAuthenticationToken authentication) {
		List<OpenIDAttribute> attributes = new ArrayList<OpenIDAttribute>();
		for (OpenIDAttribute attr : authentication.getAttributes()) {
			if (attr.getValues() == null || attr.getValues().isEmpty()) {
				continue;
			}
			if (attr.getValues().size() == 1 && attr.getValues().get(0) == null) {
				continue;
			}
			attributes.add(attr);
		}
		return attributes;
	}
}
