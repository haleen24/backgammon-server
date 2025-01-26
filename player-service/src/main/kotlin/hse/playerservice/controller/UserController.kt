package hse.playerservice.controller

import hse.playerservice.service.UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import player.request.*
import player.response.JwtResponse


@RestController
class UserController(
    private val userService: UserService,
) {

    companion object {
        const val AUTH_HEADER = "auth-user"
    }

    @PostMapping("/create")
    fun createUser(@RequestBody request: CreateUserRequest): JwtResponse {
        return userService.createUser(request)
    }


    @PostMapping("/login")
    fun login(
        @RequestBody request: AuthRequest,
        response: HttpServletResponse
    ): JwtResponse {
        return userService.authenticate(request.login, request.password)
    }

    @GetMapping("/auth")
    fun auth(@RequestParam token: String): JwtResponse {
        return userService.authenticate(token)
    }

    @PostMapping("/password")
    fun changePassword(
        @RequestHeader(AUTH_HEADER) userId: Long,
        @RequestBody changePasswordRequest: ChangePasswordRequest
    ): JwtResponse {
        return userService.changePassword(userId, changePasswordRequest)
    }

    @DeleteMapping("/delete")
    fun deleteUser(
        @RequestHeader(AUTH_HEADER) userId: Long,
        @RequestBody request: DeleteUserRequest
    ): ResponseEntity<Void> {
        return userService.deleteUser(userId, request)
    }

    @PutMapping("/username")
    fun updateName(
        @RequestHeader(AUTH_HEADER) userId: Long,
        @RequestBody updateUsernameRequest: UpdateUsernameRequest
    ): ResponseEntity<Void> {
        return userService.updateName(userId, updateUsernameRequest)
    }
}