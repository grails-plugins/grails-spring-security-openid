import com.testopenid.Role
import com.testopenid.User
import com.testopenid.UserRole

class BootStrap {

	def init = { servletContext ->

		def roleAdmin = new Role(authority: 'ROLE_ADMIN').save()
		def roleUser = new Role(authority: 'ROLE_USER').save()

		def user = new User(username: 'user', password: 'password').save()
		def admin = new User(username: 'admin', password: 'password').save()

		UserRole.create user, roleUser
		UserRole.create admin, roleUser
		UserRole.create admin, roleAdmin, true
	}
}
