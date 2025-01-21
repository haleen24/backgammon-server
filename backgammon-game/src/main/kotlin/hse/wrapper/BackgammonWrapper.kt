package hse.wrapper

import game.backgammon.Gammon
import game.backgammon.GammonRestorer
import game.backgammon.dto.ChangeDto
import game.backgammon.dto.DeckItemDto
import game.backgammon.dto.MoveDto
import game.backgammon.dto.TossZarDto
import game.backgammon.enums.BackgammonType
import game.backgammon.enums.Color
import game.backgammon.response.ConfigResponse
import hse.dto.GammonRestoreContextDto
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.math.absoluteValue
import kotlin.math.sign


class BackgammonWrapper(
    private val game: Gammon,
    private val type: BackgammonType,
) {

    companion object {
        const val BLACK_COLOR = -1
        const val WHITE_COLOR = 1

        fun buildFromContext(restoreContextDto: GammonRestoreContextDto): BackgammonWrapper {
            val gameWrapper = when (restoreContextDto.type) {
                BackgammonType.SHORT_BACKGAMMON -> BackgammonWrapper(
                    GammonRestorer.restoreBackgammon(restoreContextDto.game),
                    BackgammonType.SHORT_BACKGAMMON
                )
            }
            gameWrapper.firstPlayer = restoreContextDto.firstUserId
            gameWrapper.secondPlayer = restoreContextDto.secondUserId
            gameWrapper.numberOfMoves = restoreContextDto.numberOfMoves
            return gameWrapper
        }
    }

    private var firstPlayer: Int = -1

    private var secondPlayer: Int = -1

    var numberOfMoves: Int = 0

    fun connect(first: Int, second: Int) {
        firstPlayer = first
        secondPlayer = second
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
        return game.move(getPlayerMask(playerId), moves).also { ++numberOfMoves }
    }

    fun tossZar(): TossZarDto {
        return game.tossBothZar()
    }

    fun getPlayerColor(userId: Int): Color {
        val mask = getPlayerMask(userId)
        return getColor(mask)
    }

    fun gameEndStatus(): Map<Boolean, Color> {
        val res = game.getEndState() ?: throw ResponseStatusException(HttpStatus.TOO_EARLY, "Game not ended")
        return listOf(firstPlayer, secondPlayer).associate { (getPlayerMask(it) == res.winner) to getPlayerColor(it) }
    }

    fun checkEnd(): Boolean {
        return game.checkEnd()
    }


    fun getRestoreContext(): GammonRestoreContextDto {
        val config = game.getConfiguration()
        return GammonRestoreContextDto(
            game = GammonRestorer.GammonRestoreContext(
                deck = config.deck.mapIndexed { index, it -> index to it }.filter { it.second != 0 }.toMap(),
                turn = config.turn,
                zarResult = config.zar,
                bar = config.bar,
                endFlag = game.checkEnd(),
            ),
            firstUserId = firstPlayer,
            secondUserId = secondPlayer,
            type = type,
            numberOfMoves = numberOfMoves
        )
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
}