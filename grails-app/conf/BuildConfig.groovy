grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir	= 'target/test-reports'
grails.project.docs.output.dir = 'docs' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'

	log 'warn'

	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()

		mavenCentral()
	}

	dependencies {
		compile('org.springframework.security:spring-security-openid:3.0.5.RELEASE') {
			transitive = false
		}
		compile('org.openid4java:openid4java-nodeps:0.9.5') {
			transitive = false
		}
		compile('commons-httpclient:commons-httpclient:3.1') {
			transitive = false
		}
		compile('xerces:xercesImpl:2.9.1') {
			transitive = false
		}
		compile('net.sourceforge.nekohtml:nekohtml:1.9.14') {
			transitive = false
		}
	}
}
