package hse.user.config

import hse.user.config.filter.JwtAuthFilter
import hse.user.config.filter.SseFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig {


    @Lazy
    @Autowired
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @Lazy
    @Autowired
    private lateinit var sseFilter: SseFilter

    @Bean
    fun securityFilterChain(http: HttpSecurity, authenticationProvider: AuthenticationProvider): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .addFilterAfter(sseFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authenticationProvider(authenticationProvider)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/user/create", "/login2").permitAll()
                    .requestMatchers("/game/backgammon/history/*").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { it.disable() }
            .build()
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