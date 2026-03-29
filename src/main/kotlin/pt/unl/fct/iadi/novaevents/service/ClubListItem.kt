package pt.unl.fct.iadi.novaevents.service

import pt.unl.fct.iadi.novaevents.model.Club

data class ClubListItem(
    val club: Club,
    val eventCount: Long
)
