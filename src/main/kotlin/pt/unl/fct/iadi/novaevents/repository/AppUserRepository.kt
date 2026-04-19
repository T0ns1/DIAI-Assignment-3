package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import pt.unl.fct.iadi.novaevents.model.AppUser

interface AppUserRepository : JpaRepository<AppUser, Long> {
    @EntityGraph(attributePaths = ["roles"])
    fun findByUsername(username: String): AppUser?

    fun existsByUsername(username: String): Boolean
}
