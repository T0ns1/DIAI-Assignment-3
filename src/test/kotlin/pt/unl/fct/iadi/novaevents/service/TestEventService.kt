package pt.unl.fct.iadi.novaevents.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import java.time.LocalDate

class TestEventService {
    private val clubService = mock(ClubService::class.java)
    private val eventRepository = mock(EventRepository::class.java)
    private val eventTypeRepository = mock(EventTypeRepository::class.java)
    private val appUserRepository = mock(AppUserRepository::class.java)
    private val weatherService = mock(WeatherService::class.java)
    private val eventService = EventService(
        clubService,
        eventRepository,
        eventTypeRepository,
        appUserRepository,
        weatherService
    )

    @Test
    fun `createEvent rejects outdoor event without location`() {
        val club = Club(id = 4L, name = EventService.HIKING_AND_OUTDOORS_CLUB)
        val owner = AppUser(id = 1L, username = "alice")

        `when`(clubService.findById(4L)).thenReturn(club)
        `when`(appUserRepository.findByUsername("alice")).thenReturn(owner)

        val exception = assertThrows(EventCreationValidationException::class.java) {
            eventService.createEvent(
                clubId = 4L,
                name = "Mountain Walk",
                date = LocalDate.of(2026, 5, 1),
                location = "   ",
                typeName = "SOCIAL",
                description = null,
                ownerUsername = "alice"
            )
        }

        assertEquals("Location is required for outdoor events", exception.message)
    }

    @Test
    fun `createEvent rejects outdoor event when weather is raining`() {
        val club = Club(id = 4L, name = EventService.HIKING_AND_OUTDOORS_CLUB)
        val owner = AppUser(id = 1L, username = "alice")

        `when`(clubService.findById(4L)).thenReturn(club)
        `when`(appUserRepository.findByUsername("alice")).thenReturn(owner)
        `when`(weatherService.isRaining("Sintra")).thenReturn(true)

        val exception = assertThrows(EventCreationValidationException::class.java) {
            eventService.createEvent(
                clubId = 4L,
                name = "Rainy Hike",
                date = LocalDate.of(2026, 5, 1),
                location = "Sintra",
                typeName = "SOCIAL",
                description = null,
                ownerUsername = "alice"
            )
        }

        assertEquals(
            "It is currently raining at \"Sintra\" — outdoor events cannot be created in bad weather",
            exception.message
        )
    }

    @Test
    fun `createEvent saves normalized event when checks pass`() {
        val club = Club(id = 1L, name = "Chess Club")
        val owner = AppUser(id = 1L, username = "alice")
        val type = EventType(id = 6L, name = "OTHER")

        `when`(clubService.findById(1L)).thenReturn(club)
        `when`(appUserRepository.findByUsername("alice")).thenReturn(owner)
        `when`(eventRepository.existsByNameIgnoreCase("Spring Meetup")).thenReturn(false)
        `when`(eventTypeRepository.findByNameIgnoreCase("OTHER")).thenReturn(type)
        `when`(eventRepository.save(org.mockito.ArgumentMatchers.any(Event::class.java))).thenAnswer { it.arguments[0] }

        val event = eventService.createEvent(
            clubId = 1L,
            name = "  Spring Meetup  ",
            date = LocalDate.of(2026, 5, 1),
            location = "  Room 1  ",
            typeName = "OTHER",
            description = "  Details  ",
            ownerUsername = "alice"
        )

        val captor = ArgumentCaptor.forClass(Event::class.java)
        verify(eventRepository).save(captor.capture())
        assertEquals("Spring Meetup", captor.value.name)
        assertEquals("Room 1", captor.value.location)
        assertEquals("Details", captor.value.description)
        assertEquals(event.name, captor.value.name)
    }
}
