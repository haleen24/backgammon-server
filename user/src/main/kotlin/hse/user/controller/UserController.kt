package hse.user.controller

import hse.user.dto.request.AuthRequest
import hse.user.dto.response.JwtResponse
import hse.user.entity.User
import hse.user.service.JwtService
import hse.user.service.UserService
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

    @PostMapping("/user/create")
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
                .httpOnly(false)
                .secure(false)
                .build()
            response.addHeader(SET_COOKIE, cookies.toString())
            return JwtResponse(token)
        }
        throw RuntimeException()
    }

}