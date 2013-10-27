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
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.openid.OpenIdAuthenticationFailureHandler
import grails.plugin.springsecurity.openid.userdetails.OpenIdUserDetailsService
import grails.plugin.springsecurity.web.authentication.NullLogoutHandlerRememberMeServices

import org.openid4java.consumer.ConsumerManager
import org.openid4java.consumer.InMemoryNonceVerifier
import org.springframework.security.openid.OpenID4JavaConsumer
import org.springframework.security.openid.OpenIDAttribute
import org.springframework.security.openid.OpenIDAuthenticationFilter
import org.springframework.security.openid.OpenIDAuthenticationProvider

class SpringSecurityOpenidGrailsPlugin {

	String version = '2.0-RC2'
	String grailsVersion = '2.0 > *'
	List loadAfter = ['springSecurityCore']
	List pluginExcludes = [
		'grails-app/domain/**',
		'docs/**',
		'src/docs/**',
		'scripts/CreateOpenIdTestApps.groovy'
	]

	String author = 'Burt Beckwith'
	String authorEmail = 'burt@burtbeckwith.com'
	String title = 'OpenID authentication support for the Spring Security plugin.'
	String description = 'OpenID authentication support for the Spring Security plugin.'
	String documentation = 'http://grails-plugins.github.io/grails-spring-security-openid/'

	String license = 'APACHE'
	def organization = [name: 'SpringSource', url: 'http://www.springsource.org/']
	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPSPRINGSECURITYOPENID']
	def scm = [url: 'https://github.com/grails-plugins/grails-spring-security-openid']

	def doWithSpring = {

		def conf = SpringSecurityUtils.securityConfig
		if (!conf || !conf.active) {
			return
		}

		SpringSecurityUtils.loadSecondaryConfig 'DefaultOpenIdSecurityConfig'
		// have to get again after overlaying DefaultOpenIdSecurityConfig
		conf = SpringSecurityUtils.securityConfig

		if (!conf.openid.active) {
			return
		}

		boolean printStatusMessages = (conf.printStatusMessages instanceof Boolean) ? conf.printStatusMessages : true

		if (printStatusMessages) {
			println '\nConfiguring Spring Security OpenID ...'
		}

		SpringSecurityUtils.registerProvider 'openIDAuthProvider'
		SpringSecurityUtils.registerFilter 'openIDAuthenticationFilter',
				SecurityFilterPosition.OPENID_FILTER

		openIDAuthProvider(OpenIDAuthenticationProvider) {
			userDetailsService = ref('userDetailsService')
		}

		openIDNonceVerifier(InMemoryNonceVerifier, conf.openid.nonceMaxSeconds) // 300 seconds

		openIDConsumerManager(ConsumerManager) {
			nonceVerifier = openIDNonceVerifier
		}

		def attrs = []
		conf.openid.registration.optionalAttributes.each { name, uri ->
			attrs << new OpenIDAttribute(name, uri)
		}
		conf.openid.registration.requiredAttributes.each { name, uri ->
			def attr = new OpenIDAttribute(name, uri)
			attr.required = true
			attrs << attr
		}
		openIDAttributes(ArrayList, attrs)

		openIDConsumer(OpenID4JavaConsumer, openIDConsumerManager, openIDAttributes)

		openIDAuthenticationFilter(OpenIDAuthenticationFilter) {
			claimedIdentityFieldName = conf.openid.claimedIdentityFieldName // openid_identifier
			consumer = ref('openIDConsumer')
			rememberMeServices = ref('rememberMeServices')
			authenticationManager = ref('authenticationManager')
			authenticationSuccessHandler = ref('authenticationSuccessHandler')
			authenticationFailureHandler = ref('authenticationFailureHandler')
			authenticationDetailsSource = ref('authenticationDetailsSource')
			sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
			filterProcessesUrl = '/j_spring_openid_security_check' // not configurable
		}

		authenticationFailureHandler(OpenIdAuthenticationFailureHandler) {
			redirectStrategy = ref('redirectStrategy')
			defaultFailureUrl = conf.failureHandler.defaultFailureUrl //'/login/authfail?login_error=1'
			useForward = conf.failureHandler.useForward // false
			ajaxAuthenticationFailureUrl = conf.failureHandler.ajaxAuthFailUrl // '/login/authfail?ajax=true'
			exceptionMappings = conf.failureHandler.exceptionMappings // [:]
		}

		// custom subclass that searches by username and openIds
		userDetailsService(OpenIdUserDetailsService) {
			grailsApplication = ref('grailsApplication')
		}

		if (!conf.rememberMe.persistent) {
			// auth is external, so no password, so regular cookie isn't possible
			rememberMeServices(NullLogoutHandlerRememberMeServices)
		}

		if (printStatusMessages) {
			println '... finished configuring Spring Security OpenID\n'
		}
	}

	def doWithApplicationContext = { ctx ->
		def conf = SpringSecurityUtils.securityConfig
		if (!conf || !conf.active) {
			return
		}

		String userClassName = conf.userLookup.userDomainClassName
		def userClass = ctx.grailsApplication.getClassForName(userClassName)
		String openIdsPropertyName = conf.openid.userLookup.openIdsPropertyName
		if (openIdsPropertyName && !userClass.newInstance().hasProperty(openIdsPropertyName)) {
			println """
ERROR: Your configuration specifies

   grails.plugin.springsecurity.openid.userLookup.openIdsPropertyName='${openIdsPropertyName}'

for $conf.userLookup.userDomainClassName but there's no property with that name in your user class;
either add a hasMany for the OpenID strings:

   static hasMany = [${openIdsPropertyName}: OpenID]

or set the property to null in Config.groovy if you aren't supporting associating OpenIDs with local accounts.
"""

			// reset the property in case the user doesn't restart to avoid ugly exceptions
			conf.openid.userLookup.openIdsPropertyName = ''
		}
	}
}
