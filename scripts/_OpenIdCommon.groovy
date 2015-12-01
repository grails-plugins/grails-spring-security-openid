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

includeTargets << new File(springSecurityCorePluginDir, 'scripts/_S2Common.groovy')

templateDir = "$springSecurityOpenidPluginDir/src/templates"

checkValue = { String value, String attributeName ->
	if (!value) {
		errorMessage "\nERROR: Cannot generate; grails.plugin.springsecurity.$attributeName isn't set"
		System.exit 1
	}
}

printMessage = { String message -> event('StatusUpdate', [message]) }
errorMessage = { String message -> event('StatusError', [message]) }
