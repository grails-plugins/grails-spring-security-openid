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
import grails.util.GrailsNameUtils

includeTargets << new File("$springSecurityOpenidPluginDir/scripts/_OpenIdCommon.groovy")

target(s2InitOpenid: 'Initializes OpenID artifacts for the Spring Security OpenID plugin') {
	depends(checkVersion, configureProxy, packageApp, classpath)

	configure()
	copyControllersAndViews()
}

private void configure() {

	def SpringSecurityUtils = classLoader.loadClass(
	       'org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils')
	def conf = SpringSecurityUtils.securityConfig

	String userClassFullName = conf.userLookup.userDomainClassName
	checkValue userClassFullName, 'userLookup.userDomainClassName'

	String userPackageName
	String userClassName
	(userPackageName, userClassName) = splitClassName(userClassFullName)

	String roleClassFullName = conf.authority.className
	checkValue roleClassFullName, 'authority.className'

	String rolePackageName
	String roleClassName
	(rolePackageName, roleClassName) = splitClassName(roleClassFullName)

	String userRoleFullClassName = conf.userLookup.authorityJoinClassName
	checkValue userRoleFullClassName, 'userLookup.authorityJoinClassName'

	String userRolePackageName
	String userRoleClassName
	(userRolePackageName, userRoleClassName) = splitClassName(userRoleFullClassName)

	String usernamePropertyName = conf.userLookup.usernamePropertyName
	checkValue usernamePropertyName, 'userLookup.usernamePropertyName'

	String passwordPropertyName = conf.userLookup.passwordPropertyName
	checkValue passwordPropertyName, 'userLookup.passwordPropertyName'

	String enabledPropertyName = conf.userLookup.enabledPropertyName
	checkValue enabledPropertyName, 'userLookup.enabledPropertyName'

	String roleNameField = conf.authority.nameField
	checkValue roleNameField, 'authority.nameField'

	templateAttributes = [userClassFullName: userClassFullName,
	                      userClassName: userClassName,
	                      roleClassName: roleClassName,
	                      authorityCapName: GrailsNameUtils.getClassName(roleNameField, null),
	                      usernameCapName: GrailsNameUtils.getClassName(usernamePropertyName, null),
	                      roleClassFullName: roleClassFullName,
	                      userRoleFullClassName: userRoleFullClassName,
	                      userRoleClassName: userRoleClassName,
	                      usernamePropertyName: usernamePropertyName,
	                      passwordPropertyName: passwordPropertyName,
	                      enabledPropertyName: enabledPropertyName]
}

private void copyControllersAndViews() {
	ant.mkdir dir: "$appDir/views/openId"
	copyFile "$templateDir/auth.gsp.template", "$appDir/views/openId/auth.gsp"
	copyFile "$templateDir/createAccount.gsp.template", "$appDir/views/openId/createAccount.gsp"
	copyFile "$templateDir/linkAccount.gsp.template", "$appDir/views/openId/linkAccount.gsp"

	copyFile "$templateDir/spring-security-openid.messages.properties.template",
		"$appDir/i18n/spring-security-openid.messages.properties"

	generateFile "$templateDir/OpenIdController.groovy.template",
	             "$appDir/controllers/OpenIdController.groovy"
}

setDefaultTarget 's2InitOpenid'
