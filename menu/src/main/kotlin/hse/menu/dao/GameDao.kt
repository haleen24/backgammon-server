package hse.menu.dao

import hse.menu.entity.Game
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GameDao : CrudRepository<Game, Long> {
}