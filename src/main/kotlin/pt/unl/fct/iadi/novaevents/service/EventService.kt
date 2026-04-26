package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Sort
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.AppUserRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import java.time.LocalDate
import java.util.NoSuchElementException

@Service
@Transactional(readOnly = true)
class EventService(
    private val clubService: ClubService,
    private val eventRepository: EventRepository,
    private val eventTypeRepository: EventTypeRepository,
    private val appUserRepository: AppUserRepository,
    private val weatherService: WeatherService
) {
    companion object {
        const val HIKING_AND_OUTDOORS_CLUB = "Hiking & Outdoors Club"
    }

    fun getAllEvents(type: String?, clubId: Long?): List<Event> =
        eventRepository.findAllByFilters(clubId, type?.takeIf { it.isNotBlank() })

    fun findByClubId(clubId: Long): List<Event> {
        clubService.findById(clubId)
        return eventRepository.findByClubIdWithDetails(clubId)
    }

    fun findByIdInClub(clubId: Long, eventId: Long): Event {
        clubService.findById(clubId)
        return eventRepository.findByClubIdAndIdWithDetails(clubId, eventId)
            ?: throw NoSuchElementException("Event with id $eventId not found in club with id $clubId")
    }

    fun findAllEventTypes(): List<EventType> = eventTypeRepository.findAll(Sort.by("id"))

    fun findEventTypeById(typeId: Long): EventType =
        eventTypeRepository.findById(typeId).orElseThrow { NoSuchElementException("Event type with id $typeId not found") }

    fun findEventTypeByName(typeName: String): EventType =
        eventTypeRepository.findByNameIgnoreCase(typeName.trim())
            ?: throw NoSuchElementException("Event type with name $typeName not found")

    @Transactional
    fun createEvent(
        clubId: Long,
        name: String,
        date: LocalDate,
        location: String?,
        typeName: String,
        description: String?,
        ownerUsername: String
    ): Event {
        val club = clubService.findById(clubId)
        val owner = appUserRepository.findByUsername(ownerUsername)
            ?: throw NoSuchElementException("User with username $ownerUsername not found")
        validateOutdoorEvent(club.name, location)
        validateDuplicateName(name, null)

        return eventRepository.save(
            Event(
                club = club,
                name = name.trim(),
                date = date,
                location = location?.takeIf { it.isNotBlank() }?.trim(),
                type = findEventTypeByName(typeName),
                owner = owner,
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
        typeName: String,
        description: String?
    ): Event {
        val event = findByIdInClub(clubId, eventId)
        validateDuplicateName(name, eventId)

        event.name = name.trim()
        event.date = date
        event.location = location?.takeIf { it.isNotBlank() }?.trim()
        event.type = findEventTypeByName(typeName)
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

    private fun validateOutdoorEvent(clubName: String, location: String?) {
        if (clubName != HIKING_AND_OUTDOORS_CLUB) {
            return
        }

        val normalizedLocation = location?.trim().orEmpty()
        if (normalizedLocation.isBlank()) {
            throw EventCreationValidationException(
                field = "location",
                message = "Location is required for outdoor events"
            )
        }

        if (weatherService.isRaining(normalizedLocation) == true) {
            throw EventCreationValidationException(
                field = "location",
                message = "It is currently raining at \"$normalizedLocation\" — outdoor events cannot be created in bad weather"
            )
        }
    }
}
