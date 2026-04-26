package pt.unl.fct.iadi.novaevents.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Component
class OpenWeatherMapClient(
    restClientBuilder: RestClient.Builder,
    @Value("\${weather.api.key}") private val apiKey: String
) : WeatherClient {
    private val httpClient: OpenWeatherMapHttpClient = HttpServiceProxyFactory
        .builderFor(
            RestClientAdapter.create(
                restClientBuilder
                    .baseUrl("https://api.openweathermap.org")
                    .build()
            )
        )
        .build()
        .createClient(OpenWeatherMapHttpClient::class.java)

    override fun getCurrentWeather(location: String): OpenWeatherMapResponse? =
        runCatching {
            httpClient.getCurrentWeather(location = location, apiKey = apiKey)
        }.getOrNull()
}
