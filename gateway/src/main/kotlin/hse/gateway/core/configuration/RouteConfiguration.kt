package hse.gateway.core.configuration

import hse.gateway.core.service.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.RequestPredicates.path
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse

@Configuration
internal class RouteConfiguration {

    @Autowired
    private lateinit var jwtService: JwtService

    @Bean
    fun menuRouteFunction(): RouterFunction<ServerResponse> {
        return route("menu").route(path("menu/**"), http("http://localhost:81"))
            .before(authHeaderRequestProcessor()).build()
    }

    @Bean
    fun backGammonGameRouteFunction(): RouterFunction<ServerResponse> {
        return route("backgammon-game").route(path("game/backgammon/**"), http("http://localhost:82"))
            .before(authHeaderRequestProcessor()).build()
    }

    fun authHeaderRequestProcessor(): (ServerRequest) -> ServerRequest {
        return { request ->
            val token = request.cookies()["token"]?.firstOrNull()?.value
            if (token == null) {
                request
            } else {
                val userId = jwtService.extractUserId(token)
                ServerRequest.from(request).header("auth-user", userId).build()
            }
        }
    }
}