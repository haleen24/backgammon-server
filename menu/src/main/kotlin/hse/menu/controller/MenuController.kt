package hse.menu.controller

import game.backgammon.request.CreateGameRequest
import hse.menu.service.MenuGameService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("menu")
class MenuController(
    private val menuGameService: MenuGameService,
) {
    companion object {
        private const val AUTH_USER = "auth-user"
    }

    @PostMapping("connect")
    fun connect(
        @RequestHeader(AUTH_USER) user: Int,
        @RequestBody request: CreateGameRequest,
    ): Int {
        return menuGameService.connect(user, request)
    }
}