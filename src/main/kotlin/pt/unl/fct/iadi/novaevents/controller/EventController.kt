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

@Controller
class EventController(
    private val eventService: EventService,
    private val clubService: ClubService
    ) {

    @GetMapping("/events")
    fun listEvents(
        @RequestParam(required = false) type: EventType?,
        @RequestParam(required = false) clubId: Long?,
        model: Model
    ): String {
        val clubs = clubService.findAll()
        model.addAttribute("clubs", clubs)
        model.addAttribute("clubMap", clubs.associate { it.id to it })

        model.addAttribute("events",
            eventService.getAllEvents(type, clubId))

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
        if (bindingResult.hasErrors()) {
            model.addAttribute("club", club)
            return "events/form"
        }

        try {
            val event = eventService.createEvent(
                clubId = clubId,
                name = eventForm.name,
                date = eventForm.date!!,
                location = eventForm.location,
                type = eventForm.type!!,
                description = eventForm.description
            )
            return "redirect:/clubs/$clubId/events/${event.id}" // Avoid double post
        } catch (ex: IllegalArgumentException) {
            bindingResult.rejectValue("name", "duplicate",
                ex.message ?: "An event with this name already exists")
            model.addAttribute("club", club)
            return "events/form"
        }
    }

    @GetMapping("/clubs/{clubId}/events/{eventId}/edit")
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
            type = event.type,
            description = event.description
        )

        model.addAttribute("club", clubService.findById(clubId))
        model.addAttribute("event", event)
        model.addAttribute("eventForm", eventForm)
        return "events/update"
    }

    @PostMapping("/clubs/{clubId}/events/{eventId}", params = ["_method=PUT"])
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
            return "events/update"
        }
        try {
            val updatedEvent = eventService.update(
                clubId = clubId,
                eventId = eventId,
                name = eventForm.name,
                date = eventForm.date!!,
                location = eventForm.location,
                type = eventForm.type!!,
                description = eventForm.description
            )
            return "redirect:/clubs/$clubId/events/$eventId" // To avoid duplicate form submission on refresh
        } catch (ex: IllegalArgumentException) {
            bindingResult.rejectValue("name", "duplicate",
                ex.message ?: "An event with this name already exists")
            model.addAttribute("club", club)
            model.addAttribute("event", event)
            model.addAttribute("eventForm", eventForm)
            return "events/update"
        }
    }

    @GetMapping("/clubs/{clubId}/events/{eventId}/delete")
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
    fun deleteEvent(
        @PathVariable clubId: Long,
        @PathVariable eventId: Long
    ): String {
        eventService.delete(clubId, eventId)
        return "redirect:/clubs/$clubId"
    }

    @DeleteMapping("/clubs/{clubId}/events/{id}")
    fun deleteEventDelete(@PathVariable clubId: Long, @PathVariable id: Long): String {
        return deleteEvent(clubId, id)
    }

    @PutMapping("/clubs/{clubId}/events/{id}")
    fun updateEventPut(@PathVariable clubId: Long, @PathVariable id: Long,
                       @Valid @ModelAttribute("form") form: EventFormDto, bindingResult: BindingResult, model: Model): String {
        return updateEvent(clubId, id, form, bindingResult, model)
    }
}
