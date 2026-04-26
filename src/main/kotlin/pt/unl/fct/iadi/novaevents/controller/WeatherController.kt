package pt.unl.fct.iadi.novaevents.controller

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import pt.unl.fct.iadi.novaevents.service.WeatherService

@Controller
class WeatherController(
    private val weatherService: WeatherService
) {
    @GetMapping("/api/weather", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun weatherJson(@RequestParam location: String): Map<String, Boolean?> =
        mapOf("raining" to weatherService.isRaining(location))

    @GetMapping("/api/weather", produces = [MediaType.TEXT_HTML_VALUE])
    fun weatherHtml(@RequestParam location: String, model: Model): String {
        model.addAttribute("raining", weatherService.isRaining(location))
        return "fragments/weather :: status"
    }
}
