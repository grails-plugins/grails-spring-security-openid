grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir	= 'target/test-reports'
grails.project.docs.output.dir = 'docs' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits('global') {
//		excludes 'commons-codec' // Grails ships with 1.3, need 1.4
	}

	log 'warn'

	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()

		ebr() // SpringSource  http://www.springsource.com/repository
	}

	plugins {
		build(":release:1.0.0.RC2") {
			export = false
		}
		compile(":spring-security-core:1.1.1")
	}

	dependencies {
		compile("commons-codec:commons-codec:1.4") {
			export = false
		}

		runtime('org.springframework.security:org.springframework.security.openid:3.0.4.RELEASE') {
			excludes(
					[group: "org.apache.xerces", name: "com.springsource.org.apache.xerces"],
					[group: "org.apache.commons", name: "com.springsource.org.apache.commons.logging"],
					[group: "org.springframework", name: "org.springframework.beans"],
					[group: "org.springframework", name: "org.springframework.core"],
					[group: "org.springframework.security", name: "org.springframework.security.core"],
					[group: "org.springframework.security", name: "org.springframework.security.web"],
					[group: "org.openid4java", name: "com.springsource.org.openid4java"])
		}
		runtime('org.openid4java:com.springsource.org.openid4java:0.9.5') {
			excludes(
					[group: "org.apache.xerces", name: "com.springsource.org.apache.xerces"],
					[group: "org.apache.commons", name: "com.springsource.org.apache.commons.httpclient"],
					[group: "org.apache.commons", name: "com.springsource.org.apache.commons.codec"],
					[group: "org.apache.commons", name: "com.springsource.org.apache.commons.logging"],
					[group: "net.sourceforge.nekohtml", name: "com.springsource.org.cyberneko.html"])
			
		}
		runtime('org.apache.commons:com.springsource.org.apache.commons.httpclient:3.1.0') {
			excludes(
					[group: "org.apache.commons", name: "com.springsource.org.apache.commons.codec"],
					[group: "org.apache.commons", name: "com.springsource.org.apache.commons.logging"])
		}
		runtime('org.apache.xerces:com.springsource.org.apache.xerces:2.9.1') {
			exclude([group: "org.apache.xmlcommons", name: "com.springsource.org.apache.xmlcommons"])
		}
		runtime('net.sourceforge.nekohtml:com.springsource.org.cyberneko.html:1.9.13') {
			exclude([group: "org.apache.xerces", name: "com.springsource.org.apache.xerces"])
		}
	}
}
