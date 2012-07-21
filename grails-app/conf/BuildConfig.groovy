grails.project.work.dir = 'target'
grails.project.source.level = 1.6
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
		mavenRepo 'http://guice-maven.googlecode.com/svn/trunk'
	}

	dependencies {
		compile('org.springframework.security:spring-security-openid:3.0.7.RELEASE') {
			excludes 'jmock-junit4', 'junit', 'mockito-core', 'openid4java',
			         'servlet-api', 'spring-core', 'spring-security-core',
			         'spring-security-web', 'spring-test', 'spring-web'
		}
		compile('org.openid4java:openid4java-nodeps:0.9.6') {
			excludes 'axiom-api', 'commons-logging', 'ehcache', 'higgins-configuration-api',
			         'higgins-sts-api', 'higgins-sts-common', 'higgins-sts-server-token-handler',
			         'higgins-sts-spi', 'httpclient', 'jcip-annotations', 'jdom', 'jetty',
			         'jetty-util', 'junit', 'jwebunit-htmlunit-plugin', 'log4j', 'nekohtml',
			         'openxri-client', 'openxri-syntax', 'servlet-api', 'spring-jdbc', 'xercesImpl', 'xmlsec'
		}
		runtime('org.apache.httpcomponents:httpclient:4.1.1') {
			excludes 'commons-codec', 'commons-logging', 'junit', 'mockito-core'
		}
		compile('xerces:xercesImpl:2.9.1') {
			excludes 'xml-apis', 'xml-resolver'
		}
		compile('net.sourceforge.nekohtml:nekohtml:1.9.15') {
			excludes 'xercesImpl'
		}
	}

	plugins {
		compile ':spring-security-core:1.2.7.3'

		build(':release:2.0.3', ':rest-client-builder:1.0.2') {
			export = false
		}
	}
}
