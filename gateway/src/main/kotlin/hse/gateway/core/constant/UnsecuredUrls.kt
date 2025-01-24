package hse.gateway.core.constant

const val PLAYER_SERVICE = "/player"

const val LOGIN = "$PLAYER_SERVICE/login"

const val REGISTER = "$PLAYER_SERVICE/create"

const val AUTH = "$PLAYER_SERVICE/auth"

val unsecuredUrls = setOf(
    LOGIN,
    REGISTER,
    AUTH
)

val toAuth = setOf(
    LOGIN,
    REGISTER,
    AUTH
)