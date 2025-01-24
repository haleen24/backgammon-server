package hse.playerservice.controller

import hse.playerservice.entity.User
import hse.playerservice.service.UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*
import player.request.AuthRequest
import player.response.JwtResponse


@RestController
class UserController(
    private val userService: UserService,
) {

    @PostMapping("/create")
    fun createUser(@RequestBody user: User): JwtResponse {
        return userService.createUser(user)
    }


    @PostMapping("/login")
    fun login(
        @RequestBody request: AuthRequest,
        response: HttpServletResponse
    ): JwtResponse {
        return userService.authenticate(request.username, request.password)
    }

    @GetMapping("/auth")
    fun auth(@RequestParam token: String): JwtResponse {
        return userService.authenticate(token)
    }

}