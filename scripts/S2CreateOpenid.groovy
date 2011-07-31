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

USAGE = """
	Usage: grails s2-create-openid <domain-class-name>

	Creates an OpenID domain class

	Example: grails s2-create-openid com.yourapp.OpenID
"""

target(s2CreateOpenid: 'Creates the OpenID domain class for the Spring Security OpenID plugin') {
	depends(checkVersion, configureProxy, packageApp, classpath)

	configure()
	createDomainClass()
	updateConfig()

	printMessage """
	*******************************************************
	* Your OpenID link domain class has been created and  *
	* your grails-app/conf/Config.groovy has been updated *
	* with the class name; please verify that the value   *
	* is correct.                                         *
	*******************************************************
"""
}

private void configure() {

	String fullClassName = parseArgs()
	String packageName
	String className
	(packageName, className) = splitClassName(fullClassName)

	String packageDeclaration = ''
	if (packageName) {
		packageDeclaration = "package $packageName"
	}

	def SpringSecurityUtils = classLoader.loadClass(
		'org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils')
	def conf = SpringSecurityUtils.securityConfig

	String userClassFullName = conf.userLookup.userDomainClassName
	checkValue userClassFullName, 'userLookup.userDomainClassName'

	String userPackageName
	String userClassName
	(userPackageName, userClassName) = splitClassName(userClassFullName)

	String userImport = ''
	if (userPackageName && userPackageName != packageName) {
		userImport = "import $userClassFullName"
	}

	templateAttributes = [fullClassName: fullClassName,
	                      packageName: packageName,
	                      packageDeclaration: packageDeclaration,
	                      className: className,
	                      userImport: userImport,
	                      userPropertyName: GrailsNameUtils.getPropertyName(userClassName),
	                      userClassName: userClassName]
}

private void updateConfig() {
	def configFile = new File(appDir, 'conf/Config.groovy')
	if (configFile.exists()) {
		configFile.withWriterAppend {
			it.writeLine "\ngrails.plugins.springsecurity.openid.domainClass = '$templateAttributes.fullClassName'"
		}
	}
}

private void createDomainClass() {
	String dir = packageToDir(templateAttributes.packageName)
	generateFile "$templateDir/OpenID.groovy.template",
		"$appDir/domain/${dir}${templateAttributes.className}.groovy"
}

private parseArgs() {
	args = args ? args.split('\n') : []
	switch (args.size()) {
		case 1:
			printMessage "Creating OpenID domain class ${args[0]}"
			return args[0]
		default:
			errorMessage USAGE
			System.exit 1
			break
	}
}

setDefaultTarget 's2CreateOpenid'
