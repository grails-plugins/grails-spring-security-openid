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
	}

	dependencies {
		compile('org.springframework.security:spring-security-openid:3.0.7.RELEASE') {
			excludes 'spring-security-web', 'spring-security-core', 'spring-core',
			         'spring-web', 'spring-test', 'servlet-api', 'openid4java',
			         'junit', 'mockito-core', 'jmock-junit4'
		}
		compile('org.openid4java:openid4java-nodeps:0.9.5') {
			excludes 'commons-logging', 'commons-httpclient', 'nekohtml', 'openxri-syntax',
			         'openxri-client', 'ehcache', 'higgins-configuration-api',
			         'higgins-sts-api', 'higgins-sts-common', 'higgins-sts-server-token-handler',
			         'higgins-sts-spi', 'xercesImpl', 'xmlsec', 'axiom-api',
			         'higgins-configuration-api', 'higgins-sts-api', 'higgins-sts-common',
			         'higgins-sts-server-token-handler', 'higgins-sts-spi', 'spring-jdbc',
			         'servlet-api', 'junit', 'jdom', 'jetty', 'jetty-util',
			         'log4j', 'jwebunit-htmlunit-plugin'
		}
		compile('commons-httpclient:commons-httpclient:3.1') {
			excludes 'junit', 'commons-logging', 'commons-codec'
		}
		compile('xerces:xercesImpl:2.9.1') {
			excludes 'xml-apis', 'xml-resolver'
		}
		compile('net.sourceforge.nekohtml:nekohtml:1.9.14') {
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
