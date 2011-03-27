/* Copyright 2010-2011 the original author or authors.
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
package org.codehaus.groovy.grails.plugins.springsecurity.openid;

import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

/**
 * Will be thrown where an invalid OpenID identifier was used (invalid URL, target
 * server not answering, or haven't openid endpoint).
 *
 * Allows you to configure handler for this type of exception
 * (see {@link ExceptionMappingAuthenticationFailureHandler#setExceptionMappings(Map failureUrlMap)}
 *
 * @author <a href="http://igorartamonov.com">Igor Artamonov</a>
 */
public class InvalidOpenidEndpoint extends AuthenticationException {

	private static final long serialVersionUID = 1;

	/**
	 * Constructor.
	 * @param e the real AuthenticationException
	 */
	public InvalidOpenidEndpoint(AuthenticationException e) {
		super("Invalid OpenId endpoint", e);
	}
}
