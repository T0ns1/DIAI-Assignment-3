package pt.unl.fct.iadi.novaevents

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import pt.unl.fct.iadi.novaevents.client.WeatherClient

@SpringBootTest
@AutoConfigureMockMvc
class TestClubController {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var weatherClient: WeatherClient

    @Test
    fun `home redirects to clubs`() {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/clubs"))
    }

    @Test
    fun `login page is public`() {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Sign In")))
    }

    @Test
    fun `clubs list and detail are public`() {
        mockMvc.perform(get("/clubs"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Hiking &amp; Outdoors Club")))

        mockMvc.perform(get("/clubs/1"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Chess Club")))
    }

    @Test
    fun `club events route redirects to filtered events list`() {
        mockMvc.perform(get("/clubs/1/events"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/events?clubId=1"))
    }

    @Test
    fun `events list and detail are public`() {
        mockMvc.perform(get("/events"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Beginner&#39;s Chess Workshop")))

        mockMvc.perform(get("/clubs/1/events/1"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Beginner&#39;s Chess Workshop")))
    }
}
