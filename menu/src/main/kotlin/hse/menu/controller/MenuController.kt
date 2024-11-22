package hse.menu.controller

import game.backgammon.request.CreateGameRequest
import hse.menu.adapter.GameAdapter
import hse.menu.service.MenuGameService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("menu")
class MenuController(
    private val menuGameService: MenuGameService,
    private val gameAdapter: GameAdapter,
) {
    companion object {
        private const val AUTH_USER = "auth-user"
    }

    @PostMapping("create-room")
    fun createRoom(
        @RequestHeader(AUTH_USER) user: Int,
        @RequestBody requestBody: CreateGameRequest,
    ): Int? {
        val id = menuGameService.storeRoom(requestBody.type, user)

        return gameAdapter.gameCreation(requestBody.type, id)
    }
}