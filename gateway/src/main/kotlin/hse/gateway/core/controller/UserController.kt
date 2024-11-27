package hse.gateway.core.controller

import hse.gateway.core.dto.request.AuthRequest
import hse.gateway.core.dto.response.JwtResponse
import hse.gateway.core.entity.User
import hse.gateway.core.service.JwtService
import hse.gateway.core.service.UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseCookie
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class UserController(
    private val userService: UserService,
    private val jwtService: JwtService,
) {

    @PostMapping("/create`/user")
    fun createUser(@RequestBody user: User) {
        userService.createUser(user)
    }


    @PostMapping("/login2")
    fun authenticateAndGetToken(
        @RequestBody request: AuthRequest,
        response: HttpServletResponse
    ): JwtResponse {
        val user = userService.authenticate(request.username, request.password)

        if (user != null) {
            val token = jwtService.generateToken(user)
            val cookies = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false)
                .build()
            response.addHeader(SET_COOKIE, cookies.toString())
            return JwtResponse(token)
        }
        throw RuntimeException()
    }

}