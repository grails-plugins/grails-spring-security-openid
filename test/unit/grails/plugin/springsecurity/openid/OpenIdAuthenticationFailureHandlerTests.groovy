/* Copyright 2006-2015 the original author or authors.
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
package grails.plugin.springsecurity.openid

import grails.plugin.springsecurity.ReflectionUtils
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.web.SecurityRequestHolder

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.openid.OpenIDAttribute
import org.springframework.security.openid.OpenIDAuthenticationStatus
import org.springframework.security.openid.OpenIDAuthenticationToken

/**
 * Unit tests for <code>OpenIdAuthenticationFailureHandler</code>.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class OpenIdAuthenticationFailureHandlerTests extends GroovyTestCase {

	private static final String OPENID_REDIRECT = '/login/openIdCreate'
	private static final String AJAX_REDIRECT = '/ajaxAuthenticationFailureUrl'
	private static final String STANDARD_REDIRECT = '/defaultFailureUrl'

	private OpenIdAuthenticationFailureHandler handler = new OpenIdAuthenticationFailureHandler()
	private response = new MockHttpServletResponse()
	private request = new MockHttpServletRequest()

	@Override
	protected void setUp() {
		super.setUp()
		SecurityRequestHolder.set request, response
		ReflectionUtils.application = new FakeApplication()
		ReflectionUtils.setConfigProperty 'openid.registration.autocreate', true
		ReflectionUtils.setConfigProperty 'ajaxHeader', 'ajaxHeader'
		ReflectionUtils.setConfigProperty 'openid.registration.createAccountUri', OPENID_REDIRECT
		handler.defaultFailureUrl = STANDARD_REDIRECT
		handler.ajaxAuthenticationFailureUrl = AJAX_REDIRECT
	}

	void testOnAuthenticationFailure_NotUsernameNotFound() {

		handler.onAuthenticationFailure request, response, new AccountExpiredException('expired')

		assertEquals STANDARD_REDIRECT, response.redirectedUrl
	}

	void testOnAuthenticationFailure_NotOpenId() {
		handler.onAuthenticationFailure request, response, new UsernameNotFoundException('expired')

		assertEquals STANDARD_REDIRECT, response.redirectedUrl
	}

	void testOnAuthenticationFailure_NotOpenIdSuccess() {
		def e = new UsernameNotFoundException('expired')
		e.authentication = new OpenIDAuthenticationToken(OpenIDAuthenticationStatus.FAILURE, '', '', [])

		handler.onAuthenticationFailure request, response, e

		assertEquals STANDARD_REDIRECT, response.redirectedUrl
	}

	void testOnAuthenticationFailure_OpenIdSuccess_NotAutocreate() {
		ReflectionUtils.setConfigProperty 'openid.registration.autocreate', false
		def e = new UsernameNotFoundException('expired')
		e.authentication = new OpenIDAuthenticationToken(OpenIDAuthenticationStatus.SUCCESS, '', '', [])

		handler.onAuthenticationFailure request, response, e

		assertEquals STANDARD_REDIRECT, response.redirectedUrl
	}

	void testOnAuthenticationFailure_OpenIdSuccess_Autocreate() {
		def e = new UsernameNotFoundException('expired')
		String openId = 'http://foo.someopenid.com'
		e.authentication = new OpenIDAuthenticationToken(
				OpenIDAuthenticationStatus.SUCCESS, openId, '',
				[new OpenIDAttribute('email', 'type', ['foo@bar.com'])])

		handler.onAuthenticationFailure request, response, e

		assertEquals OPENID_REDIRECT, response.redirectedUrl
		assertEquals openId, request.session.getAttribute(OpenIdAuthenticationFailureHandler.LAST_OPENID_USERNAME)
		def attributes = request.session.getAttribute(OpenIdAuthenticationFailureHandler.LAST_OPENID_ATTRIBUTES)
		assertEquals 1, attributes.size()
		assertEquals 'foo@bar.com', attributes[0].values[0]
	}

	@Override
	protected void tearDown() {
		super.tearDown()
		SCH.clearContext()
		ReflectionUtils.application = null
		SpringSecurityUtils.resetSecurityConfig()
	}
}
