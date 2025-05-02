package hse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableCaching
@EnableFeignClients
class BackgammonGameApplication

fun main(args: Array<String>) {
    runApplication<BackgammonGameApplication>(*args)
}
