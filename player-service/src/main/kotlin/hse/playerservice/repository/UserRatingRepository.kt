package hse.playerservice.repository

import hse.playerservice.entity.UserRating
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRatingRepository : CrudRepository<UserRating, Int> {
    fun findByUserId(userId: Long): UserRating
}