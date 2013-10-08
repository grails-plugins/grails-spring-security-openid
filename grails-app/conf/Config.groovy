// for testing only, not included in plugin zip

grails {
	plugin {
		springsecurity {
			userLookup {
				userDomainClassName = 'test.TestUser'
				authorityJoinClassName = 'test.TestUserRole'
			}
			authority {
				className = 'test.TestRole'
			}
			openid {
				domainClass = 'test.TestOpenID'
			}
		}
	}
}

grails.doc.authors = 'Burt Beckwith'
grails.doc.license = 'Apache License 2.0'
