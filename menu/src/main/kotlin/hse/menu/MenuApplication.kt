 package hse.menu

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

 @SpringBootApplication
@EnableFeignClients
class MenuApplication

fun main(args: Array<String>) {
    runApplication<MenuApplication>(*args)
}
