package hse.gateway.core.configuration

import hse.gateway.core.service.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity.http
import org.springframework.web.reactive.function.server.RequestPredicates.path
import org.springframework.web.reactive.function.server.RouterFunctions.route
import java.util.function.Consumer

@Configuration
internal class RouteConfiguration {

    @Autowired
    private lateinit var jwtService: JwtService

    @Bean
    fun menuRouteFunction(routeLocatorBuilder: RouteLocatorBuilder): RouteLocator {
        return routeLocatorBuilder.routes()
            .route("menu") {
                it.path("menu/**").customize { consumer -> authHeaderRequestProcessor(consumer) }
                    .uri("http://localhost:81")
            }
            .route("backgammon-game") {
                it.path("game/backgammon/**").customize { consumer -> authHeaderRequestProcessor(consumer) }
                    .uri("http://localhost:82")
            }
            .build()
    }

    fun authHeaderRequestProcessor(builder: Route.AsyncBuilder) {
        builder.filter { exchange, chain ->
            val token = exchange.request.cookies["token"]?.firstOrNull()?.value ?: return@filter chain.filter(exchange)
            val userId = jwtService.extractUserId(token)

            exchange.request.mutate().header("auth-user", userId)
            chain.filter(exchange)
        }
    }
}