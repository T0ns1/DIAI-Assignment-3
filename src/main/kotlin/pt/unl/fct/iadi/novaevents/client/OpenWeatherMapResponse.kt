package pt.unl.fct.iadi.novaevents.client

data class OpenWeatherMapResponse(
    val weather: List<OpenWeatherConditionDto> = emptyList()
)

data class OpenWeatherConditionDto(
    val main: String = ""
)
