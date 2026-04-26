package pt.unl.fct.iadi.novaevents.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pt.unl.fct.iadi.novaevents.client.OpenWeatherConditionDto
import pt.unl.fct.iadi.novaevents.client.OpenWeatherMapResponse
import pt.unl.fct.iadi.novaevents.client.WeatherClient

class TestWeatherService {
    @Test
    fun `isRaining returns true for rain response`() {
        val service = WeatherService(FakeWeatherClient(OpenWeatherMapResponse(listOf(OpenWeatherConditionDto("Rain")))))

        assertEquals(true, service.isRaining("Lisbon"))
    }

    @Test
    fun `isRaining returns false for clear response`() {
        val service = WeatherService(FakeWeatherClient(OpenWeatherMapResponse(listOf(OpenWeatherConditionDto("Clear")))))

        assertEquals(false, service.isRaining("Lisbon"))
    }

    @Test
    fun `isRaining returns null when data is unavailable`() {
        val service = WeatherService(FakeWeatherClient(null))

        assertEquals(null, service.isRaining("Lisbon"))
        assertEquals(null, service.isRaining("   "))
    }

    private class FakeWeatherClient(
        private val response: OpenWeatherMapResponse?
    ) : WeatherClient {
        override fun getCurrentWeather(location: String): OpenWeatherMapResponse? = response
    }
}
