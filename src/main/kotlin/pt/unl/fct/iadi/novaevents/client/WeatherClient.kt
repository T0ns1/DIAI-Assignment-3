package pt.unl.fct.iadi.novaevents.client

interface WeatherClient {
    fun getCurrentWeather(location: String): OpenWeatherMapResponse?
}
