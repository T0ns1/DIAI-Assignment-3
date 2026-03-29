package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pt.unl.fct.iadi.novaevents.model.Event

interface EventRepository : JpaRepository<Event, Long> {
    fun existsByNameIgnoreCase(name: String): Boolean

    fun existsByNameIgnoreCaseAndIdNot(name: String, id: Long): Boolean

    fun findByClub_IdOrderByDateAscIdAsc(clubId: Long): List<Event>

    fun findByClub_IdAndId(clubId: Long, id: Long): Event?

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
