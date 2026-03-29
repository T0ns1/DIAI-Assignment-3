package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Sort
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import java.util.NoSuchElementException

@Service
@Transactional(readOnly = true)
class ClubService(
    private val clubRepository: ClubRepository
) {
    fun findAll(): List<Club> = clubRepository.findAll(Sort.by("id"))

    fun findAllForList(): List<ClubListItem> {
        val countsByClubId = clubRepository.findEventCounts().associate { it.getClubId() to it.getEventCount() }
        return findAll().map { club ->
            ClubListItem(club = club, eventCount = countsByClubId[club.id] ?: 0L)
        }
    }

    fun findById(id: Long): Club =
        clubRepository.findById(id).orElseThrow { NoSuchElementException("Club with id $id not found") }
}
