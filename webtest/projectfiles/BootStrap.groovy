import com.testopenid.Role
import com.testopenid.User
import com.testopenid.UserRole

class BootStrap {

	def springSecurityService

	def init = { servletContext ->

		String password = springSecurityService.encodePassword('password')

		def roleAdmin = new Role(authority: 'ROLE_ADMIN').save()
		def roleUser = new Role(authority: 'ROLE_USER').save()

		def user = new User(username: 'user', password: password, enabled: true).save()
		def admin = new User(username: 'admin', password: password, enabled: true).save()

		UserRole.create user, roleUser
		UserRole.create admin, roleUser
		UserRole.create admin, roleAdmin, true
	}
}
