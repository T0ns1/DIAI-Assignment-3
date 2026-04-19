package pt.unl.fct.iadi.novaevents.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository

@Service
class JpaUserDetailsManager(
    private val appUserRepository: AppUserRepository
) : UserDetailsManager {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = appUserRepository.findByUsername(username)
            ?: throw UsernameNotFoundException(username)

        return User(
            user.username,
            user.password,
            user.roles.map { SimpleGrantedAuthority(it.role.name) }
        )
    }

    @Transactional
    override fun createUser(user: UserDetails) {
        if (userExists(user.username)) {
            throw IllegalArgumentException("User ${user.username} already exists")
        }

        val appUser = AppUser(username = user.username, password = user.password)
        appUser.replaceRoles(user.authorities.map { it.authority })
        appUserRepository.save(appUser)
    }

    @Transactional
    override fun updateUser(user: UserDetails) {
        val appUser = appUserRepository.findByUsername(user.username)
            ?: throw UsernameNotFoundException(user.username)

        appUser.password = user.password
        appUser.replaceRoles(user.authorities.map { it.authority })
    }

    @Transactional
    override fun deleteUser(username: String) {
        appUserRepository.findByUsername(username)?.let { appUserRepository.delete(it) }
    }

    @Transactional
    override fun changePassword(oldPassword: String?, newPassword: String) {
        val username = SecurityContextHolder.getContext().authentication?.name
            ?: throw UsernameNotFoundException("No authenticated user")
        val appUser = appUserRepository.findByUsername(username)
            ?: throw UsernameNotFoundException(username)
        appUser.password = newPassword
    }

    @Transactional(readOnly = true)
    override fun userExists(username: String): Boolean =
        appUserRepository.existsByUsername(username)
}
