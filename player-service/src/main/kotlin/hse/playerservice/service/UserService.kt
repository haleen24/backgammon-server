package hse.playerservice.service

import hse.playerservice.entity.User
import hse.playerservice.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import player.response.JwtResponse
import javax.security.sasl.AuthenticationException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    fun createUser(user: User): JwtResponse {

        if (userRepository.existsByUsername(user.username)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists")
        }

        val saved = userRepository.save(user.copy(password = passwordEncoder.encode(user.password)))

        val token = jwtService.generateToken(saved)

        return JwtResponse(token = token, userId = saved.id)

    }

    fun findUser(username: String): User? {
        return userRepository.findByUsername(username)
    }

    fun authenticate(login: String, password: String): JwtResponse {
        val user = findUser(login) ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        return if (passwordEncoder.matches(password, user.password)) {
            JwtResponse(token = jwtService.generateToken(user), user.id)
        } else
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED)

    }

    fun authenticate(token: String): JwtResponse {
        return if (jwtService.validateToken(token)) {
            val newToken = jwtService.refreshToken(token)
            JwtResponse(
                token = newToken,
                userId = jwtService.extractUserId(newToken).toLong()
            )
        } else {
            throw AuthenticationException("invalid token")
        }
    }
}