package game.backgammon

import game.backgammon.dto.MoveDto
import game.backgammon.sht.ShortBackgammonGame
import java.util.*


private fun printDeck(game: ShortBackgammonGame) {
    println(game.toString())
    for (i in 13..24) {
        print(i.toString() + "\t")
        if (i == 18) {
            print("|\t")
        }
    }
    println()
    for (i in 13..24) {
        print(game.deck[i].toString() + "\t")
        if (i == 18) {
            print("|\t")
        }
    }
    print("home=-1: ${game.deck[25]}")
    println()
    for (i in 12 downTo 1) {
        print(game.deck[i].toString() + "\t")
        if (i == 7) {
            print("|\t")
        }
    }
    print("home=1: ${game.deck[0]}")
    println()
    for (i in 12 downTo 1) {
        print(i.toString() + "\t")
        if (i == 7) {
            print("|\t")
        }
    }
    println()

}


fun main() {
    val game = ShortBackgammonGame()
    var turn = game.turn

    val scanner = Scanner(System.`in`)
    while (true) {
//        if (game.zarResults.isEmpty()) {
//            game.tossBothZar(turn)
//        }
        printDeck(game)
        val moves = listOf(MoveDto(scanner.nextInt(), scanner.nextInt()), MoveDto(scanner.nextInt(), scanner.nextInt()))
        val changes = try {
            game.move(turn, moves)
        } catch (e: Exception) {
            println(e.message)
            continue
        }
        turn = game.turn
        if (game.checkEnd()) {
            println("win ${game.deck[0]} || ${game.deck[25]}")
            break
        }
    }
}
