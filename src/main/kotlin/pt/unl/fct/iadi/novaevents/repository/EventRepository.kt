package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Event

interface EventRepository : JpaRepository<Event, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean

    fun existsByNameIgnoreCaseAndIdNot(name: String, id: Long): Boolean

    @Query(
        """
        select e from Event e
        join fetch e.club c
        join fetch e.type t
        where c.id = :clubId
        order by e.date asc, e.id asc
        """
    )
    fun findByClubIdWithDetails(clubId: Long): List<Event>

    @Query(
        """
        select e from Event e
        join fetch e.club c
        join fetch e.type t
        where c.id = :clubId and e.id = :id
        """
    )
    fun findByClubIdAndIdWithDetails(clubId: Long, id: Long): Event?

    @Modifying
    @Transactional
    @Query("update Event e set e.owner = :owner where e.owner is null")
    fun assignMissingOwner(owner: AppUser): Int

    @Query(
        """
        select e from Event e
        join fetch e.club c
        join fetch e.type t
        where (:clubId is null or c.id = :clubId)
          and (:typeName is null or lower(t.name) = lower(:typeName))
        order by e.date asc, e.id asc
        """
    )
    fun findAllByFilters(clubId: Long?, typeName: String?): List<Event>

    fun countByClub_Id(clubId: Long): Long
}
