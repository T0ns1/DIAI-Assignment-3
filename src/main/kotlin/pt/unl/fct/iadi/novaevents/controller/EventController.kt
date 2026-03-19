package pt.unl.fct.iadi.novaevents.controller

import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import pt.unl.fct.iadi.novaevents.controller.dto.EventFormDto
import pt.unl.fct.iadi.novaevents.model.Event.EventType
import pt.unl.fct.iadi.novaevents.service.ClubService
import pt.unl.fct.iadi.novaevents.service.EventService
import java.time.LocalDate

@Controller
class EventController(
    private val eventService: EventService,
    private val clubService: ClubService
    ) {

    @GetMapping("/events")
    fun listEvents(
        @RequestParam(required = false) type: EventType?,
        @RequestParam(required = false) clubId: Long?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
        model: Model
    ): String {
        model.addAttribute("events", eventService.getAllEvents(type, clubId, from, to))
        model.addAttribute("clubs", clubService.findAll())
        model.addAttribute("clubMap", clubService.findAll().associateBy { it.id })
        model.addAttribute("eventTypes", EventType.values())
        model.addAttribute("selectedClubId", clubId)
        model.addAttribute("from", from)
        model.addAttribute("to", to)
        return "events/list"
    }

    @GetMapping("/clubs/{clubId}/events/{eventId}")
    fun showEvent(@PathVariable clubId: Long, @PathVariable eventId: Long, model: Model): String {
        val event = eventService.findByIdInClub(clubId, eventId)
        val club = clubService.findById(clubId)
        model.addAttribute("club", club)
        model.addAttribute("event", event)
        return "events/detail"
    }


    @GetMapping("/clubs/{clubId}/events/new")
    fun createForm(@PathVariable clubId: Long, model: Model): String {
        model.addAttribute("club", clubService.findById(clubId))
        model.addAttribute("eventForm", EventFormDto())
        model.addAttribute("eventTypes", EventType.values())
        model.addAttribute("formAction", "/clubs/$clubId/events")
        model.addAttribute("formTitle", "Create New Event")
        model.addAttribute("submitLabel", "Create")
        return "events/form"
    }

    @PostMapping("/clubs/{clubId}/events")
    fun createEvent(
        @PathVariable clubId: Long,
        @Valid @ModelAttribute("eventForm") eventForm: EventFormDto,
        bindingResult: BindingResult,
        model: Model
    ): String {
        val club = clubService.findById(clubId)

        if (!bindingResult.hasFieldErrors("name")) {
            try {
                eventService.validateDuplicateName(eventForm.name, null)
            } catch (ex: IllegalArgumentException) {
                bindingResult.rejectValue("name", "duplicate", ex.message!!)
                model.addAttribute("club", club)
                model.addAttribute("eventTypes", EventType.values())
                model.addAttribute("formAction", "/clubs/$clubId/events")
                model.addAttribute("formTitle", "Create New Event")
                model.addAttribute("submitLabel", "Create")
                return "events/form"
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("club", club)
            model.addAttribute("eventTypes", EventType.values())
            model.addAttribute("formAction", "/clubs/$clubId/events")
            model.addAttribute("formTitle", "Create New Event")
            model.addAttribute("submitLabel", "Create")
            return "events/form"
        }

        val created = eventService.createEvent(clubId, eventForm)
        return "redirect:/clubs/$clubId/events/${created.id}" // To avoid duplicate form submission on refresh
    }

    @GetMapping("/clubs/{clubId}/events/{eventId}/edit")
    fun editForm(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        model: Model
    ): String {
        val club = clubService.findById(clubId)
        val event = eventService.findByIdInClub(clubId, eventId)

        model.addAttribute("club", club)
        model.addAttribute("eventForm", eventService.toFormDto(event))
        model.addAttribute("eventTypes", EventType.values())
        model.addAttribute("formAction", "/clubs/$clubId/events/$eventId")
        model.addAttribute("formTitle", "Edit Event")
        model.addAttribute("submitLabel", "Update")
        model.addAttribute("httpMethod", "put")
        return "events/form"
    }

    @PutMapping("/clubs/{clubId}/events/{eventId}")
    fun updateEvent(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        @Valid @ModelAttribute("eventForm") eventForm: EventFormDto,
        bindingResult: BindingResult,
        model: Model
    ): String {
        val club = clubService.findById(clubId)
        val event = eventService.findByIdInClub(clubId, eventId)

        if (!bindingResult.hasFieldErrors("name")) {
            try {
                eventService.validateDuplicateName(eventForm.name, eventId)
            } catch (ex: IllegalArgumentException) {
                bindingResult.rejectValue("name", "duplicate", ex.message!!)
                model.addAttribute("club", club)
                model.addAttribute("event", event)
                model.addAttribute("eventTypes", EventType.values())
                model.addAttribute("formAction", "/clubs/$clubId/events/$eventId")
                model.addAttribute("formTitle", "Edit Event")
                model.addAttribute("submitLabel", "Update")
                model.addAttribute("httpMethod", "put")
                return "events/form"
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("club", club)
            model.addAttribute("event", event)
            model.addAttribute("eventTypes", EventType.values())
            model.addAttribute("formAction", "/clubs/$clubId/events/$eventId")
            model.addAttribute("formTitle", "Edit Event")
            model.addAttribute("submitLabel", "Update")
            model.addAttribute("httpMethod", "put")
            return "events/form"
        }

        eventService.update(clubId, eventId, eventForm)
        return "redirect:/clubs/$clubId/events/$eventId" // To avoid duplicate form submission on refresh
    }

    @GetMapping("/clubs/{clubId}/events/{eventId}/delete")
    fun deleteConfirmation(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long,
        model: Model
    ): String {
        val club = clubService.findById(clubId)
        val event = eventService.findByIdInClub(clubId, eventId)

        model.addAttribute("club", club)
        model.addAttribute("event", event)
        return "events/delete"
    }

    @DeleteMapping("/clubs/{clubId}/events/{eventId}")
    fun deleteEvent(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long
    ): String {
        eventService.delete(clubId, eventId)
        return "redirect:/clubs/$clubId"
    }

    @PostMapping("/clubs/{clubId}/events/{eventId}", params = ["_method=PUT"])
fun updateEventPostOverride(
    @PathVariable clubId: Long,
    @PathVariable eventId: Long,
    @Valid @ModelAttribute("eventForm") eventForm: EventFormDto,
    bindingResult: BindingResult,
    model: Model
): String {
    return updateEvent(clubId, eventId, eventForm, bindingResult, model)
}

@PostMapping("/clubs/{clubId}/events/{eventId}", params = ["_method=DELETE"])
fun deleteEventPostOverride(
    @PathVariable clubId: Long,
    @PathVariable eventId: Long
): String {
    return deleteEvent(clubId, eventId)
}
}
