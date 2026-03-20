package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.Event.EventType
import java.time.LocalDate
import java.util.NoSuchElementException

@Service
class EventService(private val clubService: ClubService) {

    // One event for each club
    private val events = mutableListOf(
        Event(1L, 1L, "Beginner's Chess Workshop", LocalDate.now().plusDays(7), "Room A1", EventType.WORKSHOP, "Learn core openings and tactics."),
        Event(2L, 2L, "Intro to Arduino", LocalDate.now().plusDays(10), "Lab 2", EventType.WORKSHOP, "Hands-on robotics session."),
        Event(3L, 3L, "Campus Photo Walk", LocalDate.now().plusDays(5), "Main Gate", EventType.SOCIAL, "Walk and shoot around campus."),
        Event(4L, 4L, "Sintra Trail Day", LocalDate.now().plusDays(14), "Sintra Station", EventType.SOCIAL, "A full-day hiking trip."),
        Event(5L, 5L, "Classic Cinema Night", LocalDate.now().plusDays(3), "Auditorium", EventType.MEETING, "Film screening and discussion.")
    )

    private var nextId = events.size + 1L

    fun getAllEvents(
        type: EventType?,
        clubId: Long?,
    ): List<Event> = 
        events.filter { event ->
            (type == null || event.type == type) &&
            (clubId == null || event.clubId == clubId)
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

    fun createEvent(clubId: Long, name: String, date: LocalDate,
                    location: String?, type: EventType, description: String?): Event {
        clubService.findById(clubId) // Ensure club exists, will throw NoSuchElementException if not
        validateDuplicateName(name) // No name dupes

        val newEvent = Event(
            id = nextId++,
            clubId = clubId,
            name = name,
            date = date,
            location = location,
            type = type,
            description = description
        )

        events.add(newEvent)
        return newEvent
    }

    fun update(clubId: Long, eventId: Long, name: String,
               date: LocalDate, location: String?, type: EventType, description: String?): Event {
        val event = findByIdInClub(clubId, eventId)
        if (events.any { it.id!= clubId && it.name.equals(name, true) }) { throw IllegalArgumentException("An event with this name already exists") }

        event.name = name
        event.date = date
        event.location = location
        event.type = type
        event.description = description

        return event
    }

    fun delete(clubId: Long, eventId: Long) {
        val event = findByIdInClub(clubId, eventId)
        events.remove(event)
    }

    private fun validateDuplicateName(name: String) {
        if (events.any { it.name.equals(name, ignoreCase = true) })
            throw IllegalArgumentException("An event with this name already exists")
    }
}