/* Copyright 2013 SpringSource.
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

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import org.springframework.beans.factory.InitializingBean
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException

/**
 * Manages associating OpenIDs with application users, both by creating a new local user
 * associated with an OpenID and also by associating a new OpenID to an existing account.
 */
@Secured('permitAll')
class OpenIdController implements InitializingBean {

	/** Dependency injection for daoAuthenticationProvider. */
	def daoAuthenticationProvider

	/** Dependency injection for the grailsApplication. */
	def grailsApplication

	/** Dependency injection for OpenIDAuthenticationFilter. */
	def openIDAuthenticationFilter

	/** Dependency injection for the requestCache. */
	def requestCache

	/** Dependency injection for the springSecurityService. */
	def springSecurityService

	private Class User
	private Class UserRole
	private Class Role
	private String usernamePropertyName
	private String passwordPropertyName
	private String enabledPropertyName
	private String roleNameField

	static defaultAction = 'auth'
	static scope = 'singleton'

	/**
	 * Shows the login page. The user has the choice between using an OpenID and a username
	 * and password for a local account. If an OpenID authentication is successful but there
	 * is no corresponding local account, they'll be redirected to createAccount to create
	 * a new account, or click through to linkAccount to associate the OpenID with an
	 * existing local account.
	 */
	def auth() {

		def config = SpringSecurityUtils.securityConfig

		if (springSecurityService.isLoggedIn()) {
			redirect uri: config.successHandler.defaultTargetUrl
			return
		}

		[openIdPostUrl: "$request.contextPath$openIDAuthenticationFilter.filterProcessesUrl",
		 daoPostUrl:    "$request.contextPath$config.apf.filterProcessesUrl",
		 persistentRememberMe: config.rememberMe.persistent,
		 rememberMeParameter: config.rememberMe.parameter,
		 openidIdentifier: config.openid.claimedIdentityFieldName]
	}

	/**
	 * Initially we're redirected here after a UserNotFoundException with a valid OpenID
	 * authentication. This action is specified by the openid.registration.createAccountUri
	 * attribute.
	 * <p/>
	 * The GSP displays the OpenID that was received by the external provider and keeps it
	 * in the session rather than passing it between submits so the user has no opportunity
	 * to change it.
	 */
	def createAccount(OpenIdRegisterCommand command) {

		String openId = session[OpenIdAuthenticationFailureHandler.LAST_OPENID_USERNAME]
		if (!openId) {
			flash.error = 'Sorry, an OpenID was not found'
			return [command: command]
		}

		if (!request.post) {
			// show the form
			command.clearErrors()
			copyFromAttributeExchange command
			return [command: command, openId: openId]
		}

		if (command.hasErrors()) {
			return [command: command, openId: openId]
		}

		if (!createNewAccount(command.username, command.password, openId)) {
			return [command: command, openId: openId]
		}

		authenticateAndRedirect command.username
	}

	/**
	 * The registration page has a link to this action so an existing user who successfully
	 * authenticated with an OpenID can associate it with their account for future logins.
	 */
	def linkAccount(OpenIdLinkAccountCommand command) {

		String openId = session[OpenIdAuthenticationFailureHandler.LAST_OPENID_USERNAME]
		if (!openId) {
			flash.error = 'Sorry, an OpenID was not found'
			return [command: command]
		}

		if (!request.post) {
			// show the form
			command.clearErrors()
			return [command: command, openId: openId]
		}

		if (command.hasErrors()) {
			return [command: command, openId: openId]
		}

		try {
			registerAccountOpenId command.username, command.password, openId
		}
		catch (AuthenticationException e) {
			flash.error = 'Sorry, no user was found with that username and password'
			return [command: command, openId: openId]
		}

		authenticateAndRedirect command.username
	}

	/**
	 * Authenticate the user for real now that the account exists/is linked and redirect
	 * to the originally-requested uri if there's a SavedRequest.
	 *
	 * @param username the user's login name
	 */
	protected void authenticateAndRedirect(String username) {
		session.removeAttribute OpenIdAuthenticationFailureHandler.LAST_OPENID_USERNAME
		session.removeAttribute OpenIdAuthenticationFailureHandler.LAST_OPENID_ATTRIBUTES

		springSecurityService.reauthenticate username

		def config = SpringSecurityUtils.securityConfig
		def savedRequest = requestCache.getRequest(request, response)
		if (savedRequest && !config.successHandler.alwaysUseDefault) {
			redirect url: savedRequest.redirectUrl
		}
		else {
			redirect uri: config.successHandler.defaultTargetUrl
		}
	}

	/**
	 * Create the user instance and grant any roles that are specified in the config
	 * for new users.
	 * @param username  the username
	 * @param password  the password
	 * @param openId  the associated OpenID
	 * @return  true if successful
	 */
	protected boolean createNewAccount(String username, String password, String openId) {
		boolean created = User.withTransaction { status ->
			def config = SpringSecurityUtils.securityConfig

			password = encodePassword(password)
			def user = User.newInstance((usernamePropertyName): username,
			                            (passwordPropertyName): password,
			                            (enabledPropertyName): true)

			user.addToOpenIds(url: openId)

			if (!user.save()) {
				return false
			}

			for (roleName in config.openid.registration.roleNames) {
				UserRole.create user, Role.findWhere((roleNameField): roleName)
			}
			return true
		}
		return created
	}

	protected String encodePassword(String password) {
		def config = SpringSecurityUtils.securityConfig
		def encode = config.openid.encodePassword
		if (!(encode instanceof Boolean)) encode = false
		if (encode) {
			password = springSecurityService.encodePassword(password)
		}
		password
	}

	/**
	 * Associates an OpenID with an existing account. Needs the user's password to ensure
	 * that the user owns that account, and authenticates to verify before linking.
	 * @param username  the username
	 * @param password  the password
	 * @param openId  the associated OpenID
	 */
	protected void registerAccountOpenId(String username, String password, String openId) {
		// check that the user exists, password is valid, etc. - doesn't actually log in or log out,
		// just checks that user exists, password is valid, account not locked, etc.
		daoAuthenticationProvider.authenticate new UsernamePasswordAuthenticationToken(username, password)

		User.withTransaction { status ->
			def user = User.findWhere((usernamePropertyName): username)
			user.addToOpenIds(url: openId)
			if (!user.validate()) {
				status.setRollbackOnly()
			}
		}
	}

	/**
	 * For the initial form display, copy any registered AX values into the command.
	 * @param command  the command
	 */
	protected void copyFromAttributeExchange(OpenIdRegisterCommand command) {
		List attributes = session[OpenIdAuthenticationFailureHandler.LAST_OPENID_ATTRIBUTES] ?: []
		for (attribute in attributes) {
			// TODO document
			String name = attribute.name
			if (command.hasProperty(name)) {
				command."$name" = attribute.values[0]
			}
		}
	}

	void afterPropertiesSet() throws Exception {
		def conf = SpringSecurityUtils.securityConfig
        usernamePropertyName = conf.userLookup.usernamePropertyName
		passwordPropertyName = conf.userLookup.passwordPropertyName
		enabledPropertyName = conf.userLookup.enabledPropertyName
		roleNameField = conf.authority.nameField
		User = grailsApplication.getClassForName(conf.userLookup.userDomainClassName)
		UserRole = grailsApplication.getClassForName(conf.userLookup.authorityJoinClassName)
        Role = grailsApplication.getClassForName(conf.authority.className)
	}
}

class OpenIdRegisterCommand {

	String username = ""
	String password = ""
	String password2 = ""

	static constraints = {
		username blank: false, validator: { String username, command ->

			def User = grails.util.Holders.getGrailsApplication().getClassForName(SpringSecurityUtils.securityConfig.userLookup.userDomainClassName)

			User.withNewSession { session ->
				if (username) {
					boolean exists = User.createCriteria().count {
						eq SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName, username
					}
					if (exists) {
						return 'openIdRegisterCommand.username.error.unique'
					}
				}
			}
		}
		password blank: false, minSize: 8, maxSize: 64, validator: { password, command ->
			if (command.username && command.username.equals(password)) {
				return 'openIdRegisterCommand.password.error.username'
			}

			if (password && password.length() >= 8 && password.length() <= 64 &&
					(!password.matches('^.*\\p{Alpha}.*$') ||
					!password.matches('^.*\\p{Digit}.*$') ||
					!password.matches('^.*[!@#$%^&*].*$'))) {
				return 'openIdRegisterCommand.password.error.strength'
			}
		}
		password2 validator: { password2, command ->
			if (command.password != password2) {
				return 'openIdRegisterCommand.password2.error.mismatch'
			}
		}
	}
}

class OpenIdLinkAccountCommand {

	String username = ""
	String password = ""

	static constraints = {
		username blank: false
		password blank: false
	}
}
