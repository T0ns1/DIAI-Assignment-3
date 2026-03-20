package pt.unl.fct.iadi.novaevents.service

import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Club.ClubCategory
import java.util.NoSuchElementException

@Service
class ClubService {
    
    private val clubs = listOf(
        Club(1L, "Chess Club", "A club for chess lovers of all skill levels.", ClubCategory.ACADEMIC),
        Club(2L, "Robotics Club", "The Robotics Club is the place to turn ideas into machines.", ClubCategory.TECHNOLOGY),
        Club(3L, "Photography Club", "Explore composition, editing, and visual storytelling through photography.", ClubCategory.ARTS),
        Club(4L, "Hiking & Outdoors Club", "Weekend hikes, outdoor adventures, and nature exploration.", ClubCategory.SPORTS),
        Club(5L, "Film Society", "Screenings, discussion nights, and appreciation of cinema.", ClubCategory.CULTURAL)
    )

    fun findAll(): List<Club> = clubs

    fun findById(id: Long): Club = clubs.find { it.id == id } ?: throw NoSuchElementException("Club with id $id not found")
}