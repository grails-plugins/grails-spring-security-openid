/* Copyright 2006-2010 the original author or authors.
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
package org.codehaus.groovy.grails.plugins.springsecurity.openid

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.springframework.security.core.userdetails.UsernameNotFoundException

import test.TestUser

/**
 * Integration tests for OpenIdUserDetailsService.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class OpenIdUserDetailsServiceTests extends GroovyTestCase {

	private static final String username = 'loginName'
	private static final String openId1 = 'foo@yahoo.com'
	private static final String openId2 = 'https://foo.openidprovider.net'

	def sessionFactory
	def userDetailsService

	/**
	 * {@inheritDoc}
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() {
		super.setUp()
		CH.config = new ConfigObject()
	}

	/**
	 * {@inheritDoc}
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() {
		super.tearDown()
		CH.config = null
	}

	void testIsOpenIdUserDetailsService() {
		assertTrue userDetailsService instanceof OpenIdUserDetailsService
	}

	void testLoadUserByUsername_NotFound() {
		String message = shouldFail(UsernameNotFoundException) {
			userDetailsService.loadUserByUsername 'not_a_user'
		}

		assertTrue message.contains('not found')
	}

	void testLoadUserByUsername() {
		assertEquals 0, TestUser.count()
		def user = new TestUser(username: username, password: 'password123', enabled: true)
		user.addToOpenIds(url: openId1)
		user.addToOpenIds(url: openId2)
		user.save(flush: true)
		assertEquals 1, TestUser.count()

		def session = sessionFactory.currentSession

		session.clear()
		assertEquals username, userDetailsService.loadUserByUsername(username).username

		session.clear()
		assertEquals username, userDetailsService.loadUserByUsername(openId1).username

		session.clear()
		assertEquals username, userDetailsService.loadUserByUsername(openId2).username
	}
}
