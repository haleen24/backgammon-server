package hse.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

@Configuration
class CacheConfig {

    @Bean
    fun jedisConfiguration(
        @Value("\${config.jedis.host}") host: String,
        @Value("\${config.jedis.port}") port: Int
    ): Jedis? {
        return try {
            JedisPool(host, port).resource
        } catch (_: RuntimeException) {
            null
        }
    }
}