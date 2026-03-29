package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import pt.unl.fct.iadi.novaevents.model.Club

interface ClubRepository : JpaRepository<Club, Long> {
    @Query(
        """
        select c.id as clubId, count(e) as eventCount
        from Club c
        left join c.events e
        group by c.id
        """
    )
    fun findEventCounts(): List<ClubEventCount>
}

interface ClubEventCount {
    fun getClubId(): Long
    fun getEventCount(): Long
}
