package hse.wrapper

import game.backgammon.Backgammon
import game.backgammon.dto.ChangeDto
import game.backgammon.dto.DeckItemDto
import game.backgammon.dto.MoveDto
import game.backgammon.dto.TossZarDto
import game.backgammon.enums.Color
import game.backgammon.response.ConfigResponse
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.math.absoluteValue
import kotlin.math.sign

class BackgammonWrapper(
    private val game: Backgammon,
) {

    companion object {
        const val BLACK_COLOR = -1
        const val WHITE_COLOR = 1
    }


    @Volatile
    private var firstPlayer: Int = -1

    @Volatile
    private var secondPlayer: Int = -1

    @Volatile
    private var isBothConnected = false

    @Volatile
    private var numberOfMoves: Int = 0

    @Synchronized
    fun connect(playerId: Int): Boolean {
        return if (firstPlayer == -1 || firstPlayer == playerId) {
            firstPlayer = playerId
            true
        } else if (secondPlayer == -1 || secondPlayer == playerId) {
            secondPlayer = playerId
            isBothConnected = true
            true
        } else {
            false
        }
    }

    fun getConfiguration(playerId: Int): ConfigResponse {
        val config = game.getConfiguration()

        return ConfigResponse(
            color = getPlayerColor(playerId),
            turn = getColor(config.turn),
            bar = config.bar.entries.associate { getColor(it.key) to it.value.absoluteValue },
            deck = config.deck
                .mapIndexed { index, it ->
                    getDeckItemDto(index, it)
                }
                .filterNotNull()
                .toSet(),
            zar = config.zar,
            first = numberOfMoves == 0
        )
    }

    fun move(playerId: Int, moves: List<MoveDto>): ChangeDto {
        checkBothConnectedAndThrow()
        return game.move(getPlayerMask(playerId), moves).also { ++numberOfMoves }
    }

    fun tossZar(): TossZarDto {
        return game.tossBothZar()
    }

    fun getPlayerColor(userId: Int): Color {
        val mask = getPlayerMask(userId)
        return getColor(mask)
    }

    fun getEndState(): Map<Boolean, Color> {
        checkBothConnectedAndThrow()
        val res = game.getEndState() ?: throw ResponseStatusException(HttpStatus.TOO_EARLY, "Game not ended")
        return listOf(firstPlayer, secondPlayer).associate { (getPlayerMask(it) == res.winner) to getPlayerColor(it) }
    }

    fun checkIsGameStarted(): Boolean {
        return isBothConnected
    }

    fun checkEnd(): Boolean {
        return game.checkEnd()
    }

    private fun getDeckItemDto(index: Int, value: Int): DeckItemDto? {
        return if (value == 0) {
            null
        } else {
            DeckItemDto(
                id = index,
                color = getColor(value.sign),
                count = value.absoluteValue
            )
        }
    }

    private fun getColor(mask: Int): Color {
        return when (mask) {
            BLACK_COLOR -> Color.BLACK
            WHITE_COLOR -> Color.WHITE
            else -> throw RuntimeException("mask should be -1 or 1, not $mask")
        }
    }

    private fun getPlayerMask(playerId: Int): Int {
        return if (firstPlayer == playerId) {
            BLACK_COLOR
        } else if (secondPlayer == playerId) {
            WHITE_COLOR
        } else {
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Player not connected"
            )
        }
    }

    private fun checkBothConnectedAndThrow() {
        if (!isBothConnected) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Players are not connected")
        }
    }
}