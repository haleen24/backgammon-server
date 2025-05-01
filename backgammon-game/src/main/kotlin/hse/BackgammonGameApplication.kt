package hse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class BackgammonGameApplication

fun main(args: Array<String>) {
    runApplication<BackgammonGameApplication>(*args)
}
