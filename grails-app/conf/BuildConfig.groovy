grails.project.work.dir = 'target'
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
		mavenRepo 'http://mvnrepository.com/artifact'
		mavenRepo 'http://repo.spring.io/milestone' // TODO remove
	}

	dependencies {

		String springSecurityVersion = '3.2.0.RC1'

		compile "org.springframework.security:spring-security-openid:$springSecurityVersion", {
			excludes 'commons-logging', 'fest-assert', 'guice', 'httpclient', 'jcl-over-slf4j',
			         'junit', 'logback-classic', 'mockito-core', 'openid4java-nodeps', 'spring-aop',
			         'spring-beans', 'spring-context', 'spring-core', 'spring-security-core',
			         'spring-security-web', 'spring-test', 'spring-web', 'tomcat-servlet-api'
		}
		compile 'org.openid4java:openid4java-nodeps:0.9.6', {
			excludes 'axiom-api', 'commons-logging', 'ehcache', 'guice', 'higgins-configuration-api',
			         'higgins-sts-api', 'higgins-sts-common', 'higgins-sts-server-token-handler',
			         'higgins-sts-spi', 'httpclient', 'jcip-annotations', 'jdom', 'jetty', 'jetty-util',
			         'junit', 'jwebunit-htmlunit-plugin', 'log4j', 'nekohtml', 'openxri-client',
			         'openxri-syntax', 'servlet-api', 'spring-jdbc', 'xercesImpl', 'xmlsec'
		}
		runtime 'org.apache.httpcomponents:httpclient:4.2.3', {
			excludes 'commons-codec', 'commons-logging', 'httpcore', 'junit', 'mockito-core'
		}
		runtime 'org.apache.httpcomponents:httpcore:4.2.3', {
			excludes 'junit', 'mockito-core'
		}
		compile 'xerces:xercesImpl:2.9.1', {
			excludes 'xml-resolver'
		}
		compile 'net.sourceforge.nekohtml:nekohtml:1.9.15', {
			excludes 'xercesImpl'
		}
		compile 'com.google.inject:guice:2.0', {
			excludes 'aopalliance'
		}
		compile 'aopalliance:aopalliance:1.0'
	}

	plugins {
		compile ':spring-security-core:2.0-RC2'

		build ':release:2.2.1', ':rest-client-builder:1.0.3', {
			export = false
		}
	}
}
