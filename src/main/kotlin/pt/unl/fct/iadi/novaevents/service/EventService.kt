package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.controller.dto.EventFormDto
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.Event.EventType
import java.time.LocalDate
import java.util.NoSuchElementException

@Service
class EventService(private val clubService: ClubService) {
   
    private val events = mutableListOf(
        Event(1L, 1L, "Beginner Chess Workshop", LocalDate.now().plusDays(7), "Room A1", EventType.WORKSHOP, "Learn core openings and tactics."),
        Event(2L, 2L, "Intro to Arduino", LocalDate.now().plusDays(10), "Lab 2", EventType.WORKSHOP, "Hands-on robotics session."),
        Event(3L, 3L, "Campus Photo Walk", LocalDate.now().plusDays(5), "Main Gate", EventType.SOCIAL, "Walk and shoot around campus."),
        Event(4L, 4L, "Sintra Trail Day", LocalDate.now().plusDays(14), "Sintra Station", EventType.SOCIAL, "A full-day hiking trip."),
        Event(5L, 5L, "Classic Cinema Night", LocalDate.now().plusDays(3), "Auditorium", EventType.MEETING, "Film screening and discussion.")
    )

    private var nextId = events.size + 1L

    fun getAllEvents(
        type: EventType?,
        clubId: Long?,
        from: LocalDate?,
        to: LocalDate?
    ): List<Event> = 
        events.filter { event ->
            (type == null || event.type == type) &&
            (clubId == null || event.clubId == clubId) &&
            (from == null || !event.date.isBefore(from)) &&
            (to == null || !event.date.isAfter(to))
        }

    fun findByClubId(clubId: Long): List<Event> {
        clubService.findById(clubId) // Ensure club exists, will throw NoSuchElementException if not
        return events.filter { it.clubId == clubId }
    }

    fun findByIdInClub(clubId: Long, eventId: Long): Event {
        clubService.findById(clubId) // Ensure club exists, will throw NoSuchElementException if not
        return events.find { it.id == eventId && it.clubId == clubId }
            ?: throw NoSuchElementException("Event with id $eventId not found in club with id $clubId")
    }

    fun createEvent(clubId: Long, form: EventFormDto): Event {
        clubService.findById(clubId) // Ensure club exists, will throw NoSuchElementException if not
        validateDuplicateName(form.name, null)

        val newEvent = Event(
            id = nextId++,
            clubId = clubId,
            name = form.name.trim(),
            date = form.date!!,
            location = form.location.takeIf { it.isNotBlank() },
            type = form.type!!,
            description = form.description.takeIf { it.isNotBlank() }
        )

        events.add(newEvent)
        return newEvent
    }

    fun update(clubId: Long, eventId: Long, form: EventFormDto): Event {
        val event = findByIdInClub(clubId, eventId)
        validateDuplicateName(form.name, eventId)

        event.name = form.name.trim()
        event.date = form.date!!
        event.location = form.location.takeIf { it.isNotBlank() }
        event.type = form.type!!
        event.description = form.description.takeIf { it.isNotBlank() }

        return event
    }

    fun delete(clubId: Long, eventId: Long) {
        val event = findByIdInClub(clubId, eventId)
        events.remove(event)
    }

    fun validateDuplicateName(name: String, currentEventId: Long?) {
        val normalizedNewName = name.trim().lowercase()
        val duplicate = events.any {
            it.id != currentEventId && it.name.trim().lowercase() == normalizedNewName
        }
        if (duplicate) {
            throw IllegalArgumentException("An event with this name already exists.")
        }
    }

    fun toFormDto(event: Event): EventFormDto = EventFormDto(
        name = event.name,
        date = event.date,
        location = event.location ?: "",
        type = event.type,
        description = event.description ?: ""
    )
}