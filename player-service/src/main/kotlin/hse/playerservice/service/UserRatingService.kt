package hse.playerservice.service

import game.backgammon.enums.BackgammonType.REGULAR_GAMMON
import game.backgammon.enums.BackgammonType.SHORT_BACKGAMMON
import game.common.enums.TimePolicy
import hse.playerservice.entity.User
import hse.playerservice.entity.UserRating
import hse.playerservice.repository.UserRatingRepository
import hse.playerservice.service.UserService.Companion.NO_ID
import kafka.GameEndMessage
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.math.pow

@Service
class UserRatingService(
    val userRatingRepository: UserRatingRepository
) {
    companion object {
        const val DEFAULT_RATING = 100
    }

    fun createDefaultRating(user: User) {
        val userRating = UserRating(
            id = NO_ID,
            user = user,
            backgammonBlitz = DEFAULT_RATING,
            backgammonDefault = DEFAULT_RATING,
            nardeBlitz = DEFAULT_RATING,
            nardeDefault = DEFAULT_RATING,
            numberOfGames = 0
        )
        userRatingRepository.save(userRating)
    }

    fun findByUserId(id: Long): UserRating {
        return userRatingRepository.findByUserId(id)
    }

    fun changeRating(gameEndMessage: GameEndMessage) {
        val winnerRating = userRatingRepository.findByUserId(gameEndMessage.winnerId)
        val loserRating = userRatingRepository.findByUserId(gameEndMessage.loserId)
        val winnerCurrentRating: Int
        val loserCurrentRating: Int
        if (gameEndMessage.gameType == REGULAR_GAMMON && gameEndMessage.gameTimePolicy == TimePolicy.DEFAULT_TIMER) {
            winnerCurrentRating = winnerRating.nardeDefault
            loserCurrentRating = loserRating.nardeDefault
        } else if (gameEndMessage.gameType == REGULAR_GAMMON && gameEndMessage.gameTimePolicy == TimePolicy.BLITZ) {
            winnerCurrentRating = winnerRating.nardeBlitz
            loserCurrentRating = loserRating.nardeBlitz
        } else if (gameEndMessage.gameType == SHORT_BACKGAMMON && gameEndMessage.gameTimePolicy == TimePolicy.DEFAULT_TIMER) {
            winnerCurrentRating = winnerRating.backgammonDefault
            loserCurrentRating = loserRating.backgammonDefault
        } else if (gameEndMessage.gameType == SHORT_BACKGAMMON && gameEndMessage.gameTimePolicy == TimePolicy.BLITZ) {
            winnerCurrentRating = winnerRating.backgammonBlitz
            loserCurrentRating = loserRating.backgammonBlitz
        } else {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
        val winnerExcepted = getExpected(winnerCurrentRating, loserCurrentRating)
        val loserExcepted = getExpected(loserCurrentRating, winnerCurrentRating)
        val winnerCoefficient = getRatingCoefficient(winnerCurrentRating, winnerRating.numberOfGames)
        val loserCoefficient = getRatingCoefficient(loserCurrentRating, loserRating.numberOfGames)
        val winnerNewRating = winnerCurrentRating + winnerCoefficient * (1 - winnerExcepted)
        val loserNewRating = loserCurrentRating + loserCoefficient * (0 - loserExcepted)
        if (gameEndMessage.gameType == REGULAR_GAMMON && gameEndMessage.gameTimePolicy == TimePolicy.DEFAULT_TIMER) {
            winnerRating.nardeDefault = winnerNewRating.toInt()
            loserRating.nardeDefault = loserNewRating.toInt()
        } else if (gameEndMessage.gameType == REGULAR_GAMMON) {
            winnerRating.nardeBlitz = winnerNewRating.toInt()
            loserRating.nardeBlitz = loserNewRating.toInt()
        } else if (gameEndMessage.gameTimePolicy == TimePolicy.DEFAULT_TIMER) {
            winnerRating.backgammonDefault = winnerNewRating.toInt()
            loserRating.backgammonDefault = loserNewRating.toInt()
        } else {
            winnerRating.backgammonDefault = winnerNewRating.toInt()
            loserRating.backgammonBlitz = loserNewRating.toInt()
        }
        winnerRating.numberOfGames += 1
        loserRating.numberOfGames += 1
        userRatingRepository.save(winnerRating)
        userRatingRepository.save(loserRating)
    }

    private fun getExpected(playerRating: Int, opponentRating: Int): Double {
        return 1.0 / (1.0 + 10).pow((opponentRating - playerRating / 400))
    }

    private fun getRatingCoefficient(currentRating: Int, numberOfGame: Int): Long {
        return if (currentRating >= 2400) {
            10
        } else if (numberOfGame < 30) {
            40
        } else {
            20
        }
    }
}