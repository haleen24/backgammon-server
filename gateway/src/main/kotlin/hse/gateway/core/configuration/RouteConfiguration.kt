package hse.gateway.core.configuration

import hse.gateway.core.service.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.RequestPredicates.path
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Configuration
internal class RouteConfiguration(
    @Value("\${route.token.name}")
    private val tokenName: String,
    @Value("\${route.header.auth-user.name}")
    private val authUserHeaderName: String
) {

    @Autowired
    private lateinit var jwtService: JwtService

    @Bean
    fun menuRouteFunction(
        @Value("\${route.config.menu.id}") routeId: String,
        @Value("\${route.config.menu.path}") routePath: String,
        @Value("\${route.config.menu.host}") routeHost: String,
    ): RouterFunction<ServerResponse> {
        return route(routeId).route(path(routePath), http(routeHost))
            .before(authHeaderRequestProcessor()).build()
    }

    @Bean
    fun backGammonGameRouteFunction(
        @Value("\${route.config.backgammon-game.id}") routeId: String,
        @Value("\${route.config.backgammon-game.path}") routePath: String,
        @Value("\${route.config.backgammon-game.host}") routeHost: String,
    ): RouterFunction<ServerResponse> {
        return route(routeId).route(path(routePath), http(routeHost))
            .before(authHeaderRequestProcessor()).build()
    }

    fun authHeaderRequestProcessor(): (ServerRequest) -> ServerRequest {
        return { request ->
            val token = request.cookies()[tokenName]?.firstOrNull()?.value
            if (token == null) {
                request
            } else {
                val userId = jwtService.extractUserId(token)
                ServerRequest.from(request).header(authUserHeaderName, userId).build()
            }
        }
    }
}