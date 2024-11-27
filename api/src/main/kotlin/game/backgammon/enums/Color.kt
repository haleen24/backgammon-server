package game.backgammon.enums

enum class Color {
    BLACK,
    WHITE;

    fun getOpponent(): Color {
        return when (this) {
            WHITE -> BLACK
            BLACK -> WHITE
        }
    }
}