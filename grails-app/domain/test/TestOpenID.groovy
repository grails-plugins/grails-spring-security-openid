package test

class TestOpenID {

	String url

	static belongsTo = [user: TestUser]

	static constraints = {
		url unique: true
	}
}
