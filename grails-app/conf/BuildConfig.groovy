grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir	= 'target/test-reports'
grails.project.docs.output.dir = 'docs' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits('global') {
		excludes 'commons-codec' // Grails ships with 1.3, need 1.4
	}

	log 'warn'

	repositories {        
		grailsPlugins()
		grailsHome()
		grailsCentral()

		ebr() // SpringSource  http://www.springsource.com/repository
	}

	dependencies {
		runtime('org.springframework.security:org.springframework.security.openid:3.0.4.RELEASE') {
			transitive = false
		}
		runtime('org.openid4java:com.springsource.org.openid4java:0.9.5') {
			transitive = false
		}
		runtime('org.apache.commons:com.springsource.org.apache.commons.httpclient:3.1.0') {
			transitive = false
		}
		runtime('org.apache.xerces:com.springsource.org.apache.xerces:2.9.1') {
			transitive = false
		}
		runtime('org.apache.xerces:com.springsource.org.apache.xerces:2.9.1') {
			transitive = false
		}
		runtime('net.sourceforge.nekohtml:com.springsource.org.cyberneko.html:1.9.13') {
			transitive = false
		}
	}
}
