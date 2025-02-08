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
        const val AUTH_USER = "auth-user"
    }

    @PostMapping("connect")
    fun connect(
        @RequestHeader(AUTH_USER) user: Int,
        @RequestBody request: CreateGameRequest,
    ): Int {
        return menuGameService.connect(user, request)
    }

    @PostMapping("disconnect")
    fun disconnect(@RequestHeader(AUTH_USER) user: Int) {
        menuGameService.disconnect(user)
    }
}