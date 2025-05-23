package hse.playerservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories
class PlayerServiceApplication

fun main(args: Array<String>) {
    runApplication<PlayerServiceApplication>(*args)
}
