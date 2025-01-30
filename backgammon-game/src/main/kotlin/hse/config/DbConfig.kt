package hse.config

import de.hdi.mongobumblebee.MongoBumblebee
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DbConfig {

    @Bean
    fun mongoBumblebee(
        @Value("\${spring.data.mongodb.host}") host: String,
        @Value("\${spring.data.mongodb.port}") port: Int,
        @Value("\${spring.data.mongodb.database}") dbName: String,
    ): MongoBumblebee {
        val runner = MongoBumblebee("mongodb://$host:$port", dbName)
        runner.setChangeLogsScanPackage("hse.changelogs")
        return runner
    }
}