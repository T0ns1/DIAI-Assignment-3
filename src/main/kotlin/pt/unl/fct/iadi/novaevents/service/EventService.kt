package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.Event.EventType
import java.time.LocalDate
import java.util.NoSuchElementException

@Service
class EventService(private val clubService: ClubService) {

    private val events = mutableListOf(
        // Chess Club (clubId=1)
        Event(1, 1, "Beginner's Chess Workshop", LocalDate.of(2026, 3, 10), "Room A101", EventType.WORKSHOP, "Workshop para iniciantes"),
        Event(2, 1, "Spring Chess Tournament", LocalDate.of(2026, 4, 5), "Main Hall", EventType.COMPETITION, "Torneio de primavera"),
        Event(3, 1, "Advanced Openings Talk", LocalDate.of(2026, 5, 20), "Room A101", EventType.TALK, "Aberturas avançadas"),

        // Robotics Club (clubId=2)
        Event(4, 2, "Arduino Intro Workshop", LocalDate.of(2026, 3, 15), "Engineering Lab 2", EventType.WORKSHOP, "Introdução ao Arduino"),
        Event(5, 2, "RoboCup Preparation Meeting", LocalDate.of(2026, 3, 28), "Engineering Lab 1", EventType.MEETING, "Preparação para RoboCup"),
        Event(6, 2, "Sensor Integration Talk", LocalDate.of(2026, 4, 22), "Auditorium B", EventType.TALK, "Integração de sensores"),
        Event(7, 2, "Regional Robotics Competition", LocalDate.of(2026, 6, 1), "Sports Hall", EventType.COMPETITION, "Competição regional"),

        // Photography Club (clubId=3)
        Event(8, 3, "Night Photography Walk", LocalDate.of(2026, 3, 20), "Campus Gardens", EventType.SOCIAL, "Passeio fotográfico noturno"),
        Event(9, 3, "Lightroom Workshop", LocalDate.of(2026, 4, 10), "Lab 3", EventType.WORKSHOP, "Edição com Lightroom"),

        // Hiking & Outdoors Club (clubId=4)
        Event(10, 4, "Serra da Arrábida Hike", LocalDate.of(2026, 3, 22), "Arrábida", EventType.SOCIAL, "Caminhada na Arrábida"),
        Event(11, 4, "Trail Safety Talk", LocalDate.of(2026, 4, 15), "Room B202", EventType.TALK, "Segurança em trilhos"),

        // Film Society (clubId=5)
        Event(12, 5, "Kubrick Retrospective", LocalDate.of(2026, 3, 25), "Auditorium A", EventType.OTHER, "Retrospetiva de Kubrick"),
        Event(13, 5, "Scriptwriting Workshop", LocalDate.of(2026, 4, 18), "Room C101", EventType.WORKSHOP, "Escrita de guiões"),
    )

    private var nextId = 14L

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