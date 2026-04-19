package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*

@Entity
@Table(
    name = "app_roles",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "role"])]
)
class AppRole(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: AppUser? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: RoleName = RoleName.ROLE_EDITOR
) {
    enum class RoleName {
        ROLE_EDITOR,
        ROLE_ADMIN
    }
}
