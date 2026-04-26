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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.unl.fct.iadi.novaevents.client.OpenWeatherConditionDto
import pt.unl.fct.iadi.novaevents.client.OpenWeatherMapResponse
import pt.unl.fct.iadi.novaevents.client.WeatherClient

@SpringBootTest
@AutoConfigureMockMvc
class TestWeatherController {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var weatherClient: WeatherClient

    @Test
    fun `api weather requires authentication`() {
        mockMvc.perform(get("/api/weather").param("location", "Lisbon"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `api weather returns json when requested`() {
        `when`(weatherClient.getCurrentWeather("Lisbon"))
            .thenReturn(OpenWeatherMapResponse(listOf(OpenWeatherConditionDto("Rain"))))

        mockMvc.perform(
            get("/api/weather")
                .param("location", "Lisbon")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().json("""{"raining":true}"""))
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `api weather returns html fragment when requested`() {
        `when`(weatherClient.getCurrentWeather("Lisbon"))
            .thenReturn(OpenWeatherMapResponse(listOf(OpenWeatherConditionDto("Clear"))))

        mockMvc.perform(
            get("/api/weather")
                .param("location", "Lisbon")
                .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Clear")))
    }

    @Test
    @WithMockUser(username = "alice", roles = ["EDITOR"])
    fun `api weather returns null when unavailable`() {
        `when`(weatherClient.getCurrentWeather("Unknown")).thenReturn(null)

        mockMvc.perform(
            get("/api/weather")
                .param("location", "Unknown")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().json("""{"raining":null}"""))
    }
}
