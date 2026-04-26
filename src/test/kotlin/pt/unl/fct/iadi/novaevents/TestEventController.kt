package pt.unl.fct.iadi.novaevents

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.unl.fct.iadi.novaevents.client.OpenWeatherConditionDto
import pt.unl.fct.iadi.novaevents.client.OpenWeatherMapResponse
import pt.unl.fct.iadi.novaevents.client.WeatherClient
import pt.unl.fct.iadi.novaevents.repository.EventRepository

@SpringBootTest
@AutoConfigureMockMvc
class TestEventController {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var eventRepository: EventRepository

    @MockBean
    private lateinit var weatherClient: WeatherClient

    @Test
    fun `new event form requires authentication`() {
        mockMvc.perform(get("/clubs/1/events/new"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `create event succeeds for regular club`() {
        mockMvc.perform(
            post("/clubs/1/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Integration Event")
                .param("date", "2026-07-01")
                .param("location", "")
                .param("type", "OTHER")
                .param("description", "Created in test")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("/clubs/1/events/*"))
    }

    @Test
    @WithMockUser(username = "bob", roles = ["EDITOR"])
    fun `create hiking event requires location`() {
        mockMvc.perform(
            post("/clubs/4/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Outdoor Event Without Location")
                .param("date", "2026-07-02")
                .param("location", " ")
                .param("type", "SOCIAL")
                .param("description", "Should fail")
        )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Location is required for outdoor events")))
    }

    @Test
    @WithMockUser(username = "bob", roles = ["EDITOR"])
    fun `create hiking event rejects rain`() {
        `when`(weatherClient.getCurrentWeather("Sintra"))
            .thenReturn(OpenWeatherMapResponse(listOf(OpenWeatherConditionDto("Rain"))))

        mockMvc.perform(
            post("/clubs/4/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("name", "Rainy Integration Hike")
                .param("date", "2026-07-03")
                .param("location", "Sintra")
                .param("type", "SOCIAL")
                .param("description", "Should fail")
        )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("It is currently raining at")))
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `owner can view edit form and update event`() {
        val event = eventRepository.findAll().first { it.name == "Beginner's Chess Workshop" }
        val clubId = event.club?.id ?: error("missing club id")
        val eventId = event.id ?: error("missing event id")

        mockMvc.perform(get("/clubs/$clubId/events/$eventId/edit"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Update Event")))

        mockMvc.perform(
            post("/clubs/$clubId/events/$eventId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("_method", "PUT")
                .param("name", "Beginner's Chess Workshop Updated")
                .param("date", "2026-03-11")
                .param("location", "Room A102")
                .param("type", "WORKSHOP")
                .param("description", "Updated description")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/clubs/$clubId/events/$eventId"))
    }

    @Test
    @WithMockUser(username = "charlie", roles = ["ADMIN"])
    fun `admin can access delete flow`() {
        val event = eventRepository.findAll().first { it.name == "Arduino Intro Workshop" }
        val clubId = event.club?.id ?: error("missing club id")
        val eventId = event.id ?: error("missing event id")

        mockMvc.perform(get("/clubs/$clubId/events/$eventId/delete"))
            .andExpect(status().isOk)

        mockMvc.perform(
            post("/clubs/$clubId/events/$eventId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("_method", "DELETE")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/clubs/$clubId"))
    }
}
