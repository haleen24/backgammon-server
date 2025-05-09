package hse.menu.controller

import game.backgammon.request.CreateGameRequest
import hse.menu.service.MenuService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("menu")
class MenuController(
    private val menuService: MenuService,
) {
    companion object {
        const val AUTH_USER = "auth-user"
    }

    @PostMapping("connect")
    fun connect(
        @RequestHeader(AUTH_USER) user: Int,
        @RequestBody request: CreateGameRequest,
    ): Int {
        return menuService.connect(user, request)
    }

    @PostMapping("disconnect")
    fun disconnect(@RequestHeader(AUTH_USER) user: Int) {
        menuService.disconnect(user)
    }
}