package hse.gateway.core.configuration.filter

import hse.gateway.core.constant.AUTH
import hse.gateway.core.constant.unsecuredUrls
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import player.response.JwtResponse
import reactor.core.publisher.Mono

@Component
class PreAuthorizedGlobalFilter(
    @Value("\${route.config.player-service.uri}") private val authUri: String,
    @Value("\${route.token.name}") private val tokenName: String,
    @Value("\${route.header.auth-user.name}") private val authUserHeaderName: String,
) : GlobalFilter, Ordered {

    private val restTemplate: RestTemplate = RestTemplate()

    override fun filter(exchange: ServerWebExchange?, chain: GatewayFilterChain?): Mono<Void> {
        val path = exchange!!.request.path.value()

        if (path in unsecuredUrls) {
            return chain!!.filter(exchange)
        }

        val currentToken =
            exchange.request.cookies[tokenName]?.firstOrNull()?.value
                ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token not found")
        val jwt = restTemplate.getForObject<JwtResponse>("$authUri/$AUTH?token=$currentToken")
        exchange.response.cookies[tokenName] =
            mutableListOf(ResponseCookie.from(tokenName, jwt.token).path("/").build())

        val newRequest = exchange.mutate().request(
            exchange.request.mutate()
                .header(authUserHeaderName, jwt.userId.toString())
                .build()
        ).build()

        return chain!!.filter(newRequest)
    }

    override fun getOrder(): Int {
        return -2
    }
}