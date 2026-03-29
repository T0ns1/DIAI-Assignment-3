package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Sort
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import java.time.LocalDate
import java.util.NoSuchElementException

@Service
@Transactional(readOnly = true)
class EventService(
    private val clubService: ClubService,
    private val eventRepository: EventRepository,
    private val eventTypeRepository: EventTypeRepository
) {
    fun getAllEvents(type: String?, clubId: Long?): List<Event> =
        eventRepository.findAllByFilters(clubId, type?.takeIf { it.isNotBlank() })

    fun findByClubId(clubId: Long): List<Event> {
        clubService.findById(clubId)
        return eventRepository.findByClub_IdOrderByDateAscIdAsc(clubId)
    }

    fun findByIdInClub(clubId: Long, eventId: Long): Event {
        clubService.findById(clubId)
        return eventRepository.findByClub_IdAndId(clubId, eventId)
            ?: throw NoSuchElementException("Event with id $eventId not found in club with id $clubId")
    }

    fun findAllEventTypes(): List<EventType> = eventTypeRepository.findAll(Sort.by("id"))

    fun findEventTypeById(typeId: Long): EventType =
        eventTypeRepository.findById(typeId).orElseThrow { NoSuchElementException("Event type with id $typeId not found") }

    @Transactional
    fun createEvent(
        clubId: Long,
        name: String,
        date: LocalDate,
        location: String?,
        typeId: Long,
        description: String?
    ): Event {
        val club = clubService.findById(clubId)
        validateDuplicateName(name, null)

        return eventRepository.save(
            Event(
                club = club,
                name = name.trim(),
                date = date,
                location = location?.takeIf { it.isNotBlank() }?.trim(),
                type = findEventTypeById(typeId),
                description = description?.takeIf { it.isNotBlank() }?.trim()
            )
        )
    }

    @Transactional
    fun update(
        clubId: Long,
        eventId: Long,
        name: String,
        date: LocalDate,
        location: String?,
        typeId: Long,
        description: String?
    ): Event {
        val event = findByIdInClub(clubId, eventId)
        validateDuplicateName(name, eventId)

        event.name = name.trim()
        event.date = date
        event.location = location?.takeIf { it.isNotBlank() }?.trim()
        event.type = findEventTypeById(typeId)
        event.description = description?.takeIf { it.isNotBlank() }?.trim()

        return event
    }

    @Transactional
    fun delete(clubId: Long, eventId: Long) {
        eventRepository.delete(findByIdInClub(clubId, eventId))
    }

    fun validateDuplicateName(name: String, currentEventId: Long?) {
        val normalizedName = name.trim()
        val duplicate = if (currentEventId == null) {
            eventRepository.existsByNameIgnoreCase(normalizedName)
        } else {
            eventRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, currentEventId)
        }

        if (duplicate) {
            throw IllegalArgumentException("An event with this name already exists.")
        }
    }
}
