package pt.unl.fct.iadi.novaevents.security

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pt.unl.fct.iadi.novaevents.repository.EventRepository

@Component("eventAuthorization")
class EventAuthorization(
    private val eventRepository: EventRepository
) {
    @Transactional(readOnly = true)
    fun canEdit(eventId: Long, username: String): Boolean {
        val event = eventRepository.findById(eventId).orElse(null) ?: return false
        return event.owner?.username == username
    }

    @Transactional(readOnly = true)
    fun canDelete(eventId: Long, authentication: Authentication): Boolean {
        val isAdmin = authentication.authorities.any { it.authority == "ROLE_ADMIN" }
        if (isAdmin) {
            return true
        }

        val event = eventRepository.findById(eventId).orElse(null) ?: return false
        return event.owner?.username == authentication.name
    }
}
