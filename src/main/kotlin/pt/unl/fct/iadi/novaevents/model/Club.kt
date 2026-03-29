package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*

@Entity
@Table(name = "clubs")
class Club(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false, length = 2000)
    var description: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: ClubCategory = ClubCategory.ACADEMIC
) {
    @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
    var events: MutableList<Event> = mutableListOf()

    enum class ClubCategory {
        TECHNOLOGY,
        ARTS,
        SPORTS,
        ACADEMIC,
        SOCIAL,
        CULTURAL
    }
}
