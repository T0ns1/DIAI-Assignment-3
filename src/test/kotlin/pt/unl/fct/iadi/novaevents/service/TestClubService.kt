package pt.unl.fct.iadi.novaevents.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.data.domain.Sort
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.repository.ClubEventCount
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import java.util.Optional

class TestClubService {
    private val clubRepository = mock(ClubRepository::class.java)
    private val clubService = ClubService(clubRepository)

    @Test
    fun `findAllForList combines clubs with repository counts`() {
        val chess = Club(id = 1L, name = "Chess Club")
        val robotics = Club(id = 2L, name = "Robotics Club")
        val count = object : ClubEventCount {
            override fun getClubId(): Long = 1L
            override fun getEventCount(): Long = 3L
        }

        `when`(clubRepository.findAll(Sort.by("id"))).thenReturn(listOf(chess, robotics))
        `when`(clubRepository.findEventCounts()).thenReturn(listOf(count))

        val items = clubService.findAllForList()

        assertEquals(2, items.size)
        assertEquals(3L, items.first { it.club.id == 1L }.eventCount)
        assertEquals(0L, items.first { it.club.id == 2L }.eventCount)
    }

    @Test
    fun `findById throws when club does not exist`() {
        `when`(clubRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows(NoSuchElementException::class.java) {
            clubService.findById(99L)
        }
    }
}
