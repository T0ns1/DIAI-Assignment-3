package pt.unl.fct.iadi.novaevents.controller

import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import pt.unl.fct.iadi.novaevents.controller.dto.EventFormDto
import pt.unl.fct.iadi.novaevents.service.ClubService
import pt.unl.fct.iadi.novaevents.service.EventService
import java.security.Principal
import org.springframework.security.access.prepost.PreAuthorize
import pt.unl.fct.iadi.novaevents.service.EventCreationValidationException

@Controller
class EventController(
    private val eventService: EventService,
    private val clubService: ClubService
    ) {

    @GetMapping("/events")
    fun listEvents(
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) clubId: Long?,
        model: Model
    ): String {
        val events = eventService.getAllEvents(type, clubId)
        val clubs = events.mapNotNull { it.club }.distinctBy { it.id }.sortedBy { it.id }
        val eventTypes = events.mapNotNull { it.type }.distinctBy { it.id }.sortedBy { it.id }

        model.addAttribute("clubs", clubs)
        model.addAttribute("eventTypes", eventTypes)
        model.addAttribute("selectedType", type)
        model.addAttribute("selectedClubId", clubId)
        model.addAttribute("events", events)

        return "events/list"
    }

    @GetMapping("/clubs/{clubId}/events/{eventId}")
    fun showEvent(
            @PathVariable clubId: Long,
            @PathVariable eventId: Long,
            model: Model
    ): String {
        model.addAttribute("club", clubService.findById(clubId))
        model.addAttribute("event", eventService.findByIdInClub(clubId, eventId))
        return "events/detail"
    }


    @GetMapping("/clubs/{clubId}/events/new")
    fun createForm(@PathVariable clubId: Long, model: Model): String {
        model.addAttribute("club", clubService.findById(clubId))
        model.addAttribute("eventForm", EventFormDto())
        model.addAttribute("eventTypes", eventService.findAllEventTypes())
        return "events/form"
    }

    @PostMapping("/clubs/{clubId}/events")
    fun createEvent(
        @PathVariable clubId: Long,
        @Valid @ModelAttribute("eventForm") eventForm: EventFormDto,
        bindingResult: BindingResult,
        model: Model,
        principal: Principal
    ): String {
        val club = clubService.findById(clubId)
        if (bindingResult.hasErrors()) {
            model.addAttribute("club", club)
            model.addAttribute("eventTypes", eventService.findAllEventTypes())
            return "events/form"
        }

        try {
            val event = eventService.createEvent(
                clubId = clubId,
                name = eventForm.name,
                date = eventForm.date!!,
                location = eventForm.location,
                typeName = eventForm.type,
                description = eventForm.description,
                ownerUsername = principal.name
            )
            return "redirect:/clubs/$clubId/events/${event.id}"
        } catch (ex: EventCreationValidationException) {
            if (ex.field != null) {
                bindingResult.rejectValue(ex.field, "invalid", ex.message)
            } else {
                bindingResult.reject("invalid", ex.message)
            }
            model.addAttribute("club", club)
            model.addAttribute("eventTypes", eventService.findAllEventTypes())
            return "events/form"
        } catch (ex: IllegalArgumentException) {
            bindingResult.rejectValue("name", "duplicate",
                ex.message ?: "An event with this name already exists")
            model.addAttribute("club", club)
            model.addAttribute("eventTypes", eventService.findAllEventTypes())
            return "events/form"
        }
    }

    @GetMapping("/clubs/{clubId}/events/{eventId}/edit")
    @PreAuthorize("@eventAuthorization.canEdit(#eventId, authentication.name)")
    fun editForm(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        model: Model
    ): String {
        val event = eventService.findByIdInClub(clubId, eventId)

        val eventForm = EventFormDto(
            name = event.name,
            date = event.date,
            location = event.location,
            type = event.type?.name ?: "",
            description = event.description
        )

        model.addAttribute("club", clubService.findById(clubId))
        model.addAttribute("event", event)
        model.addAttribute("eventForm", eventForm)
        model.addAttribute("eventTypes", eventService.findAllEventTypes())
        return "events/update"
    }

    @PostMapping("/clubs/{clubId}/events/{eventId}", params = ["_method=PUT"])
    @PreAuthorize("@eventAuthorization.canEdit(#eventId, authentication.name)")
    fun updateEvent(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        @Valid @ModelAttribute("eventForm") eventForm: EventFormDto,
        bindingResult: BindingResult,
        model: Model
    ): String {
        val club = clubService.findById(clubId)
        val event = eventService.findByIdInClub(clubId, eventId)
        if (bindingResult.hasErrors()) {
            model.addAttribute("club", club)
            model.addAttribute("event", event)
            model.addAttribute("eventForm", eventForm)
            model.addAttribute("eventTypes", eventService.findAllEventTypes())
            return "events/update"
        }
        try {
            eventService.update(
                clubId = clubId,
                eventId = eventId,
                name = eventForm.name,
                date = eventForm.date!!,
                location = eventForm.location,
                typeName = eventForm.type,
                description = eventForm.description
            )
            return "redirect:/clubs/$clubId/events/$eventId"
        } catch (ex: IllegalArgumentException) {
            bindingResult.rejectValue("name", "duplicate",
                ex.message ?: "An event with this name already exists")
            model.addAttribute("club", club)
            model.addAttribute("event", event)
            model.addAttribute("eventForm", eventForm)
            model.addAttribute("eventTypes", eventService.findAllEventTypes())
            return "events/update"
        }
    }

    @GetMapping("/clubs/{clubId}/events/{eventId}/delete")
    @PreAuthorize("@eventAuthorization.canDelete(#eventId, authentication)")
    fun deleteConfirmation(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        model: Model
    ): String {
        model.addAttribute("club", clubService.findById(clubId))
        model.addAttribute("event", eventService.findByIdInClub(clubId, eventId))
        return "events/delete"
    }

    @PostMapping("/clubs/{clubId}/events/{eventId}", params = ["_method=DELETE"])
    @PreAuthorize("@eventAuthorization.canDelete(#eventId, authentication)")
    fun deleteEvent(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long
    ): String {
        eventService.delete(clubId, eventId)
        return "redirect:/clubs/$clubId"
    }

    @DeleteMapping("/clubs/{clubId}/events/{eventId}")
    @PreAuthorize("@eventAuthorization.canDelete(#eventId, authentication)")
    fun deleteEventDelete(@PathVariable clubId: Long, @PathVariable eventId: Long): String {
        return deleteEvent(clubId, eventId)
    }

    @PutMapping("/clubs/{clubId}/events/{eventId}")
    @PreAuthorize("@eventAuthorization.canEdit(#eventId, authentication.name)")
    fun updateEventPut(@PathVariable clubId: Long, @PathVariable eventId: Long,
                       @Valid @ModelAttribute("eventForm") eventForm: EventFormDto, bindingResult: BindingResult, model: Model): String {
        return updateEvent(clubId, eventId, eventForm, bindingResult, model)
    }
}
