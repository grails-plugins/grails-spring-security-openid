package org.codehaus.groovy.grails.plugins.springsecurity.openid;

import org.springframework.security.core.*;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

/**
 * Will be throws where there was used and invalid OpenID identifier (invalid URL, target 
 * server not answering, or haven't openid endpoint).   
 * 
 * Allows you to configure handler for this type of exception 
 * (see {@link ExceptionMappingAuthenticationFailureHandler#setExceptionMappings(Map failureUrlMap)}
 * 
 * @author <a href="http://igorartamonov.com">Igor Artamonov</a>
 */
@SuppressWarnings("serial")
class InvalidOpenidEndpoint extends AuthenticationException {
	
	public InvalidOpenidEndpoint(Throwable t) {
		super("Invalid OpenId endpoint", t);
	}
}