package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*

@Entity
@Table(name = "app_users")
class AppUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(nullable = false)
    var password: String = ""
) {
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var roles: MutableList<AppRole> = mutableListOf()

    fun replaceRoles(authorities: Collection<String>) {
        roles.clear()
        roles.addAll(
            authorities.map {
                AppRole(
                    user = this,
                    role = AppRole.RoleName.valueOf(it)
                )
            }
        )
    }
}
