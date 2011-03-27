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
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
 * Extends the core plugin's implementation to add in searching by username and
 * the collection of OpenIDs to allow login via linked OpenIDs.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class OpenIdUserDetailsService extends GormUserDetailsService {

	@Override
	UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {

		def conf = SpringSecurityUtils.securityConfig
		if (!conf.openid.userLookup.openIdsPropertyName) {
			return super.loadUser(username)
		}

		String userDomainClassName = conf.userLookup.userDomainClassName
		String usernamePropertyName = conf.userLookup.usernamePropertyName

		Class<?> User = grailsApplication.getDomainClass(userDomainClassName).clazz

		User.withTransaction { status ->

			def user = User.findWhere((usernamePropertyName): username)

			if (!user) {
				String openIdDomainClassName = conf.openid.domainClass
				Class<?> OpenID = grailsApplication.getDomainClass(openIdDomainClassName).clazz
				user = OpenID.findByUrl(username)?.user
			}

			if (!user) {
				log.warn "User not found: $username"
				throw new UsernameNotFoundException('User not found', username)
			}

			Collection<GrantedAuthority> authorities = loadAuthorities(user, username, loadRoles)
			createUserDetails(user, authorities)
		}
	}
}
