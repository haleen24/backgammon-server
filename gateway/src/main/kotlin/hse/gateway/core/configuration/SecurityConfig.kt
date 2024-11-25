package hse.gateway.core.configuration

//import hse.gateway.core.configuration.filter.JwtAuthFilter
import hse.gateway.core.configuration.filter.JwtAuthFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {


    @Lazy
    @Autowired
    private lateinit var jwtAuthFilter: JwtAuthFilter

//    @Autowired
//    private lateinit var gatewayHeaderFilter: GatewayHeaderFilter


    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/create/user", "/login").permitAll()
                it.anyExchange().authenticated()
            }
            .sessionManagement { SessionCreationPolicy.STATELESS }
            .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.HTTP_BASIC)
            .build()

//        return http
//            .csrf { it.disable() }
////            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
//            .addFilterAfter(gatewayHeaderFilter, jwtAuthFilter::class.java)
//            .authenticationProvider(authenticationProvider)
//            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
//            .authorizeHttpRequests {
//                it.requestMatchers("/app/**").authenticated()
//                    .requestMatchers("/user/create", "/login2").permitAll()
//                    .anyRequest().permitAll()
//            }
//            .formLogin { it.permitAll() }
//            .build()
    }

    @Bean
    fun authenticationProvider(
        userDetailService: CustomUserDetailService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userDetailService)
        provider.setPasswordEncoder(passwordEncoder)
        return provider
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}