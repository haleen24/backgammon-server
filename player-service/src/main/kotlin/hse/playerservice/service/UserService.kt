package hse.playerservice.service

import hse.playerservice.entity.User
import hse.playerservice.repository.UserImageStorage
import hse.playerservice.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import player.request.*
import player.response.JwtResponse
import player.response.UserInfoResponse
import javax.security.sasl.AuthenticationException
import kotlin.jvm.optionals.getOrNull
import kotlin.math.pow

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val userImageStorage: UserImageStorage,
    val passwordEncoder: PasswordEncoder,
) {
    companion object {
        const val NO_ID = 0L
        val allowedImageTypes = setOf("jpg", "png", "jpeg")
        val maxImageSize = (50 * 10.0.pow(6.0)).toLong()
    }

    fun createUser(request: CreateUserRequest): JwtResponse {

        if (userRepository.existsByLogin(request.login)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists")
        }

        val user = User(
            id = NO_ID,
            login = request.login,
            username = request.username,
            password = passwordEncoder.encode(request.password),
        )

        val saved = userRepository.save(user)

        val token = jwtService.generateToken(saved)

        return JwtResponse(token = token, userId = saved.id)

    }

    fun findUser(login: String): User? {
        return userRepository.findByLogin(login)
    }

    fun findUser(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    fun findAllUsers(ids: List<Long>): List<User> {
        return userRepository.findAllById(ids)
    }

    fun findUserNotNull(login: String): User {
        return findUser(login) ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
    }

    fun findUserNotNull(id: Long): User {
        return findUser(id) ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
    }

    fun authenticate(login: String, password: String): JwtResponse {
        val user = findUserNotNull(login)
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

    fun changePassword(userId: Long, chanePasswordRequest: ChangePasswordRequest): JwtResponse {
        val user = findUserNotNull(userId)
        if (!passwordEncoder.matches(chanePasswordRequest.oldPassword, user.password)) {
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Cant change password: inputted incorrect old password"
            )
        }
        val saved = userRepository.save(user.copy(password = passwordEncoder.encode(chanePasswordRequest.newPassword)))
        return JwtResponse(token = jwtService.generateToken(saved), user.id)
    }

    fun deleteUser(userId: Long, request: DeleteUserRequest): ResponseEntity<Void> {
        val user = findUserNotNull(userId)
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Cant delete user, password doesnt match"
            )
        }
        userRepository.deleteById(userId)
        return ResponseEntity(HttpStatus.OK)
    }

    fun updateName(userId: Long, request: UpdateUsernameRequest): ResponseEntity<Void> {
        val user = findUserNotNull(userId).copy(username = request.newUsername)
        userRepository.save(user)
        return ResponseEntity(HttpStatus.OK)
    }

    fun changeInvitePolicy(userId: Long, request: ChangeInvitePolicyRequest): ResponseEntity<Void> {
        userRepository.changePolicy(userId, request.newPolicy.name)
        return ResponseEntity(HttpStatus.OK)
    }

    fun saveUserImage(userId: Long, image: MultipartFile): ResponseEntity<Void> {
        val extension = StringUtils.getFilenameExtension(image.originalFilename) ?: throw ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "file without extension"
        )
        if (extension !in allowedImageTypes || image.size > maxImageSize) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be less then 50 megabytes")
        }
        userImageStorage.storeImage(userId, image, extension)
        return ResponseEntity(HttpStatus.OK)
    }

    fun getImage(userId: Long): UserImageStorage.ImageWithExtension {
        return userImageStorage.getImage(userId)
    }

    fun getUserInfo(id: Long): UserInfoResponse {
        val user = userRepository.findById(id).getOrNull() ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "User not found"
        )
        return UserInfoResponse(username = user.username)
    }
}