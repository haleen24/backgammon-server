package hse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackgammonGameApplication

fun main(args: Array<String>) {
    runApplication<BackgammonGameApplication>(*args)
}
