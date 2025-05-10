package hse.service

import hse.adapter.MenuAdapter
import org.springframework.stereotype.Service

@Service
class MenuService(
    private val menuAdapter: MenuAdapter,
) {
    fun setEndStatus(matchId: Int) {
        menuAdapter.sendEndGame(matchId)
    }
}