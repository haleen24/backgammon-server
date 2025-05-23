package hse.user.service

import hse.gateway.core.entity.User
import hse.user.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*


@Service
class JwtService(
    @Value("\${jwt.secret}") val secret: String,
    @Value("\${jwt.expire}") val expireTime: Long

) {
    private final val key = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))


    fun generateToken(user: User): String {
        return createToken(user.id, user.username)
    }

    fun validateToken(token: String?, userDetails: UserDetails): Boolean {
        token ?: return false
        val claims = extractAllClaims(token)
        val username: String = claims.subject
        val expiresAt = claims.expiration

        return (username == userDetails.username && !isExpired(expiresAt))
    }

    fun extractUserName(token: String): String {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.subject
    }

    fun extractUserId(token: String): String {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload.id
    }

    private fun isExpired(date: Date): Boolean {
        return date.before(Date.from(Instant.now()))
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun createToken(id: Long, username: String): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(expireTime)

        return Jwts.builder()
            .subject(username)
            .id(id.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key).compact();
    }
}