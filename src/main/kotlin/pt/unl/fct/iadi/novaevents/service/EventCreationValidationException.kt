package pt.unl.fct.iadi.novaevents.service

class EventCreationValidationException(
    val field: String?,
    override val message: String
) : RuntimeException(message)
