package hse.playerservice.repository

import hse.playerservice.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {
    fun findByLogin(login: String): User?

    fun existsByLogin(login: String): Boolean
}