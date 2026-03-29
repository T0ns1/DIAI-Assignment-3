package pt.unl.fct.iadi.novaevents.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import pt.unl.fct.iadi.novaevents.service.ClubService
import pt.unl.fct.iadi.novaevents.service.EventService

@Controller
@RequestMapping("/clubs")
class ClubController(
    private val clubService: ClubService,
    private val eventService: EventService
) {

    @GetMapping
    fun listClubs(model: Model): String {
        model.addAttribute("clubs", clubService.findAllForList())
        return "clubs/list"
    }

    @GetMapping("/{clubId}")
    fun clubDetail(@PathVariable clubId: Long, model: Model): String {
        val club = clubService.findById(clubId)
        model.addAttribute("club", club)
        return "clubs/detail"
    }

    @GetMapping("/{clubId}/events")
    fun clubEvents(@PathVariable clubId: Long, model: Model): String {
        val club = clubService.findById(clubId)
        val events = eventService.findByClubId(clubId)

        model.addAttribute("club", club)
        model.addAttribute("events", events)
        return "redirect:/events?clubId=$clubId"
    }
}
