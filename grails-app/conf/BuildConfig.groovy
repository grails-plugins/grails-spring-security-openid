grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		mavenLocal()
		grailsCentral()
		mavenCentral()
	}

	dependencies {

		String springSecurityVersion = '3.2.9.RELEASE'

		compile "org.springframework.security:spring-security-openid:$springSecurityVersion", {
			excludes 'commons-logging', 'javax.servlet-api', 'nekohtml', 'openid4java-nodeps', 'spring-aop', 'spring-beans',
			         'spring-context', 'spring-core', 'spring-security-core', 'spring-security-web', 'spring-web'
		}

		compile 'org.openid4java:openid4java:1.0.0', {
			excludes 'commons-logging', 'ehcache', 'guice', 'hsqldb', 'httpclient', 'jdom', 'jetty', 'jetty-util', 'junit',
			         'jwebunit-htmlunit-plugin', 'log4j', 'nekohtml', 'servlet-api', 'spring-jdbc', 'xercesImpl'
		}

		compile 'xerces:xercesImpl:2.11.0', {
			excludes 'xml-resolver'
		}

		compile 'net.sourceforge.nekohtml:nekohtml:1.9.22', {
			excludes 'xercesImpl'
		}
	}

	plugins {
		compile ':spring-security-core:2.0-RC6'

		compile ':hibernate:3.6.10.18', {
			export = false
		}

		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}
	}
}
