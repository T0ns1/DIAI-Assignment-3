package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.client.WeatherClient

@Service
class WeatherService(
    private val weatherClient: WeatherClient
) {
    fun isRaining(location: String): Boolean? {
        val normalizedLocation = location.trim()
        if (normalizedLocation.isBlank()) {
            return null
        }

        val response = weatherClient.getCurrentWeather(normalizedLocation) ?: return null
        val main = response.weather.firstOrNull()?.main?.trim().orEmpty()
        if (main.isBlank()) {
            return null
        }

        return main.equals("Rain", ignoreCase = true)
    }
}
