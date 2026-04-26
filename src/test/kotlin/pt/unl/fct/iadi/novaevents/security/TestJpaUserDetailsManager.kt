package pt.unl.fct.iadi.novaevents.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository
import org.springframework.security.core.userdetails.User

class TestJpaUserDetailsManager {
    private val appUserRepository = mock(AppUserRepository::class.java)
    private val manager = JpaUserDetailsManager(appUserRepository)

    @Test
    fun `loadUserByUsername maps stored roles`() {
        val user = AppUser(id = 1L, username = "alice", password = "secret").apply {
            replaceRoles(listOf("ROLE_EDITOR"))
        }
        `when`(appUserRepository.findByUsername("alice")).thenReturn(user)

        val loaded = manager.loadUserByUsername("alice")

        assertEquals("alice", loaded.username)
        assertEquals(setOf("ROLE_EDITOR"), loaded.authorities.map { it.authority }.toSet())
    }

    @Test
    fun `createUser persists new app user`() {
        `when`(appUserRepository.existsByUsername("bob")).thenReturn(false)

        manager.createUser(User.withUsername("bob").password("pw").roles("ADMIN").build())

        verify(appUserRepository).save(org.mockito.ArgumentMatchers.any(AppUser::class.java))
    }

    @Test
    fun `createUser rejects duplicates`() {
        `when`(appUserRepository.existsByUsername("bob")).thenReturn(true)

        assertThrows(IllegalArgumentException::class.java) {
            manager.createUser(User.withUsername("bob").password("pw").roles("ADMIN").build())
        }
    }
}
