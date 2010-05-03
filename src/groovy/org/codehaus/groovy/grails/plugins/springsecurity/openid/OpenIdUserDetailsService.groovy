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

import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * Extends the core plugin's implementation to add in searching by username and
 * the collection of OpenIDs to allow login via linked OpenIDs.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class OpenIdUserDetailsService extends GormUserDetailsService {

	@Override
	protected loadUser(String username, session) {

		def conf = SpringSecurityUtils.securityConfig
		if (!conf.openid.userLookup.openIdsPropertyName) {
			return super.loadUser(username, session)
		}

		String userDomainClassName = conf.userLookup.userDomainClassName
		String usernamePropertyName = conf.userLookup.usernamePropertyName

		// first do the regular lookup by username
		List<?> users = session.createQuery(
				"FROM $userDomainClassName WHERE $usernamePropertyName = :username")
				.setString('username', username)
				.list()
		if (users) {
			return users[0]
		}

		String openIdDomainClassName = conf.openid.domainClass

		// then check if it matches a linked OpenID
		users = session.createQuery(
				"SELECT o.user FROM $openIdDomainClassName o WHERE o.url=:url")
				.setString('url', username)
				.list()

		if (!users) {
			log.warn "User not found: $username"
			throw new UsernameNotFoundException('User not found', username)
		}

		users[0]
	}
}
