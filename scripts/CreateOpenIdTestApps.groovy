/* Copyright 2011 the original author or authors.
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

/**
 * Creates test applications for functional tests.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */

includeTargets << grailsScript('_GrailsBootstrap')

appName = null
grailsHome = null
dotGrails = null
projectDir = null
pluginVersion = null
pluginZip = null
testprojectRoot = null
deleteAll = false

target(createOpenidTestApps: 'Creates OpenID test apps') {

	def configFile = new File(basedir, 'testapps.config.groovy')
	if (!configFile.exists()) {
		error "$configFile.path not found"
	}

	new ConfigSlurper().parse(configFile.text).each { name, config ->
		echo "\nCreating app based on configuration $name: ${config.flatten()}\n"
		init name, config
		createApp()
		installPlugins()
		runQuickstart()
		createProjectFiles()
	}
}

private void init(String name, config) {

	pluginVersion = config.pluginVersion
	if (!pluginVersion) {
		error "pluginVersion wasn't specified for config '$name'"
	}

	pluginZip = new File(basedir, "grails-spring-security-openid-${pluginVersion}.zip")
	if (!pluginZip.exists()) {
		error "plugin $pluginZip.absolutePath not found"
	}

	grailsHome = config.grailsHome
	if (!new File(grailsHome).exists()) {
		error "Grails home $grailsHome not found"
	}

	projectDir = config.projectDir
	appName = 'spring-security-openid-test-' + name
	testprojectRoot = "$projectDir/$appName"
	dotGrails = config.dotGrails
}

private void createApp() {

	ant.mkdir dir: projectDir

	deleteDir testprojectRoot
	deleteDir "$dotGrails/projects/$appName"

	callGrails(grailsHome, projectDir, 'dev', 'create-app') {
		ant.arg value: appName
	}
}

private void installPlugins() {

	// install plugins in local dir to make optional STS setup easier
	new File("$testprojectRoot/grails-app/conf/BuildConfig.groovy").withWriterAppend {
		it.writeLine 'grails.project.plugins.dir = "plugins"'
	}

	ant.mkdir dir: "${testprojectRoot}/plugins"

	callGrails(grailsHome, testprojectRoot, 'dev', 'install-plugin') {
		ant.arg value: pluginZip.absolutePath
	}
}

private void runQuickstart() {
	callGrails(grailsHome, testprojectRoot, 'dev', 's2-quickstart') {
		ant.arg value: 'com.testopenid'
		ant.arg value: 'User'
		ant.arg value: 'Role'
	}

	callGrails grailsHome, testprojectRoot, 'dev', 's2-init-openid'

	callGrails(grailsHome, testprojectRoot, 'dev', 's2-create-persistent-token') {
		ant.arg value: 'com.testopenid.PersistentLogin'
	}

	callGrails(grailsHome, testprojectRoot, 'dev', 's2-create-openid') {
		ant.arg value: 'com.testopenid.OpenID'
	}
}

private void createProjectFiles() {
	String source = "$basedir/webtest/projectfiles"

	ant.copy file: "$source/SecureController.groovy",
	         todir: "$testprojectRoot/grails-app/controllers", overwrite: true

	ant.copy file: "$source/BootStrap.groovy",
	         todir: "$testprojectRoot/grails-app/conf", overwrite: true

	ant.copy file: "$source/UrlMappings.groovy",
	         todir: "$testprojectRoot/grails-app/conf", overwrite: true

	ant.copy file: "$source/User.groovy",
	         todir: "$testprojectRoot/grails-app/domain/com/testopenid", overwrite: true
}

private void deleteDir(String path) {
	if (new File(path).exists() && !deleteAll) {
		String code = "confirm.delete.$path"
		ant.input message: "$path exists, ok to delete?", addproperty: code, validargs: 'y,n,a'
		def result = ant.antProject.properties[code]
		if ('a'.equalsIgnoreCase(result)) {
			deleteAll = true
		}
		else if (!'y'.equalsIgnoreCase(result)) {
			ant.echo "\nNot deleting $path"
			exit 1
		}
	}

	ant.delete dir: path
}

private void error(String message) {
	ant.echo "\nERROR: $message"
	exit 1
}

private void callGrails(String grailsHome, String dir, String env, String action, extraArgs = null) {
	ant.exec(executable: "${grailsHome}/bin/grails", dir: dir, failonerror: 'true') {
		ant.env key: 'GRAILS_HOME', value: grailsHome
		ant.arg value: env
		ant.arg value: action
		extraArgs?.call()
	}
}

setDefaultTarget 'createOpenidTestApps'
