package pt.unl.fct.iadi.novaevents.service

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.Club.ClubCategory
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import java.time.LocalDate

@Component
class DataInitializer(
    private val eventTypeRepository: EventTypeRepository,
    private val clubRepository: ClubRepository,
    private val eventRepository: EventRepository
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (eventTypeRepository.count() > 0 || clubRepository.count() > 0 || eventRepository.count() > 0) {
            return
        }

        val eventTypes = eventTypeRepository.saveAll(
            listOf(
                EventType(name = "WORKSHOP"),
                EventType(name = "TALK"),
                EventType(name = "COMPETITION"),
                EventType(name = "SOCIAL"),
                EventType(name = "MEETING"),
                EventType(name = "OTHER")
            )
        ).associateBy { it.name }

        val clubs = clubRepository.saveAll(
            listOf(
                Club(name = "Chess Club", description = "A club for chess lovers of all skill levels.", category = ClubCategory.ACADEMIC),
                Club(name = "Robotics Club", description = "The Robotics Club is the place to turn ideas into machines.", category = ClubCategory.TECHNOLOGY),
                Club(name = "Photography Club", description = "Explore composition, editing, and visual storytelling through photography.", category = ClubCategory.ARTS),
                Club(name = "Hiking & Outdoors Club", description = "Weekend hikes, outdoor adventures, and nature exploration.", category = ClubCategory.SPORTS),
                Club(name = "Film Society", description = "Screenings, discussion nights, and appreciation of cinema.", category = ClubCategory.CULTURAL)
            )
        ).associateBy { it.name }

        eventRepository.saveAll(
            listOf(
                Event(club = clubs.getValue("Chess Club"), name = "Beginner's Chess Workshop", date = LocalDate.of(2026, 3, 10), location = "Room A101", type = eventTypes.getValue("WORKSHOP"), description = "Workshop para iniciantes"),
                Event(club = clubs.getValue("Chess Club"), name = "Spring Chess Tournament", date = LocalDate.of(2026, 4, 5), location = "Main Hall", type = eventTypes.getValue("COMPETITION"), description = "Torneio de primavera"),
                Event(club = clubs.getValue("Chess Club"), name = "Advanced Openings Talk", date = LocalDate.of(2026, 5, 20), location = "Room A101", type = eventTypes.getValue("TALK"), description = "Aberturas avancadas"),
                Event(club = clubs.getValue("Robotics Club"), name = "Arduino Intro Workshop", date = LocalDate.of(2026, 3, 15), location = "Engineering Lab 2", type = eventTypes.getValue("WORKSHOP"), description = "Introducao ao Arduino"),
                Event(club = clubs.getValue("Robotics Club"), name = "RoboCup Preparation Meeting", date = LocalDate.of(2026, 3, 28), location = "Engineering Lab 1", type = eventTypes.getValue("MEETING"), description = "Preparacao para RoboCup"),
                Event(club = clubs.getValue("Robotics Club"), name = "Sensor Integration Talk", date = LocalDate.of(2026, 4, 22), location = "Auditorium B", type = eventTypes.getValue("TALK"), description = "Integracao de sensores"),
                Event(club = clubs.getValue("Robotics Club"), name = "Regional Robotics Competition", date = LocalDate.of(2026, 6, 1), location = "Sports Hall", type = eventTypes.getValue("COMPETITION"), description = "Competicao regional"),
                Event(club = clubs.getValue("Photography Club"), name = "Night Photography Walk", date = LocalDate.of(2026, 3, 20), location = "Campus Gardens", type = eventTypes.getValue("SOCIAL"), description = "Passeio fotografico noturno"),
                Event(club = clubs.getValue("Photography Club"), name = "Lightroom Workshop", date = LocalDate.of(2026, 4, 10), location = "Lab 3", type = eventTypes.getValue("WORKSHOP"), description = "Edicao com Lightroom"),
                Event(club = clubs.getValue("Photography Club"), name = "Portrait Critique Session", date = LocalDate.of(2026, 5, 6), location = "Studio 1", type = eventTypes.getValue("MEETING"), description = "Analise coletiva de retratos"),
                Event(club = clubs.getValue("Hiking & Outdoors Club"), name = "Serra da Arrabida Hike", date = LocalDate.of(2026, 3, 22), location = "Arrabida", type = eventTypes.getValue("SOCIAL"), description = "Caminhada na Arrabida"),
                Event(club = clubs.getValue("Hiking & Outdoors Club"), name = "Trail Safety Talk", date = LocalDate.of(2026, 4, 15), location = "Room B202", type = eventTypes.getValue("TALK"), description = "Seguranca em trilhos"),
                Event(club = clubs.getValue("Film Society"), name = "Kubrick Retrospective", date = LocalDate.of(2026, 3, 25), location = "Auditorium A", type = eventTypes.getValue("OTHER"), description = "Retrospetiva de Kubrick"),
                Event(club = clubs.getValue("Film Society"), name = "Scriptwriting Workshop", date = LocalDate.of(2026, 4, 18), location = "Room C101", type = eventTypes.getValue("WORKSHOP"), description = "Escrita de guioes"),
                Event(club = clubs.getValue("Film Society"), name = "Short Film Pitch Night", date = LocalDate.of(2026, 5, 8), location = "Screening Room", type = eventTypes.getValue("SOCIAL"), description = "Apresentacao informal de ideias para curtas")
            )
        )
    }
}
