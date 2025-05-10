package hse.menu.dao

import hse.menu.entity.Game
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface GameDao : JpaRepository<Game, Long>, PagingAndSortingRepository<Game, Long> {
}